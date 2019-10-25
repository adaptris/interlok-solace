package com.adaptris.core.jcsmp.solace;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.util.ExceptionHelper;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.XMLMessageProducer;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP component will produce your messages to the Solace VPN.", tag="queue,producer,solace,jcsmp")
@XStreamAlias("solace-jcsmp-queue-producer")
public class SolaceJcsmpQueueProducer extends ProduceOnlyProducerImp {

  @NotNull
  @AutoPopulated
  private SolaceJcsmpMessageTranslator messageTranslator;
  
  private transient JCSMPStreamingPublishCorrelatingEventHandler producerEventHandler;
  
  private transient JCSMPFactory jcsmpFactory;
  
  private transient JCSMPSession currentSession;
  
  private transient XMLMessageProducer messageProducer;
  
  private transient Map<String, Queue> queueCache;
  
  public SolaceJcsmpQueueProducer() {
    this.setMessageTranslator(new SolaceJcsmpBytesMessageTranslator());
    this.setQueueCache(new HashMap<String, Queue>());
    this.setProducerEventHandler(new SolaceJcsmpLoggingProducerEventHandler());
  }
  
  @Override
  public void prepare() throws CoreException {
    // TODO Auto-generated method stub
    
  }
  
  @Override
  public void init() throws CoreException {
    this.getQueueCache().clear();
    this.setMessageProducer(null);
    this.setCurrentSession(null);
  }

  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
    try {
      Queue queue = queue(msg, destination);
      XMLMessageProducer jcsmpMessageProducer = messageProducer();
      
      BytesXMLMessage translatedMessage = this.getMessageTranslator().translate(msg);
      // assumes the message ID is the same as that assigned by the consumer.
      // If we're using a splitter this may break.
      translatedMessage.setCorrelationKey(msg.getUniqueId());
      
      jcsmpMessageProducer.send(translatedMessage, queue);
    } catch (Exception ex) {
      throw ExceptionHelper.wrapProduceException(ex);
    }
    
  }
  
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

  Queue queue(AdaptrisMessage message, ProduceDestination destination) throws Exception {
    String queueName = message.resolve(destination.getDestination(message));
    if(this.getQueueCache().containsKey(queueName))
      return this.getQueueCache().get(queueName);
    else {
      this.getQueueCache().put(queueName, this.getJcsmpFactory().createQueue(queueName));
      return this.queue(message, destination);
    }
    
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

  Map<String, Queue> getQueueCache() {
    return queueCache;
  }

  void setQueueCache(Map<String, Queue> queueCache) {
    this.queueCache = queueCache;
  }

}
