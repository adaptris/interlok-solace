package com.adaptris.core.jcsmp.solace;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.TextMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP message translator.", tag="solace,jcsmp,translator")
@XStreamAlias("solace-jcsmp-bytes-message-translator")
@DisplayOrder(order = {"messageFactory"})
public class SolaceJcsmpBytesMessageTranslator implements SolaceJcsmpMessageTranslator {

  private static final DeliveryMode DEFAULT_DELIVERY_MODE = DeliveryMode.PERSISTENT;
  
  @NotNull
  @AutoPopulated
  private AdaptrisMessageFactory messageFactory;
  
  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = "PERSISTENT")
  @InputFieldHint(style="com.solacesystems.jcsmp.DeliveryMode")
  @Pattern(regexp = "PERSISTENT|NON-PERSISTENT|DIRECT")
  private String deliveryMode;

  private transient JCSMPFactory jcsmpFactory;
  
  private transient TextMessage textMessage;
  
  public SolaceJcsmpBytesMessageTranslator() {
    this.setMessageFactory(messageFactory);
    textMessage = this.jcsmpFactory().createMessage(TextMessage.class);
  }
  
  @Override
  public AdaptrisMessage translate(BytesXMLMessage message) throws Exception {
    AdaptrisMessage adaptrisMessage = this.messageFactory().newMessage();
    adaptrisMessage.setPayload(message.getBytes());
    
    return adaptrisMessage;
  }

  @Override
  public BytesXMLMessage translate(AdaptrisMessage message) throws Exception {
//    TextMessage textMessage = this.jcsmpFactory().createMessage(TextMessage.class);
    textMessage.reset();
    textMessage.setDeliveryMode(DeliveryMode.valueOf(deliveryMode()));
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
  
  String deliveryMode() {
    return (String) ObjectUtils.defaultIfNull(this.getDeliveryMode().toUpperCase(), DEFAULT_DELIVERY_MODE);
  }

  public String getDeliveryMode() {
    return deliveryMode;
  }

  public void setDeliveryMode(String deliveryMode) {
    this.deliveryMode = deliveryMode;
  }

}
