package com.adaptris.core.jcsmp.solace.translator;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.solacesystems.jcsmp.BytesMessage;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.XMLMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * A bytes message translator specifically for Solace's JCSMP API.
 * </p>
 * <p>
 * See {@link SolaceJcsmpBaseTranslatorImp} for further details.
 * </p>
 * @author Aaron
 * @since 3.11.0
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP bytes message translator.", tag="solace,jcsmp,translator", since="3.11.0")
@XStreamAlias("solace-jcsmp-bytes-message-translator")
@DisplayOrder(order = {"messageFactory"})
public class SolaceJcsmpBytesMessageTranslator extends SolaceJcsmpBaseTranslatorImp {
  
  private transient BytesMessage bytesMessage;
  
  public SolaceJcsmpBytesMessageTranslator() {
    super();
    bytesMessage = this.jcsmpFactory().createMessage(BytesMessage.class);
  }

  @Override
  protected XMLMessage performPayloadTranslation(AdaptrisMessage source) {
    bytesMessage.reset();
    bytesMessage.setDeliveryMode(DeliveryMode.valueOf(deliveryMode()));
    bytesMessage.setData(source.getPayload());
    return bytesMessage;
  }

  @Override
  protected void performPayloadTranslation(XMLMessage source, AdaptrisMessage destination) {
    destination.setPayload(((BytesMessage) source).getData());
  }

}
