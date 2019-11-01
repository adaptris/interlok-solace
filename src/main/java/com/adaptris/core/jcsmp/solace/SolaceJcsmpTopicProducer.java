package com.adaptris.core.jcsmp.solace;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ProduceDestination;
import com.solacesystems.jcsmp.Destination;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP component will produce your messages to the Solace VPN topic.", tag="topic,producer,solace,jcsmp")
@XStreamAlias("solace-jcsmp-topic-producer")
public class SolaceJcsmpTopicProducer extends SolaceJcsmpAbstractProducer {

  public SolaceJcsmpTopicProducer() {
    super();
  }

  protected Destination generateDestination(AdaptrisMessage msg, ProduceDestination destination) throws Exception {
    String topicName = msg.resolve(destination.getDestination(msg));
    if(this.getDestinationCache().containsKey(topicName))
      return this.getDestinationCache().get(topicName);
    else {
      this.getDestinationCache().put(topicName, this.jcsmpFactory().createTopic(topicName));
      return this.generateDestination(msg, destination);
    }
  }

}
