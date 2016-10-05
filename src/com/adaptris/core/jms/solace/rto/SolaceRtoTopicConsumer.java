package com.adaptris.core.jms.solace.rto;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.licensing.License;
import com.adaptris.core.licensing.License.LicenseType;
import com.adaptris.core.licensing.LicensedComponent;
import com.solacesystems.solclientj.core.SolEnum;
import com.solacesystems.solclientj.core.Solclient;
import com.solacesystems.solclientj.core.event.MessageCallback;
import com.solacesystems.solclientj.core.event.SessionEventCallback;
import com.solacesystems.solclientj.core.handle.Handle;
import com.solacesystems.solclientj.core.handle.MessageHandle;
import com.solacesystems.solclientj.core.handle.MessageSupport;
import com.solacesystems.solclientj.core.handle.SessionHandle;
import com.solacesystems.solclientj.core.resource.Topic;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("solace-rto-topic-consumer")
public class SolaceRtoTopicConsumer extends AdaptrisMessageConsumerImp implements MessageCallback, SessionEventCallback, LicensedComponent {

  private transient SessionHandle session;
  
  private transient Topic solaceDestination;
  
  @NotNull
  @AutoPopulated
  private SolaceRtoMessageTranslator messageTranslator;
  
  public SolaceRtoTopicConsumer() {
    this.setMessageTranslator(new SolaceRtoTextMessageTranslator());
  }
  
  @Override
  public void onMessage(Handle handle) {
    MessageSupport messageSupport = (MessageSupport) handle;
    MessageHandle takenMessage = Solclient.Allocator.newMessageHandle();
    messageSupport.takeRxMessage(takenMessage);
    
    AdaptrisMessage adaptrisMessage = null;
    try {
      adaptrisMessage = this.getMessageTranslator().translate(takenMessage);
    } catch (CoreException e) {
      e.printStackTrace();
    }
    
    takenMessage.destroy();
    
    this.retrieveAdaptrisMessageListener().onAdaptrisMessage(adaptrisMessage);
  }

  @Override
  public void init() throws CoreException {
    session = this.retrieveConnection(SolaceRtoConnection.class).createSession(this);
    solaceDestination = Solclient.Allocator.newTopic(getDestination().getDestination());
  }

  @Override
  public void start() throws CoreException {
    session.connect();
    session.subscribe(solaceDestination, SolEnum.SubscribeFlags.WAIT_FOR_CONFIRM, 0);
  }

  @Override
  public void stop() {
    if((session != null) && (session.isBound())) {
      session.unsubscribe(solaceDestination, 0, 0);
      session.disconnect();
    }
  }

  @Override
  public void close() {
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  public boolean isEnabled(License license) {
    return license.isEnabled(LicenseType.Standard);
  }

  public SolaceRtoMessageTranslator getMessageTranslator() {
    return messageTranslator;
  }

  public void setMessageTranslator(SolaceRtoMessageTranslator messageTranslator) {
    this.messageTranslator = messageTranslator;
  }

  @Override
  public void onEvent(SessionHandle sessionHandle) {
  }


}
