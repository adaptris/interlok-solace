package com.adaptris.core.jcsmp.solace;

import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;

public interface SolaceJcsmpProducerEventHandler extends JCSMPStreamingPublishCorrelatingEventHandler {

  public void setProducer(SolaceJcsmpAbstractProducer producer);
  
}
