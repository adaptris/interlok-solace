package com.adaptris.core.jcsmp.solace;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.JmsConsumer;
import com.adaptris.core.jms.JmsPollingConsumer;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;

/**
 * <p>
 * A Solace JCSMP asynchronous producer event handler that is registered to the Solace producer.
 * </p>
 * <p>
 * After a message has been produced to the Solace VPN this event handler will be called at some point in the future
 * with either a success (the message was received without error) or failure (an error occurred on the Solace VPN). 
 * </p>
 * <p>
 * Once either a success or failure event for any given produced message is received this event handler will 
 * execute the registered AdaptrisMessage's success or failure code.  The code executed on success or fail is determined by the 
 * consumer that originally created the message.  For example; if your consumer is JMS, then on success of the producer producing the message
 * the success code will likely be an acknowledgement/commit of the originally consumed message.
 * </p>
 * <p>
 * <b>NOTE:</b> The success and failure code executed will not run in the workflow thread, but a callback thread maintained by the Solace client API.
 * Therefore if you're bridging a JMS consumer to this Solace producer, then you must make sure your JMS consumer is run in asynchronous mode too.
 * Standard rules of JMS will not allow you to act on (acknowledge/commit) a consumed message in a thread that is not the delivery thread.
 * Simply make sure your not using a JMS message listener such as {@link JmsConsumer}, but instead an asynchronous consumer like {@link JmsPollingConsumer}.
 * </p>
 * @author Aaron
 *
 */
public class SolaceJcsmpProduceEventHandler implements JCSMPStreamingPublishEventHandler, ComponentLifecycle {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  
  public static final String SOLACE_LATCH_KEY = "solaceJcsmpLath";
  
  private transient Map<String, CallbackConsumers> unAckedMessages;
  
  private transient SolaceJcsmpAbstractProducer producer;
  
  private volatile boolean acceptSuccessCallbacks;

  public SolaceJcsmpProduceEventHandler(SolaceJcsmpAbstractProducer producer) {
    this.setProducer(producer);
  }
  
  @Override
  public void handleError(String messageId, JCSMPException ex, long arg2) {
    setAcceptSuccessCallbacks(false);
    log.error("Received failure callback from Solace for message id {}", messageId);
    logRemainingUnAckedMessages();
    
    CallbackConsumers callbackConsumers = this.getUnAckedMessages().get(messageId);
    if(callbackConsumers == null) {
      log.warn("Received failure callback for an unknown message {}", messageId);
    } else {
      defaultIfNull(callbackConsumers.getOnFailure(), (msg) -> {   }).accept(callbackConsumers.getMessage());
      getUnAckedMessages().remove(messageId);
    }
    
    getProducer().retrieveConnection(SolaceJcsmpConnection.class).getConnectionErrorHandler().handleConnectionException();
  }

  @Override
  public void responseReceived(String messageId) {
    log.debug("Success callback received from Solace for message id {}", messageId);
    if(this.isAcceptSuccessCallbacks()) {
      CallbackConsumers callbackConsumers = this.getUnAckedMessages().get(messageId);
      if(callbackConsumers == null) {
        log.warn("Received success callback for an unknown message {}", messageId);
      } else {
        defaultIfNull(callbackConsumers.getOnSuccess(), (msg) -> {   }).accept(callbackConsumers.getMessage());
        getUnAckedMessages().remove(messageId);
      }
      
      logRemainingUnAckedMessages();
    } else {
      log.warn("Executing producer restart, not accepting further success callbacks until complete.");
    }
  }

  public void addUnAckedMessage(String messageId, AdaptrisMessage message) {
    log.trace("Adding message to un'acked list with id {}", messageId);
    CallbackConsumers callbackConsumers = new CallbackConsumers(message,
        (Consumer<AdaptrisMessage>) message.getObjectHeaders().get(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK),
        (Consumer<AdaptrisMessage>) message.getObjectHeaders().get(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK));
    this.getUnAckedMessages().put(messageId, callbackConsumers);
  }

  private void logRemainingUnAckedMessages() {
    log.trace("{} messages waiting for async callback.", this.getUnAckedMessages().size());
  }
  
  @Override
  public void init() throws CoreException {
    this.setUnAckedMessages(new HashMap<>());
    this.setAcceptSuccessCallbacks(true);
  }
  
  class CallbackConsumers {
    
    private AdaptrisMessage message;
    
    private Consumer<AdaptrisMessage> onSuccess;
    private Consumer<AdaptrisMessage> onFailure;
    
    CallbackConsumers(AdaptrisMessage message, Consumer<AdaptrisMessage> onSuccess, Consumer<AdaptrisMessage> onFailure) {
      setMessage(message);
      setOnSuccess(onSuccess);
      setOnFailure(onFailure);
    }

    public Consumer<AdaptrisMessage> getOnSuccess() {
      return onSuccess;
    }

    public Consumer<AdaptrisMessage> getOnFailure() {
      return onFailure;
    }

    public void setOnSuccess(Consumer<AdaptrisMessage> onSuccess) {
      this.onSuccess = onSuccess;
    }

    public void setOnFailure(Consumer<AdaptrisMessage> onFailure) {
      this.onFailure = onFailure;
    }

    public AdaptrisMessage getMessage() {
      return message;
    }

    public void setMessage(AdaptrisMessage message) {
      this.message = message;
    }
  }

  public Map<String, CallbackConsumers> getUnAckedMessages() {
    return unAckedMessages;
  }

  public void setUnAckedMessages(Map<String, CallbackConsumers> unAckedMessages) {
    this.unAckedMessages = unAckedMessages;
  }

  public SolaceJcsmpAbstractProducer getProducer() {
    return producer;
  }

  public void setProducer(SolaceJcsmpAbstractProducer producer) {
    this.producer = producer;
  }

  public boolean isAcceptSuccessCallbacks() {
    return acceptSuccessCallbacks;
  }

  public void setAcceptSuccessCallbacks(boolean acceptSuccessCallbacks) {
    this.acceptSuccessCallbacks = acceptSuccessCallbacks;
  }
}
