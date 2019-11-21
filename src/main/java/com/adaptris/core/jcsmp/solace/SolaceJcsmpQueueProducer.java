package com.adaptris.core.jcsmp.solace;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.ProduceDestination;
import com.solacesystems.jcsmp.Destination;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This implementation of {@link AdaptrisMessageProducer} will use the Solace Jcsmp Api to produce messages to a queue on your Solace router.
 * </p>
 * 
 * @author aaron
 * @version="3.9.3"
 * @config solace-jcsmp-queue-producer
 * 
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP component will produce your messages to the Solace VPN queue.", tag="queue,producer,solace,jcsmp")
@XStreamAlias("solace-jcsmp-queue-producer")
public class SolaceJcsmpQueueProducer extends SolaceJcsmpAbstractProducer {
  
  public SolaceJcsmpQueueProducer() {
    super();
  }

  protected Destination generateDestination(AdaptrisMessage msg, ProduceDestination destination) throws Exception {
    String queueName = msg.resolve(destination.getDestination(msg));
    if(this.getDestinationCache().containsKey(queueName))
      return this.getDestinationCache().get(queueName);
    else {
      this.getDestinationCache().put(queueName, this.jcsmpFactory().createQueue(queueName));
      return this.generateDestination(msg, destination);
    }
  }

}
