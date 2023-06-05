package com.adaptris.core.jcsmp.solace;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.jcsmp.solace.auth.BasicAuthenticationProvider;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.XMLMessageListener;
import com.solacesystems.jcsmp.XMLMessageProducer;

public class TestBridge implements XMLMessageListener {

  private static final double MILLION = 1000000L;

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private static final String HOST = "tcp://localhost:55555";

  private static final String VPN = "default";

  private static final String USER = "admin";

  private static final String PASS = "admin";

  private static final String CONSUME_QUEUE = "Sample.Q1";

  private static final String PRODUCE_QUEUE = "Sample.Q2";

  private transient Map<Long, BytesXMLMessage> unacknowledgedMessages;

  private FlowReceiver flowReceiver;

  private SolaceJcsmpConnection connection;

  private Queue produceQueue;

  private Queue consumeQueue;

  private ThreadLocal<SolaceProducerSession> threadLocalProducerSession = new ThreadLocal<>();

  private JCSMPSession consumerSession;

  private int messageCount = 0;

  private long totalNanos = 0;

  // private ExecutorService executor;

  private long startMs;

  public TestBridge() {
    unacknowledgedMessages = new HashMap<>();

    // executor = new ThreadPoolExecutor(10, 10, 1L, TimeUnit.MINUTES, new LimitedQueue<>(10));
  }

  public static void main(String[] args) {
    new Thread(() -> {
      new TestBridge().doBridge();
    }).start();
  }

  private void doBridge() {
    connection = new SolaceJcsmpConnection();
    connection.setHost(HOST);
    BasicAuthenticationProvider provider = new BasicAuthenticationProvider();
    provider.setUsername(USER);
    provider.setPassword(PASS);
    connection.setVpn(VPN);

    try {
      consumerSession = connection.createSession();

      consumeQueue = JCSMPFactory.onlyInstance().createQueue(CONSUME_QUEUE);
      // Actually provision it, and do not fail if it already exists
      consumerSession.provision(consumeQueue, createEndpointProperties(), JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
      flowReceiver = consumerSession.createFlow(this, createConsumerFlowProperties(consumeQueue), createEndpointProperties());

      startMs = System.currentTimeMillis();

      flowReceiver.start();
    } catch (Exception ex) {
      log.error("Setup failed", ex);
    }
    try {
      Thread.currentThread().join();
    } catch (InterruptedException e) {
      log.debug("Ending program.");
    }
  }

  @Override
  public void onException(JCSMPException exception) {
    log.error("OnException: ", exception);
  }

  @Override
  public void onReceive(BytesXMLMessage message) {
    log.debug("Received message with ID: {}", message.getAckMessageId());

    unacknowledgedMessages.put(message.getAckMessageId(), message);

    // executor.execute(new Runnable() {
    // @Override
    // public void run() {
    try {
      onMessageDoSend(message);
    } catch (Exception e) {
      log.error("Error processing message.", e);
    }
    // }
    // });
  }

  @Override
  protected void finalize() {
    SolaceProducerSession solaceProducerSession = threadLocalProducerSession.get();
    if (solaceProducerSession != null) {
      solaceProducerSession.session.closeSession();
    }
    flowReceiver.stop();
    consumerSession.closeSession();
  }

  private void onMessageDoSend(BytesXMLMessage message) throws Exception {
    SolaceProducerSession solaceProducerSession = threadLocalProducerSession.get();
    if (solaceProducerSession == null) {
      solaceProducerSession = new SolaceProducerSession();
    }
    solaceProducerSession.refreshIfNeeded(new EventHandler());

    threadLocalProducerSession.set(solaceProducerSession);

    if (produceQueue == null) {
      produceQueue = JCSMPFactory.onlyInstance().createQueue(PRODUCE_QUEUE);
    }

    long start = System.nanoTime();

    TextMessage textMessage = JCSMPFactory.onlyInstance().createMessage(TextMessage.class);
    textMessage.setText("Content");
    textMessage.setDeliveryMode(DeliveryMode.PERSISTENT);
    textMessage.setCorrelationKey(message.getAckMessageId());

    try {
      solaceProducerSession.producer.send(textMessage, produceQueue);
    } catch (JCSMPException e) {
      log.error("Failed to send message", e);
    }

    double totalTime = System.nanoTime() - start;
    totalNanos += totalTime;
    messageCount++;
    long avgNanos = totalNanos / messageCount;

    log.trace("Message with ID {} produced in {}, avg {}", message.getAckMessageId(), totalTime / MILLION, avgNanos / MILLION);

    if (messageCount >= 10000) {
      long timetaken = (System.currentTimeMillis() - startMs) / 1000;
      log.info("Processing {} messages per second.", messageCount / timetaken);
    }
  }

  JCSMPSession session(JCSMPSession yourSession) throws Exception {
    if (yourSession == null || yourSession.isClosed()) {
      yourSession = connection.createSession();
    }

    return yourSession;
  }

  private ConsumerFlowProperties createConsumerFlowProperties(Queue queue) {
    final ConsumerFlowProperties flowProps = new ConsumerFlowProperties();
    flowProps.setEndpoint(queue);
    flowProps.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);

    return flowProps;
  }

  private EndpointProperties createEndpointProperties() {
    final EndpointProperties endpointProps = new EndpointProperties();
    // set queue permissions to "consume" and access-type to "non-exclusive"
    endpointProps.setPermission(EndpointProperties.PERMISSION_CONSUME);
    endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);

    return endpointProps;
  }

  class EventHandler implements JCSMPStreamingPublishCorrelatingEventHandler {

    @Override
    public void handleError(String arg0, JCSMPException arg1, long arg2) {
      log.trace("HandleError");
    }

    @Override
    public void responseReceived(String arg0) {
      log.trace("ResponseReceived");
    }

    @Override
    public void handleErrorEx(Object messageId, JCSMPException exception, long arg2) {
      log.error("Error response received for ID {}", messageId, exception);
    }

    @Override
    public void responseReceivedEx(Object messageId) {
      log.trace("Callback received with id {}", messageId);

      if (unacknowledgedMessages.containsKey(messageId)) {
        unacknowledgedMessages.get(messageId).ackMessage();
        unacknowledgedMessages.remove(messageId);
      } else {
        log.warn("No message found with ID {}, skipping Ack.", messageId);
      }
      log.trace("Unack'ed message cound = {}", unacknowledgedMessages.size());
    }
  }

  class LimitedQueue<E> extends LinkedBlockingQueue<E> {
    private static final long serialVersionUID = -7860829299905178920L;

    public LimitedQueue(int maxSize) {
      super(maxSize);
    }

    @Override
    public boolean offer(E e) {
      try {
        put(e);
        return true;
      } catch (Exception ex) {
        Thread.currentThread().interrupt();
      }
      return false;
    }
  }

  class SolaceProducerSession {
    private JCSMPSession session;
    private XMLMessageProducer producer;

    boolean needsRefresh() {
      if (session == null || session.isClosed() || producer == null) {
        return true;
      } else {
        return false;
      }
    }

    void refreshIfNeeded(JCSMPStreamingPublishCorrelatingEventHandler eventHandler) throws Exception {
      if (needsRefresh()) {
        session = connection.createSession();
        producer = session.getMessageProducer(eventHandler);
      }
    }
  }

}
