package com.adaptris.core.jcsmp.solace;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.CoreException;
import com.solacesystems.jcsmp.BytesXMLMessage;

public class SolaceJcsmpBaseMessageAcker implements SolaceJcsmpMessageAcker {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  private transient Map<String, BytesXMLMessage> unacknowledgedMessages;
  
  private transient SolaceJcsmpWorkflow parentWorkflow;
  
  public SolaceJcsmpBaseMessageAcker() {
    this.setUnacknowledgedMessages(new HashMap<String, BytesXMLMessage>());
  }
  
  @Override
  public void acknowledge(String messageIdentifier) {
//    log.trace("Ack'ing message with identifier {} on thread {}", messageIdentifier, Thread.currentThread().getName());
    BytesXMLMessage bytesXMLMessage = this.getUnacknowledgedMessages().get(messageIdentifier);
    if(bytesXMLMessage == null)
      log.warn("Cannot Ack message; No unack'd message found with identifier {}", messageIdentifier);
    else {
      bytesXMLMessage.ackMessage();
      this.getUnacknowledgedMessages().remove(messageIdentifier);
    }
  }

  @Override
  public void addUnacknowledgedMessage(BytesXMLMessage message, String messageIdentifier) {
    this.getUnacknowledgedMessages().put(messageIdentifier, message);
//    log.trace("Adding unack'd message with identifier {}, count now stands at [{}]", messageIdentifier, this.getUnacknowledgedMessageCount());
  }

  @Override
  public int getUnacknowledgedMessageCount() {
    return this.getUnacknowledgedMessages().size();
  }

  @Override
  public void init() throws CoreException {
    this.getUnacknowledgedMessages().clear();
  }
  
  @Override
  public void prepare() throws CoreException {
  }

  Map<String, BytesXMLMessage> getUnacknowledgedMessages() {
    return unacknowledgedMessages;
  }

  void setUnacknowledgedMessages(Map<String, BytesXMLMessage> unacknowledgedMessages) {
    this.unacknowledgedMessages = unacknowledgedMessages;
  }

  SolaceJcsmpWorkflow getParentWorkflow() {
    return parentWorkflow;
  }

  public void setParentWorkflow(SolaceJcsmpWorkflow parentWorkflow) {
    this.parentWorkflow = parentWorkflow;
  }
}
