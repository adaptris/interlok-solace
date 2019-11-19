/*
 * Copyright 2015 Adaptris Ltd.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

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
    log.trace("Ack'ing message with identifier {} on thread {}", messageIdentifier, Thread.currentThread().getName());
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
    log.trace("Adding unack'd message with identifier {}, count now stands at [{}]", messageIdentifier, this.getUnacknowledgedMessageCount());
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
