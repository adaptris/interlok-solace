package com.adaptris.core.jcsmp.solace;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP component that consumes your topic messages from the Solace VPN.", tag="subscription,topic,consumer,solace,jcsmp")
@XStreamAlias("solace-jcsmp-topic-consumer")
public class SolaceJcsmpTopicConsumer extends SolaceJcsmpAbstractConsumer {

  private transient XMLMessageConsumer messageConsumer;
  
  public SolaceJcsmpTopicConsumer() {
    super();
  }
  
  @Override
  public void startReceive() throws Exception {
    this.setCurrentSession(retrieveConnection(SolaceJcsmpConnection.class).createSession());
    this.getCurrentSession().connect();
    
    this.setMessageConsumer(this.getCurrentSession().getMessageConsumer(this));
    
    final Topic topic = jcsmpFactory().createTopic(this.getDestination().getDestination());
    this.getCurrentSession().addSubscription(topic);
    this.getMessageConsumer().start();
  }

  @Override
  public void stop() {
    if(this.getMessageConsumer() != null)
      this.getMessageConsumer().stop();
    super.stop();
  }

  @Override
  public void close() {
    if(this.getMessageConsumer() != null)
      this.getMessageConsumer().close();
    super.close();
  }
  
  XMLMessageConsumer getMessageConsumer() {
    return messageConsumer;
  }

  void setMessageConsumer(XMLMessageConsumer messageConsumer) {
    this.messageConsumer = messageConsumer;
  }

}
