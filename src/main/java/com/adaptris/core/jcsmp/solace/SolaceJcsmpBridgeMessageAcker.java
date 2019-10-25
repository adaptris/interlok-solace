package com.adaptris.core.jcsmp.solace;

import com.adaptris.core.CoreException;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;

public class SolaceJcsmpBridgeMessageAcker extends SolaceJcsmpBaseMessageAcker implements JCSMPStreamingPublishCorrelatingEventHandler {

  public SolaceJcsmpBridgeMessageAcker() {
  }
  
  @Override
  public void responseReceivedEx(Object messageId) {
    super.acknowledge((String) messageId);
  }
  
  @Override
  public void handleErrorEx(Object messageId, JCSMPException exception, long timestamp) {
    log.error("Received producer callback error from Solace for message [{}]", messageId, exception);
  }

  @Override
  public void prepare() throws CoreException {
    SolaceJcsmpWorkflow workflow = this.getParentWorkflow();
    if(workflow.getProducer() instanceof SolaceJcsmpQueueProducer) {
      SolaceJcsmpQueueProducer producer = (SolaceJcsmpQueueProducer) workflow.getProducer();
      producer.setProducerEventHandler(this);
    } else {
      throw new CoreException("Your workflow producer needs to be a Solace JCSMP producer to use Bridge mode acknowledgements.");
    }
  }
  
  @Override
  public void responseReceived(String messageId) {
    // never called - deprecated
  }
  
  @Override
  public void handleError(String messageId, JCSMPException exception, long timestamp) {
    // never called - deprecated.
  }

}
