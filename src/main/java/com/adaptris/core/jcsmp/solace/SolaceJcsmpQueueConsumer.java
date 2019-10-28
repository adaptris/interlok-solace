package com.adaptris.core.jcsmp.solace;

import java.util.concurrent.LinkedBlockingQueue;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.ExceptionHelper;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP component that consumes your messages from the Solace VPN.", tag="queue,consumer,solace,jcsmp")
@XStreamAlias("solace-jcsmp-queue-consumer")
public class SolaceJcsmpQueueConsumer extends AdaptrisMessageConsumerImp implements SolaceJcsmpReceiverStarter {
    
  @NotNull
  private String queueName;
  
  @NotNull
  @AutoPopulated
  private SolaceJcsmpMessageTranslator messageTranslator;
  
  private transient SolaceJcsmpMessageAcker messageAcker;
  
  private transient JCSMPFactory jcsmpFactory;
  
  private transient JCSMPSession currentSession;
  
  private transient FlowReceiver flowReceiver;
  
  public SolaceJcsmpQueueConsumer() {
    this.setMessageTranslator(new SolaceJcsmpBytesMessageTranslator());
  }

  @Override
  public void onException(JCSMPException exception) {
    // Assumes a connection error handler...
    log.error("Exception received from the JCSMP consumer, firing connection error handler.", exception);
    this.retrieveConnection(SolaceJcsmpConnection.class).getConnectionErrorHandler().handleConnectionException();
  }

  @Override
  public void onReceive(BytesXMLMessage message) {
    try {
      AdaptrisMessage adaptrisMessage = getMessageTranslator().translate(message);
      getMessageAcker().addUnacknowledgedMessage(message, adaptrisMessage.getUniqueId());
      
      log.trace("JCSMP Consumed message {} on thread {}", message.getMessageId(), Thread.currentThread().getName());
      
      retrieveAdaptrisMessageListener().onAdaptrisMessage(adaptrisMessage);
    } catch (Exception e) {
      log.error("Failed to translate message.", e);
    }
  }

  @Override
  public void startReceive() throws Exception {
    this.setCurrentSession(retrieveConnection(SolaceJcsmpConnection.class).createSession());
    
    final Queue queue = this.jcsmpFactory().createQueue(this.getQueueName());
    // Actually provision it, and do not fail if it already exists
    this.getCurrentSession().provision(queue, createEndpointProperties(), JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
    this.setFlowReceiver(this.getCurrentSession().createFlow(this, createConsumerFlowProperties(queue), createEndpointProperties()));
    
    this.getFlowReceiver().start();
  }
  
  @Override
  public void start() throws CoreException {
    try {
      this.startReceive();
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException("JCSMP Consumer, failed to start.", e);
    }
  }
  
  @Override
  public void init() throws CoreException {
  }
  
  @Override
  public void stop() {
    if(this.getFlowReceiver() != null)
      this.getFlowReceiver().stop();
  }

  @Override
  public void close() {
    if(this.getFlowReceiver() != null)
      this.getFlowReceiver().close();
  }
  
  @Override
  public void prepare() throws CoreException { 
  }

  private ConsumerFlowProperties createConsumerFlowProperties(Queue queue) {
    final ConsumerFlowProperties flowProps = new ConsumerFlowProperties();
    flowProps.setEndpoint(queue);
    flowProps.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
    
    return flowProps;
  }
  
  private EndpointProperties createEndpointProperties() {
    final EndpointProperties endpointProps = new EndpointProperties();
    // set queue permissions to "consume" and access-type to "exclusive"
    endpointProps.setPermission(EndpointProperties.PERMISSION_CONSUME);
    endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);
    
    return endpointProps;
  }
  
  class LimitedQueue<E> extends LinkedBlockingQueue<E> {
    private static final long serialVersionUID = -591885796518346827L;

    public LimitedQueue(int maxSize) {
      super(maxSize);
    }

    @Override
    public boolean offer(E e) {
      try {
        put(e);
        return true;
      } catch (InterruptedException ie) {
        Thread.currentThread().interrupt();
      }
      return false;
    }
  }
  
  JCSMPFactory jcsmpFactory() {
    return ObjectUtils.defaultIfNull(this.getJcsmpFactory(), JCSMPFactory.onlyInstance());
  }
  
  JCSMPFactory getJcsmpFactory() {
    return jcsmpFactory;
  }

  void setJcsmpFactory(JCSMPFactory jcsmpFactory) {
    this.jcsmpFactory = jcsmpFactory;
  }

  JCSMPSession getCurrentSession() {
    return currentSession;
  }

  void setCurrentSession(JCSMPSession currentSession) {
    this.currentSession = currentSession;
  }

  FlowReceiver getFlowReceiver() {
    return flowReceiver;
  }

  void setFlowReceiver(FlowReceiver flowReceiver) {
    this.flowReceiver = flowReceiver;
  }

  public String getQueueName() {
    return queueName;
  }

  public void setQueueName(String queueName) {
    this.queueName = queueName;
  }

  public SolaceJcsmpMessageTranslator getMessageTranslator() {
    return messageTranslator;
  }

  public void setMessageTranslator(SolaceJcsmpMessageTranslator messageTranslator) {
    this.messageTranslator = messageTranslator;
  }

  SolaceJcsmpMessageAcker getMessageAcker() {
    return messageAcker;
  }

  void setMessageAcker(SolaceJcsmpMessageAcker messageAcker) {
    this.messageAcker = messageAcker;
  }

}
