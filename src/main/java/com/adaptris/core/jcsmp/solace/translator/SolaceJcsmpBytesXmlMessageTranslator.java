package com.adaptris.core.jcsmp.solace.translator;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.XMLMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * A bytes xml message translator specifically for Solace's JCSMP API.
 * </p>
 * <p>
 * See {@link SolaceJcsmpBaseTranslatorImp} for further details.
 * </p>
 * @author Aaron
 * @since 3.9.3
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP bytes xml message translator.", tag="solace,jcsmp,translator", since="3.9.3")
@XStreamAlias("solace-jcsmp-bytes-xml-message-translator")
@DisplayOrder(order = {"messageFactory"})
public class SolaceJcsmpBytesXmlMessageTranslator extends SolaceJcsmpBaseTranslatorImp {

private transient BytesXMLMessage bytesXmlMessage;
  
  public SolaceJcsmpBytesXmlMessageTranslator() {
    super();
    bytesXmlMessage = this.jcsmpFactory().createMessage(BytesXMLMessage.class);
  }

  @Override
  protected XMLMessage performPayloadTranslation(AdaptrisMessage source) {
    bytesXmlMessage.reset();
    bytesXmlMessage.setDeliveryMode(DeliveryMode.valueOf(deliveryMode()));
    bytesXmlMessage.clearContent();
    bytesXmlMessage.writeBytes(source.getPayload());
    return bytesXmlMessage;
  }

  @Override
  protected void performPayloadTranslation(XMLMessage source, AdaptrisMessage destination) {
    byte[] contents = new byte[source.getContentLength()] ;
    ((BytesXMLMessage) source).readContentBytes(0, contents, 0, source.getContentLength());
    
    destination.setPayload(contents);
  }

}
