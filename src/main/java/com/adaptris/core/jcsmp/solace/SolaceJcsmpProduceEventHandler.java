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
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;

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
      }
      
      logRemainingUnAckedMessages();
    } else {
      log.warn("Executing producer restart, not accepting further success callbacks until complete.");
    }
  }

  public void addUnAckedMessage(String messageId, AdaptrisMessage message) {
    new CallbackConsumers(message,
        (Consumer<AdaptrisMessage>) message.getObjectHeaders().get(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK),
        (Consumer<AdaptrisMessage>) message.getObjectHeaders().get(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK));
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
