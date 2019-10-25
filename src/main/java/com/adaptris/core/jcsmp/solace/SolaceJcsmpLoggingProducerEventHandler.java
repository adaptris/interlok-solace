package com.adaptris.core.jcsmp.solace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;

public class SolaceJcsmpLoggingProducerEventHandler implements JCSMPStreamingPublishCorrelatingEventHandler {

  protected transient Logger log;
  
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
  }
  
  
  @Override
  public void responseReceived(String messageId) {
    // never called - deprecated
  }
  
  @Override
  public void handleError(String messageId, JCSMPException exception, long timestamp) {
    // never called - deprecated.
  }

  public Logger getLog() {
    return log;
  }

  public void setLog(Logger log) {
    this.log = log;
  }

}
