package com.adaptris.core.jcsmp.solace;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP component that consumes your messages from the Solace VPN.", tag="queue,consumer,solace,jcsmp")
@XStreamAlias("solace-jcsmp-queue-consumer")
public class SolaceJcsmpQueueConsumer extends SolaceJcsmpAbstractConsumer {
  
  private transient FlowReceiver flowReceiver;
  
  public SolaceJcsmpQueueConsumer() {
    super();
  }

  @Override
  public void startReceive() throws Exception {
    this.setCurrentSession(retrieveConnection(SolaceJcsmpConnection.class).createSession());
    
    final Queue queue = this.jcsmpFactory().createQueue(this.getDestination().getDestination());
    // Actually provision it, and do not fail if it already exists
    this.getCurrentSession().provision(queue, createEndpointProperties(), JCSMPSession.FLAG_IGNORE_ALREADY_EXISTS);
    this.setFlowReceiver(this.getCurrentSession().createFlow(this, createConsumerFlowProperties(queue), createEndpointProperties()));
        
    this.getFlowReceiver().start();
  }
  
  @Override
  public void stop() {
    if(this.getFlowReceiver() != null)
      this.getFlowReceiver().stop();
    super.stop();
  }

  @Override
  public void close() {
    if(this.getFlowReceiver() != null)
      this.getFlowReceiver().close();
    super.close();
  }

  FlowReceiver getFlowReceiver() {
    return flowReceiver;
  }

  void setFlowReceiver(FlowReceiver flowReceiver) {
    this.flowReceiver = flowReceiver;
  }

}
