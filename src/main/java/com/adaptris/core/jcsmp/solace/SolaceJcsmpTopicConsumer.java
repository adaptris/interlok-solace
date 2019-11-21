package com.adaptris.core.jcsmp.solace;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This implementation of {@link AdaptrisMessageConsumer} will use the Solace Jcsmp Api to consume messages from a Topic on your Solace router.
 * </p>
 * <p>
 * There are four main components that you will need to configure;
 * <ul>
 * <li><b>Destination: </b> The Solace end point to consume messages from.</li>
 * <li><b>End point permissions: </b> Should match the Solace configured end point properties.</li>
 * <li><b>End point access type: </b> Should match either EXCLUSIVE or NONEXCLUSIVE</li>
 * <li><b>Acknowledge mode: </b> Should either be CLIENT or AUTO.</li>
 * </ul>
 * </p>
 * @author aaron
 * @version="3.9.3"
 * @config solace-jcsmp-topic-consumer
 */
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
