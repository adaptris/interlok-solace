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
