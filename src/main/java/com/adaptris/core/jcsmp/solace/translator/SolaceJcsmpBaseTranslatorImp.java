package com.adaptris.core.jcsmp.solace.translator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.jcsmp.solace.SolaceJcsmpMetadataMapping;
import com.adaptris.validation.constraints.ConfigDeprecated;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.XMLMessage;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Once a message has been consumed from Solace, this translator will generate an {@link AdaptrisMessage} from the 
 * payload and headers of the consumed message.
 * </p>
 * <p>
 * Reverse translation when producing a message to a Solace end-point will create a Solace XMLMessage again from the 
 * payload and metadata of the {@link AdaptrisMessage}
 * </p>
 * <p>
 * The payload mapping is straight forward, but the metadata and header mapping does require a little configuration.
 * Most of the header values on a Solace XMLMessage are strings and in fact we assume every mapping will be a String, 
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
public abstract class SolaceJcsmpBaseTranslatorImp implements SolaceJcsmpMessageTranslator {

private static final String DEFAULT_DELIVERY_MODE = DeliveryMode.PERSISTENT.name();
  
/**
 * Defaults to the standard {@link DefaultMessageFactory}.
 * @param messageFactory
 */
  @NotNull
  @AutoPopulated
  @Getter
  @Setter
  private AdaptrisMessageFactory messageFactory;
  
