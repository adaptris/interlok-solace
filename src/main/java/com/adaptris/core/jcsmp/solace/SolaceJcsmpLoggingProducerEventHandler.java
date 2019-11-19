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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.solacesystems.jcsmp.JCSMPException;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * 
 * @author aaron
 *
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
    return ObjectUtils.defaultIfNull(this.getTriggerErrorHandlerOnFailure(), true);
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
