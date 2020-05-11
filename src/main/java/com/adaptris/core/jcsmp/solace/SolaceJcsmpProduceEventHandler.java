package com.adaptris.core.jcsmp.solace;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;

public class SolaceJcsmpProduceEventHandler implements JCSMPStreamingPublishEventHandler {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  
  public static final String SOLACE_LATCH_KEY = "solaceJcsmpLath";

  private AdaptrisMessage message;

  public SolaceJcsmpProduceEventHandler(AdaptrisMessage message) {
    this.setMessage(message);
  }
  
  @Override
  public void handleError(String messageId, JCSMPException ex, long arg2) {
    log.error("Failed to send JCSMP message.", ex);
    this.getMessage().getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION, ex);
  }

  @Override
  public void responseReceived(String messageId) {
    CountDownLatch asyncLatch = (CountDownLatch) this.getMessage().getObjectHeaders().get(SOLACE_LATCH_KEY);
    if(asyncLatch == null) {
      this.handleError(messageId, new JCSMPException("Producer latch not found in AdaptrisMessage"), 0l);
    } else {
      asyncLatch.countDown();
    }
  }

  public AdaptrisMessage getMessage() {
    return message;
  }

  public void setMessage(AdaptrisMessage message) {
    this.message = message;
  }

}