  /**
   * The mode of delivery; "PERSISTENT / NONPERSISTENT"
   * @param deliveryMode
   */
  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = "PERSISTENT")
  @InputFieldHint(style="com.solacesystems.jcsmp.DeliveryMode")
  @Pattern(regexp = "PERSISTENT|NON_PERSISTENT|DIRECT")
  @Getter
  @Setter
  private String deliveryMode;
  
  /**
   * A list of mappings between Adaptris message metadata and Solace headers.
   * @param mappings
   * @deprecated since 4.3 Use per-message-properties instead.
   */
  @NotNull
  @AutoPopulated
  @Getter
  @Setter
  @AdvancedConfig(rare=true)
  @Deprecated
  @ConfigDeprecated(removalVersion = "5.0", message = "Use per-message-properties instead.", groups = Deprecated.class)
  private List<SolaceJcsmpMetadataMapping> mappings;

  @Getter
  @Setter
  private SolaceJcsmpPerMessageProperties perMessageProperties;
  
  /**
   * Apply per message properties resolution when we produce a JCSMP message.
   */
  @Getter
  @Setter
  private boolean applyPerMessagePropertiesOnProduce;
  
  /**
   * Copy the JCSMP message properties into metadata when we consume a message.
   */
  @Getter
  @Setter
  private boolean applyPerMessagePropertiesOnConsume;
  
  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private transient JCSMPFactory jcsmpFactory;
    
  protected enum HeaderDataType {
    STRING {
      @Override
      void setHeader(XMLMessage message, String headerName, String value) throws Exception {
        getSetterMethodFromHeaderName(message, headerName, String.class).invoke(message, value);
      }

      @Override
      String getHeader(XMLMessage message, String headerName) throws Exception {
        return (String) getGetterMethodFromHeaderName(message, headerName).invoke(message);
      }
    },
    
    INTEGER {
      @Override
      void setHeader(XMLMessage message, String headerName, String value) throws Exception {
        getSetterMethodFromHeaderName(message, headerName, int.class).invoke(message, Integer.parseInt(value));
      }

      @Override
      String getHeader(XMLMessage message, String headerName) throws Exception {
        return Integer.toString((int) getGetterMethodFromHeaderName(message, headerName).invoke(message));
      }
    },
    
    LONG {
      @Override
      void setHeader(XMLMessage message, String headerName, String value) throws Exception {
        getSetterMethodFromHeaderName(message, headerName, long.class).invoke(message, Long.parseLong(value));
      }

      @Override
      String getHeader(XMLMessage message, String headerName) throws Exception {
        return Long.toString((long) getGetterMethodFromHeaderName(message, headerName).invoke(message));
      }
    },
    
    BOOLEAN {
      @Override
      void setHeader(XMLMessage message, String headerName, String value) throws Exception {
        getSetterMethodFromHeaderName(message, headerName, boolean.class).invoke(message, Boolean.parseBoolean(value));
      }

      @Override
      String getHeader(XMLMessage message, String headerName) throws Exception {
        return Boolean.toString((boolean) getGetterMethodFromHeaderName(message, headerName).invoke(message));
      }
    };
    
    abstract void setHeader(XMLMessage message, String headerName, String value) throws Exception;
    
    abstract String getHeader(XMLMessage message, String headerName) throws Exception;
    
    Method getGetterMethodFromHeaderName(XMLMessage message, String headerName) throws Exception {
      return message.getClass().getMethod("get" + headerName);
    }
    
    Method getSetterMethodFromHeaderName(XMLMessage message, String headerName, Class<?> type) throws Exception {
      return message.getClass().getMethod("set" + headerName, type);
    }
  }
  
  public SolaceJcsmpBaseTranslatorImp() {
    this.setMessageFactory(messageFactory);
    this.setPerMessageProperties(new SolaceJcsmpPerMessageProperties());
    this.setMappings(new ArrayList<>());
  } 
  
  @Override
  public AdaptrisMessage translate(XMLMessage message) throws Exception {
    AdaptrisMessage adaptrisMessage = this.messageFactory().newMessage();
    
    performPayloadTranslation(message, adaptrisMessage);
    performMetadataMappings(message, adaptrisMessage);
    if(getApplyPerMessagePropertiesOnConsume())
      getPerMessageProperties().applyPerMessageProperties(adaptrisMessage, message);
    
    return adaptrisMessage;
  }
  
  @Override
  public XMLMessage translate(AdaptrisMessage message) throws Exception {
    XMLMessage solaceMessage = performPayloadTranslation(message);
    performHeaderMappings(message, solaceMessage);
    if(getApplyPerMessagePropertiesOnProduce())
      getPerMessageProperties().applyPerMessageProperties(solaceMessage, message);
    
    return solaceMessage;
  }
  
  protected abstract XMLMessage performPayloadTranslation(AdaptrisMessage source);

  protected abstract void performPayloadTranslation(XMLMessage source, AdaptrisMessage destination);

  protected void performMetadataMappings(XMLMessage message, AdaptrisMessage adaptrisMessage) throws Exception {
    for(SolaceJcsmpMetadataMapping mapping : this.getMappings()) {
      String headerValue = HeaderDataType.valueOf(((String) ObjectUtils.defaultIfNull(mapping.getDataType(), "String")).toUpperCase())
          .getHeader(message, mapping.getHeaderKey());
      
      adaptrisMessage.addMessageHeader(mapping.getMetadataKey(), headerValue);
    }
  }
  
  protected void performHeaderMappings(AdaptrisMessage message, XMLMessage xmlMessage) throws Exception {
    for(SolaceJcsmpMetadataMapping mapping : this.getMappings()) {
      HeaderDataType.valueOf(((String) ObjectUtils.defaultIfNull(mapping.getDataType(), "String")).toUpperCase())
          .setHeader(xmlMessage, mapping.getHeaderKey(), message.getMetadataValue(mapping.getMetadataKey()));
    }
  }
  
  AdaptrisMessageFactory messageFactory() {
    return ObjectUtils.defaultIfNull(this.getMessageFactory(), DefaultMessageFactory.getDefaultInstance());
  }
  
  JCSMPFactory jcsmpFactory() {
    return ObjectUtils.defaultIfNull(this.getJcsmpFactory(), JCSMPFactory.onlyInstance());
  }
  
  String deliveryMode() {
    if(this.getDeliveryMode() != null)
      return this.getDeliveryMode().toUpperCase();
    else
      return DEFAULT_DELIVERY_MODE;
  }

}
