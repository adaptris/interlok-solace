package com.adaptris.core.jcsmp.solace;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

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

/**
 * <p>
 * Once a message has been consumed from Solace, this translator will generate an {@link AdaptrisMessage} from the 
 * payload and headers of the consumed message.
 * </p>
 * <p>
 * Reverse translation when producing a message to a Solace end-point will create a Solace BytesXmlMessage again from the 
 * payload and metadata of the {@link AdaptrisMessage}
 * </p>
 * <p>
 * The payload mapping is straight forward, but the metadata and header mapping does require a little configuration.
 * Most of the header values on a Solace BytesXmlMessage are strings and in fact we assume every mapping will be a String, 
 * unless you configure the data-type for any mapping to be anything else.
 * </p>
 * <p>
 * An example of the XML configuration for a producer (therefore translating the AdaptrisMessage to a Solace message) with a simple mapping might be;
 * <pre>
 *   <message-translator class="solace-jcsmp-bytes-message-translator">
 *     <delivery-mode>PERSISTENT</delivery-mode>
 *     <mappings>
 *       <solace-jcsmp-metadata-mapping>
 *         <header-key>MessageId</header-key>
 *         <metadata-key>message-id</metadata-key>
 *       <solace-jcsmp-metadata-mapping>
 *       <solace-jcsmp-metadata-mapping>
 *         <header-key>Priority</header-key>
 *         <metadata-key>message-priority</metadata-key>
 *         <data-type>Integer</data-type>
 *       <solace-jcsmp-metadata-mapping>
 *     </mappings>
 *   </message-translator>
 * </pre>
 * The above example will use the AdaptrisMessage metadata value from the key named "message-id" call the setter method
 * "setMessageId()" on the Solace message. 
 * <br />
 * The second mapping will use the AdaptrisMessage metadata value, turn it into an integer value and call the setter "setPriority()"
 * on the Solace message.
 * </p>
 * @author aaron
 * @config solace-jcsmp-bytes-message-translator
 *
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP message translator.", tag="solace,jcsmp,translator", since="3.9.3")
@XStreamAlias("solace-jcsmp-bytes-message-translator")
@DisplayOrder(order = {"messageFactory"})
public class SolaceJcsmpBytesMessageTranslator implements SolaceJcsmpMessageTranslator {

  private static final String DEFAULT_DELIVERY_MODE = DeliveryMode.PERSISTENT.name();
  
  @NotNull
  @AutoPopulated
  private AdaptrisMessageFactory messageFactory;
  
  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = "PERSISTENT")
  @InputFieldHint(style="com.solacesystems.jcsmp.DeliveryMode")
  @Pattern(regexp = "PERSISTENT|NON_PERSISTENT|DIRECT")
  private String deliveryMode;
  
  @NotNull
  @AutoPopulated
  private List<SolaceJcsmpMetadataMapping> mappings;

  private transient JCSMPFactory jcsmpFactory;
  
  private transient TextMessage textMessage;
  
  private enum HeaderDataType {
    STRING {
      @Override
      void setHeader(BytesXMLMessage message, String headerName, String value) throws Exception {
        getSetterMethodFromHeaderName(message, headerName, String.class).invoke(message, value);
      }

      @Override
      String getHeader(BytesXMLMessage message, String headerName) throws Exception {
        return (String) getGetterMethodFromHeaderName(message, headerName).invoke(message);
      }
    },
    
    INTEGER {
      @Override
      void setHeader(BytesXMLMessage message, String headerName, String value) throws Exception {
        getSetterMethodFromHeaderName(message, headerName, int.class).invoke(message, Integer.parseInt(value));
      }

      @Override
      String getHeader(BytesXMLMessage message, String headerName) throws Exception {
        return Integer.toString((int) getGetterMethodFromHeaderName(message, headerName).invoke(message));
      }
    },
    
    LONG {
      @Override
      void setHeader(BytesXMLMessage message, String headerName, String value) throws Exception {
        getSetterMethodFromHeaderName(message, headerName, long.class).invoke(message, Long.parseLong(value));
      }

      @Override
      String getHeader(BytesXMLMessage message, String headerName) throws Exception {
        return Long.toString((long) getGetterMethodFromHeaderName(message, headerName).invoke(message));
      }
    },
    
    BOOLEAN {
      @Override
      void setHeader(BytesXMLMessage message, String headerName, String value) throws Exception {
        getSetterMethodFromHeaderName(message, headerName, boolean.class).invoke(message, Boolean.parseBoolean(value));
      }

      @Override
      String getHeader(BytesXMLMessage message, String headerName) throws Exception {
        return Boolean.toString((boolean) getGetterMethodFromHeaderName(message, headerName).invoke(message));
      }
    };
    
    abstract void setHeader(BytesXMLMessage message, String headerName, String value) throws Exception;
    
    abstract String getHeader(BytesXMLMessage message, String headerName) throws Exception;
    
    Method getGetterMethodFromHeaderName(BytesXMLMessage message, String headerName) throws Exception {
      return message.getClass().getMethod("get" + headerName);
    }
    
    Method getSetterMethodFromHeaderName(BytesXMLMessage message, String headerName, Class<?> type) throws Exception {
      return message.getClass().getMethod("set" + headerName, type);
    }
  }
  
  public SolaceJcsmpBytesMessageTranslator() {
    this.setMessageFactory(messageFactory);
    this.setMappings(new ArrayList<>());
    textMessage = this.jcsmpFactory().createMessage(TextMessage.class);
  }
  
  @Override
  public AdaptrisMessage translate(BytesXMLMessage message) throws Exception {
    AdaptrisMessage adaptrisMessage = this.messageFactory().newMessage();
    adaptrisMessage.setPayload(message.getBytes());
    
    for(SolaceJcsmpMetadataMapping mapping : this.getMappings()) {
      String headerValue = HeaderDataType.valueOf(((String) ObjectUtils.defaultIfNull(mapping.getDataType(), "String")).toUpperCase())
          .getHeader(message, mapping.getHeaderKey());
      
      adaptrisMessage.addMessageHeader(mapping.getMetadataKey(), headerValue);
    }
    
    return adaptrisMessage;
  }

  @Override
  public BytesXMLMessage translate(AdaptrisMessage message) throws Exception {
    textMessage.reset();
    textMessage.setDeliveryMode(DeliveryMode.valueOf(deliveryMode()));
    textMessage.setText(message.getContent());
        
    for(SolaceJcsmpMetadataMapping mapping : this.getMappings()) {
      HeaderDataType.valueOf(((String) ObjectUtils.defaultIfNull(mapping.getDataType(), "String")).toUpperCase())
          .setHeader(textMessage, mapping.getHeaderKey(), message.getMetadataValue(mapping.getMetadataKey()));
    }
    
    return textMessage;
  }

  AdaptrisMessageFactory messageFactory() {
    return ObjectUtils.defaultIfNull(this.getMessageFactory(), DefaultMessageFactory.getDefaultInstance());
  }
  
  public AdaptrisMessageFactory getMessageFactory() {
    return messageFactory;
  }

  /**
   * Defaults to the standard {@link DefaultMessageFactory}.
   * @param messageFactory
   */
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
    if(this.getDeliveryMode() != null)
      return this.getDeliveryMode().toUpperCase();
    else
      return DEFAULT_DELIVERY_MODE;
  }

  public String getDeliveryMode() {
    return deliveryMode;
  }

  /**
   * The mode of delivery; "PERSISTENT / NONPERSISTENT"
   * @param deliveryMode
   */
  public void setDeliveryMode(String deliveryMode) {
    this.deliveryMode = deliveryMode;
  }

  public List<SolaceJcsmpMetadataMapping> getMappings() {
    return mappings;
  }

  /**
   * A list of mappings between Adaptris message metadata and Solace headers.
   * @param mappings
   */
  public void setMappings(List<SolaceJcsmpMetadataMapping> mappings) {
    this.mappings = mappings;
  }

}
