package com.adaptris.core.jcsmp.solace;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.jcsmp.solace.util.Timer;
import com.adaptris.core.util.ExceptionHelper;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;

public abstract class SolaceJcsmpAbstractConsumer  extends AdaptrisMessageConsumerImp implements SolaceJcsmpReceiverStarter {

  @NotNull
  @AutoPopulated
  private SolaceJcsmpMessageTranslator messageTranslator;
  
  private transient SolaceJcsmpMessageAcker messageAcker;
  
  private transient JCSMPFactory jcsmpFactory;
  
  private transient JCSMPSession currentSession;
  
  public SolaceJcsmpAbstractConsumer() {
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
      Timer.start("OnReceive", null);
      AdaptrisMessage adaptrisMessage = getMessageTranslator().translate(message);
      getMessageAcker().addUnacknowledgedMessage(message, adaptrisMessage.getUniqueId());
      
      log.trace("JCSMP Consumed message {} on thread {}", message.getMessageId(), Thread.currentThread().getName());
      
      retrieveAdaptrisMessageListener().onAdaptrisMessage(adaptrisMessage);
      Timer.stopAndLog("OnReceive");
    } catch (Exception e) {
      log.error("Failed to translate message.", e);
    }
  }

  @Override
  public void prepare() throws CoreException {
    
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
  public void stop() {
    this.getCurrentSession().closeSession();
  }
  
  protected ConsumerFlowProperties createConsumerFlowProperties(Queue queue) {
    final ConsumerFlowProperties flowProps = new ConsumerFlowProperties();
    flowProps.setEndpoint(queue);
    flowProps.setAckMode(JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT);
    
    return flowProps;
  }
  
  protected EndpointProperties createEndpointProperties() {
    final EndpointProperties endpointProps = new EndpointProperties();
    // set queue permissions to "consume" and access-type to "exclusive"
    endpointProps.setPermission(EndpointProperties.PERMISSION_CONSUME);
    endpointProps.setAccessType(EndpointProperties.ACCESSTYPE_NONEXCLUSIVE);
    
    return endpointProps;
  }

  @Override
  public abstract void startReceive() throws Exception;

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
