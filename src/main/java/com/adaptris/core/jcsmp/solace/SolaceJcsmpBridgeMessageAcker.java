package com.adaptris.core.jcsmp.solace;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.CoreException;
import com.adaptris.core.jcsmp.solace.util.Timer;
import com.solacesystems.jcsmp.JCSMPException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This implementation of the {@link SolaceJcsmpMessageAcker} should only be used when you're bridging messages
 * between Solace end points.
 * </p>
 * <p>
 * We will expect the configured producer to report a successful message delivery, which will trigger this class to fire the message acknowledgement on the consumer.
 * </p>
 * @author aaron
 * @version="3.9.3"
 * @config solace-jcsmp-bridge-message-acker
 *
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP component used to acknowledge consumed messages, based on produced message events.", tag="ack,solace,jcsmp")
@XStreamAlias("solace-jcsmp-bridge-message-acker")
public class SolaceJcsmpBridgeMessageAcker extends SolaceJcsmpBaseMessageAcker implements SolaceJcsmpProducerEventHandler {

  private transient SolaceJcsmpAbstractProducer producer;
  
  private Boolean triggerErrorHandlerOnFailure;
  
  public SolaceJcsmpBridgeMessageAcker() {
  }
  
  @Override
  public void responseReceivedEx(Object messageId) {
    Timer.start("OnReceive", "Ack", 1000);
    super.acknowledge((String) messageId);
    Timer.stop("OnReceive", "Ack");
  }
  
  @Override
  public void handleErrorEx(Object messageId, JCSMPException exception, long timestamp) {
    log.error("Received producer callback error from Solace for message [{}]", messageId, exception);
    if(triggerErrorHandlerOnFailure()) {
      this.getProducer().retrieveConnection(SolaceJcsmpConnection.class).getConnectionErrorHandler().handleConnectionException();
    }
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
  
  boolean triggerErrorHandlerOnFailure() {
    return ObjectUtils.defaultIfNull(this.getTriggerErrorHandlerOnFailure(), false);
  }

  public Boolean getTriggerErrorHandlerOnFailure() {
    return triggerErrorHandlerOnFailure;
  }

  public void setTriggerErrorHandlerOnFailure(Boolean triggerErrorHandlerOnFailure) {
    this.triggerErrorHandlerOnFailure = triggerErrorHandlerOnFailure;
  }
  
  SolaceJcsmpAbstractProducer getProducer() {
    return producer;
  }

  public void setProducer(SolaceJcsmpAbstractProducer producer) {
    this.producer = producer;
  }

}
