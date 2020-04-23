package com.adaptris.core.jcsmp.solace;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.jcsmp.solace.util.Timer;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.util.NumberUtils;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.Destination;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.XMLMessageProducer;

public abstract class SolaceJcsmpAbstractProducer extends ProduceOnlyProducerImp {

  private static final int DEFAULT_MAX_WAIT = 5000;
  
  @NotNull
  @AutoPopulated
  private SolaceJcsmpMessageTranslator messageTranslator;
  
  @InputFieldDefault(value = "5000")
  private Integer maxWaitOnProduceMillis;
  
  private transient JCSMPFactory jcsmpFactory;
  
  private transient JCSMPSession currentSession;
  
  private transient XMLMessageProducer messageProducer;
  
  private transient Map<String, Destination> destinationCache;
  
  @AdvancedConfig(rare=true)
  @InputFieldDefault(value = "false")
  private Boolean traceLogTimings;
  
  public SolaceJcsmpAbstractProducer() {
    this.setMessageTranslator(new SolaceJcsmpBytesMessageTranslator());
    this.setDestinationCache(new HashMap<String, Destination>());
  }
  @Override
  public void prepare() throws CoreException {
  }
  
  @Override
  public void init() throws CoreException {
    this.getDestinationCache().clear();
    this.setMessageProducer(null);
    this.setCurrentSession(null);
  }

  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
    try {
      Timer.start("OnProduce", 100);
      CountDownLatch messageLatch = new CountDownLatch(1);
      msg.getObjectHeaders().put(SolaceJcsmpProduceEventHandler.SOLACE_LATCH_KEY, messageLatch);
      Destination dest = generateDestination(msg, destination);
      
      Timer.start("OnProduce", "setupProducer", 100);
      XMLMessageProducer jcsmpMessageProducer = messageProducer(msg);
      Timer.stop("OnProduce", "setupProducer");
      
      Timer.start("OnProduce", "Producer-Translator", 100);
      BytesXMLMessage translatedMessage = this.getMessageTranslator().translate(msg);
      Timer.stop("OnProduce", "Producer-Translator");
      // assumes the message ID is the same as that assigned by the consumer.
      // If we're using a splitter this may break.
      translatedMessage.setCorrelationKey(msg.getUniqueId());
      
      Timer.start("OnProduce", "Producer", 100);
      jcsmpMessageProducer.send(translatedMessage, dest);
      Timer.stop("OnProduce", "Producer");
      
      Timer.start("OnProduce", "AwaitAsyncCallback", 100);
      if(!messageLatch.await(maxWaitOnProduceMillis(), TimeUnit.MILLISECONDS))
        throw new ProduceException("VPN has not ackowledged our sent message in time.");
      Timer.stop("OnProduce", "AwaitAsyncCallback");

      Timer.stop("OnProduce");
      if(traceLogTimings())
        Timer.log("OnProduce");
    } catch (Exception ex) {
      this.setMessageProducer(null);
      throw ExceptionHelper.wrapProduceException(ex);
    }
  }
  
  protected abstract Destination generateDestination(AdaptrisMessage msg, ProduceDestination destination) throws Exception;
  
  JCSMPSession session() throws Exception {
    if((this.getCurrentSession() == null) || (this.getCurrentSession().isClosed()))
      this.setCurrentSession(this.retrieveConnection(SolaceJcsmpConnection.class).createSession());

    return this.getCurrentSession();
  }
  
  XMLMessageProducer messageProducer(AdaptrisMessage msg) throws JCSMPException, Exception {
    if((this.getMessageProducer() == null) || (this.getCurrentSession() == null) || (this.getCurrentSession().isClosed()))
    this.setMessageProducer(session().getMessageProducer(new SolaceJcsmpProduceEventHandler(msg)));
    return this.getMessageProducer();
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
    this.destinationCache = queueCache;
  }
  
  /**
   * This is the maximum amount in milliseconds of time to wait for the Solace VPN send a success or failure message back 
   * after producing a message.  Should this time expire, the produced message will be deemed to have failed.
   * @return
   */
  public Integer getMaxWaitOnProduceMillis() {
    return maxWaitOnProduceMillis;
  }
  
  public void setMaxWaitOnProduceMillis(Integer maxWaitOnProduceMillis) {
    this.maxWaitOnProduceMillis = maxWaitOnProduceMillis;
  }

  int maxWaitOnProduceMillis() {
    return NumberUtils.toIntDefaultIfNull(this.getMaxWaitOnProduceMillis(), DEFAULT_MAX_WAIT);
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
    return ObjectUtils.defaultIfNull(this.getTraceLogTimings(), false);
  }
}
