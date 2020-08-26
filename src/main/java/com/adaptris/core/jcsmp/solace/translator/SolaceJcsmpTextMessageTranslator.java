package com.adaptris.core.jcsmp.solace.translator;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.TextMessage;
import com.solacesystems.jcsmp.XMLMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * A text message translator specifically for Solace's JCSMP API.
 * </p>
 * <p>
 * See {@link SolaceJcsmpBaseTranslatorImp} for further details.
 * </p>
 * @author Aaron
 * @since 3.11.0
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP text message translator.", tag="solace,jcsmp,translator", since="3.11.0")
@XStreamAlias("solace-jcsmp-text-message-translator")
@DisplayOrder(order = {"messageFactory"})
public class SolaceJcsmpTextMessageTranslator extends SolaceJcsmpBaseTranslatorImp {

  private transient TextMessage textMessage;
  
  public SolaceJcsmpTextMessageTranslator() {
    super();
    textMessage = this.jcsmpFactory().createMessage(TextMessage.class);
  }
  
  @Override
  protected XMLMessage performPayloadTranslation(AdaptrisMessage source) {
    textMessage.reset();
    textMessage.setDeliveryMode(DeliveryMode.valueOf(deliveryMode()));
    textMessage.setText(source.getContent());
    return textMessage;
  }

  @Override
  protected void performPayloadTranslation(XMLMessage source, AdaptrisMessage destination) {
    destination.setContent(((TextMessage) source).getText(), destination.getContentEncoding());
  }

}
