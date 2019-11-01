package com.adaptris.core.jcsmp.solace;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.jcsmp.solace.util.Timer;
import com.adaptris.core.util.ExceptionHelper;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.Destination;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import com.solacesystems.jcsmp.XMLMessageProducer;

public abstract class SolaceJcsmpAbstractProducer extends ProduceOnlyProducerImp {

  @NotNull
  @AutoPopulated
  private SolaceJcsmpMessageTranslator messageTranslator;
  
  private transient JCSMPStreamingPublishCorrelatingEventHandler producerEventHandler;
  
  private transient JCSMPFactory jcsmpFactory;
  
  private transient JCSMPSession currentSession;
  
  private transient XMLMessageProducer messageProducer;
  
  private transient Map<String, Destination> destinationCache;
  
  
  public SolaceJcsmpAbstractProducer() {
    this.setMessageTranslator(new SolaceJcsmpBytesMessageTranslator());
    this.setDestinationCache(new HashMap<String, Destination>());
    this.setProducerEventHandler(new SolaceJcsmpLoggingProducerEventHandler());
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
      Destination dest = generateDestination(msg, destination);
      XMLMessageProducer jcsmpMessageProducer = messageProducer();
      
      Timer.start("Producer-Translator", "Translating from Adp to Solace message");
      BytesXMLMessage translatedMessage = this.getMessageTranslator().translate(msg);
      Timer.stop("Producer-Translator");
      log.trace("Message translated to BytesXmlMessage in {} nanos", Timer.getLastTimingNanos("Producer-Translator"));
      // assumes the message ID is the same as that assigned by the consumer.
      // If we're using a splitter this may break.
      translatedMessage.setCorrelationKey(msg.getUniqueId());
      
      Timer.start("Producer", "JCSMP Send timing.");
      jcsmpMessageProducer.send(translatedMessage, dest);
      Timer.stop("Producer");
      
      log.trace("JCSMP Produced to {} on thread {} - [{} ms, {} avg ms {}]", dest.getName(), Thread.currentThread().getName(), Timer.getLastTimingMs("Producer"), Timer.getAvgTimingMs("Producer"));
    } catch (Exception ex) {
      throw ExceptionHelper.wrapProduceException(ex);
    }
  }
  
  protected abstract Destination generateDestination(AdaptrisMessage msg, ProduceDestination destination) throws Exception;
  
  JCSMPSession session() throws Exception {
    if((this.getCurrentSession() == null) || (this.getCurrentSession().isClosed()))
      this.setCurrentSession(this.retrieveConnection(SolaceJcsmpConnection.class).createSession());

    return this.getCurrentSession();
  }
  
  XMLMessageProducer messageProducer() throws JCSMPException, Exception {
    if(this.getMessageProducer() == null)
      this.setMessageProducer(session().getMessageProducer(this.getProducerEventHandler()));

    return this.getMessageProducer();
  }
  
  public SolaceJcsmpMessageTranslator getMessageTranslator() {
    return messageTranslator;
  }

  public void setMessageTranslator(SolaceJcsmpMessageTranslator messageTranslator) {
    this.messageTranslator = messageTranslator;
  }

  JCSMPStreamingPublishCorrelatingEventHandler getProducerEventHandler() {
    return producerEventHandler;
  }

  void setProducerEventHandler(JCSMPStreamingPublishCorrelatingEventHandler producerEventHandler) {
    this.producerEventHandler = producerEventHandler;
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

}
