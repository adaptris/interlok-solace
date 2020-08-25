package com.adaptris.core.jcsmp.solace;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.jcsmp.solace.util.Timer;
import com.adaptris.core.util.ExceptionHelper;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.Destination;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.XMLMessageProducer;

public abstract class SolaceJcsmpAbstractProducer extends ProduceOnlyProducerImp {

  @NotNull
  @AutoPopulated
  private SolaceJcsmpMessageTranslator messageTranslator;

  private transient JCSMPFactory jcsmpFactory;

  private transient JCSMPSession currentSession;

  private transient XMLMessageProducer messageProducer;

  private transient Map<String, Destination> destinationCache;
  
  private transient SolaceJcsmpProduceEventHandler asynEventHandler;

  @AdvancedConfig(rare=true)
  @InputFieldDefault(value = "false")
  private Boolean traceLogTimings;

  public SolaceJcsmpAbstractProducer() {
    setMessageTranslator(new SolaceJcsmpBytesMessageTranslator());
    setDestinationCache(new HashMap<String, Destination>());
    setAsynEventHandler(new SolaceJcsmpProduceEventHandler(this));
  }

  @Override
  public void init() throws CoreException {
    this.getAsynEventHandler().init();
    getDestinationCache().clear();
    setMessageProducer(null);
    setCurrentSession(null);
  }

  @Override
  public void doProduce(AdaptrisMessage msg, String queueOrTopic) throws ProduceException {
    try {
      Timer.start("OnProduce", 100);
      Destination dest = generateDestination(msg, queueOrTopic);

      Timer.start("OnProduce", "setupProducer", 100);
      XMLMessageProducer jcsmpMessageProducer = messageProducer(msg);
      Timer.stop("OnProduce", "setupProducer");

      Timer.start("OnProduce", "Producer-Translator", 100);
      BytesXMLMessage translatedMessage = getMessageTranslator().translate(msg);
      Timer.stop("OnProduce", "Producer-Translator");
      
      translatedMessage.setCorrelationKey(msg.getUniqueId());

      Timer.start("OnProduce", "Producer", 100);
      jcsmpMessageProducer.send(translatedMessage, dest);
      getAsynEventHandler().addUnAckedMessage(translatedMessage.getMessageId(), msg);
      Timer.stop("OnProduce", "Producer");
      // Standard workflow will attempt to execute this after the produce, 
      // let's remove them so it's handled by our async event handler.
      msg.getObjectHeaders().remove(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK);
      msg.getObjectHeaders().remove(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK);

      Timer.stop("OnProduce");
      if(traceLogTimings())
        Timer.log("OnProduce");
    } catch (Exception ex) {
      setMessageProducer(null);
      throw ExceptionHelper.wrapProduceException(ex);
    }
  }

  protected abstract Destination generateDestination(AdaptrisMessage msg, String queueOrTopic)
      throws Exception;

  JCSMPSession session() throws Exception {
    if((getCurrentSession() == null) || (getCurrentSession().isClosed()))
      setCurrentSession(this.retrieveConnection(SolaceJcsmpConnection.class).createSession());

    return getCurrentSession();
  }

  XMLMessageProducer messageProducer(AdaptrisMessage msg) throws JCSMPException, Exception {
    if((getMessageProducer() == null) || (getCurrentSession() == null) || (getCurrentSession().isClosed()))
    setMessageProducer(session().getMessageProducer(getAsynEventHandler()));
    return getMessageProducer();
  }

  public SolaceJcsmpMessageTranslator getMessageTranslator() {
    return messageTranslator;
  }

  /**
   * The message translator is responsible for translating the Solace JCSMP message object
   * into an {@link AdaptrisMessage} and the reverse.  The translator will typically handle the payload and the headers/metadata.
   * @param messageTranslator
   */
  public void setMessageTranslator(SolaceJcsmpMessageTranslator messageTranslator) {
    this.messageTranslator = messageTranslator;
  }

  JCSMPFactory jcsmpFactory() {
    return ObjectUtils.defaultIfNull(getJcsmpFactory(), JCSMPFactory.onlyInstance());
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

  XMLMessageProducer getMessageProducer() {
    return messageProducer;
  }

  void setMessageProducer(XMLMessageProducer messageProducer) {
    this.messageProducer = messageProducer;
  }

  Map<String, Destination> getDestinationCache() {
    return destinationCache;
  }

  void setDestinationCache(Map<String, Destination> queueCache) {
    destinationCache = queueCache;
  }

  public Boolean getTraceLogTimings() {
    return traceLogTimings;
  }

  /**
   * For debugging purposes you may want to see trace logging (in the interlok logs) of the steps
   * this producer will go through to translate, produce the message to the Solace VPN and
   * then wait for the acknowledgement.  The default value is false.
   * @param traceLogTimings
   */
  public void setTraceLogTimings(Boolean traceLogTimings) {
    this.traceLogTimings = traceLogTimings;
  }

  boolean traceLogTimings() {
    return ObjectUtils.defaultIfNull(getTraceLogTimings(), false);
  }

  public SolaceJcsmpProduceEventHandler getAsynEventHandler() {
    return asynEventHandler;
  }

  public void setAsynEventHandler(SolaceJcsmpProduceEventHandler asynEventHandler) {
    this.asynEventHandler = asynEventHandler;
  }
}
