package com.adaptris.core.jcsmp.solace;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.TextMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP message translator.", tag="solace,jcsmp,translator")
@XStreamAlias("solace-jcsmp-bytes-message-translator")
@DisplayOrder(order = {"messageFactory"})
public class SolaceJcsmpBytesMessageTranslator implements SolaceJcsmpMessageTranslator {

  @NotNull
  @AutoPopulated
  private AdaptrisMessageFactory messageFactory;

  private transient JCSMPFactory jcsmpFactory;
  
  public SolaceJcsmpBytesMessageTranslator() {
    this.setMessageFactory(messageFactory);
  }
  
  @Override
  public AdaptrisMessage translate(BytesXMLMessage message) throws Exception {
    AdaptrisMessage adaptrisMessage = this.messageFactory().newMessage();
    adaptrisMessage.setPayload(message.getBytes());
    
    return adaptrisMessage;
  }

  @Override
  public BytesXMLMessage translate(AdaptrisMessage message) throws Exception {
    TextMessage textMessage = this.jcsmpFactory().createMessage(TextMessage.class);
    textMessage.setText(message.getContent());
    
    return textMessage;
  }

  AdaptrisMessageFactory messageFactory() {
    return ObjectUtils.defaultIfNull(this.getMessageFactory(), DefaultMessageFactory.getDefaultInstance());
  }
  
  public AdaptrisMessageFactory getMessageFactory() {
    return messageFactory;
  }

  public void setMessageFactory(AdaptrisMessageFactory messageFactory) {
    this.messageFactory = messageFactory;
  }
  
  JCSMPFactory jcsmpFactory() {
    return ObjectUtils.defaultIfNull(this.getJcsmpFactory(), JCSMPFactory.onlyInstance());
  }
  
  JCSMPFactory getJcsmpFactory() {
    return jcsmpFactory;
  }

  void setJcsmpFactory(JCSMPFactory jcsmpFactory) {
    this.jcsmpFactory = jcsmpFactory;
  }

}
