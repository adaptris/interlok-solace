package com.adaptris.core.jcsmp.solace.translator;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.core.AdaptrisMessage;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.XMLContentMessage;
import com.solacesystems.jcsmp.XMLMessage;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * An xml content message translator specifically for Solace's JCSMP API.
 * </p>
 * <p>
 * See {@link SolaceJcsmpBaseTranslatorImp} for further details.
 * </p>
 * @author Aaron
 * @since 3.11.0
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP xml content message translator.", tag="solace,jcsmp,translator", since="3.11.0")
@XStreamAlias("solace-jcsmp-xml-content-message-translator")
@DisplayOrder(order = {"messageFactory"})
public class SolaceJcsmpXmlContentMessageTranslator extends SolaceJcsmpBaseTranslatorImp {

  private transient XMLContentMessage xmlContentMessage;
  
  public SolaceJcsmpXmlContentMessageTranslator() {
    super();
    xmlContentMessage = this.jcsmpFactory().createMessage(XMLContentMessage.class);
  }
  
  @Override
  protected XMLMessage performPayloadTranslation(AdaptrisMessage source) {
    xmlContentMessage.reset();
    xmlContentMessage.setDeliveryMode(DeliveryMode.valueOf(deliveryMode()));
    xmlContentMessage.setXMLContent(source.getContent());
    return xmlContentMessage;
  }

  @Override
  protected void performPayloadTranslation(XMLMessage source, AdaptrisMessage destination) {
    destination.setContent(((XMLContentMessage) source).getXMLContent(), destination.getContentEncoding());
  }

}
