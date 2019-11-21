package com.adaptris.core.jcsmp.solace;

import org.apache.commons.lang3.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.solacesystems.jcsmp.JCSMPException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Will simply log the success or failure future event of the asynchronous producer.
 * </p>
 * <p>
 * We can also trigger the connection error handler on failure if configured to do so with the 
 * trigger-error-handler-on-failure = true (default is false.)
 * </p>
 * @author aaron
 * @version=”3.9.3”
 * @config solace-jcsmp-logging-producer-event-handler
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP component that listens and simply logs produced message events.", tag="ack,solace,jcsmp")
@XStreamAlias("solace-jcsmp-logging-producer-event-handler")
public class SolaceJcsmpLoggingProducerEventHandler implements SolaceJcsmpProducerEventHandler {

  protected transient Logger log;
  
  private Boolean triggerErrorHandlerOnFailure;
  
  private transient SolaceJcsmpAbstractProducer producer;
  
  public SolaceJcsmpLoggingProducerEventHandler() {
    this.setLog(LoggerFactory.getLogger(this.getClass().getName()));
  }
  
  @Override
  public void responseReceivedEx(Object messageId) {
    getLog().debug("Received producer callback success from Solace for message [{}]", messageId);
  }
  
  @Override
  public void handleErrorEx(Object messageId, JCSMPException exception, long timestamp) {
    getLog().error("Received producer callback error from Solace for message [{}]", messageId, exception);
    if(triggerErrorHandlerOnFailure()) {
      this.getProducer().retrieveConnection(SolaceJcsmpConnection.class).getConnectionErrorHandler().handleConnectionException();
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

  Logger getLog() {
    return log;
  }

  void setLog(Logger log) {
    this.log = log;
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
