package com.adaptris.core.jcsmp.solace.translator;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AffectsMetadata;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.jcsmp.solace.SolaceJcsmpMetadataMapping;
import com.adaptris.core.metadata.MetadataFilter;
import com.adaptris.core.metadata.NoOpMetadataFilter;
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
 * In the advanced settings you can specify metadata keys against each Solace message property; when consuming we will copy the value
 * from the Solace message into the specified metadata key and when producing we will source the value for the
 * Solace header from the specified metadata key.
 * </p>
 * <p>
 * User data is slightly different however, when consuming we will copy all Solace User data into metadata, when producing 
 * you will set a metadata filter where we will copy metadata keys and values into the outgoing Solace message User data.
 * </p>
 * @author aaron
 * @config solace-jcsmp-bytes-message-translator
 *
 */
public abstract class SolaceJcsmpBaseTranslatorImp implements SolaceJcsmpMessageTranslator {

  private static final String DEFAULT_DELIVERY_MODE = DeliveryMode.PERSISTENT.name();

  private static final MetadataFilter DEFAULT_FILTER = new NoOpMetadataFilter();
  
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

  @Getter
  @Setter
  @AdvancedConfig(rare=true)
  private SolaceJcsmpPerMessageProperties perMessageProperties;
  
  /**
   * Apply per message properties resolution when we produce a JCSMP message.
   */
  @Getter
  @Setter
  @AdvancedConfig(rare=true)
  private boolean applyPerMessagePropertiesOnProduce;
  
  /**
   * Copy the JCSMP message properties into metadata when we consume a message.
   */
  @Getter
  @Setter
  @AdvancedConfig(rare=true)
  private boolean applyPerMessagePropertiesOnConsume;
  
  @Getter
  @Setter
  @AdvancedConfig(rare=true)
  private List<SolaceJcsmpUserDataTypeMapping> typeMappings;
  
  @Valid
  @AutoPopulated
  @AffectsMetadata
  @InputFieldDefault(value = "no-op-metadata-filter")
  @Getter
  @Setter
  private MetadataFilter metadataFilter;
  
  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private transient SolaceJcsmpUserDataTranslator userDataTranslator;
  
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
    this.setUserDataTranslator(new SolaceJcsmpUserDataTranslator());
  } 
  
  @Override
  public AdaptrisMessage translate(XMLMessage message) throws Exception {
    AdaptrisMessage adaptrisMessage = this.messageFactory().newMessage();
    
    performPayloadTranslation(message, adaptrisMessage);
    if(getApplyPerMessagePropertiesOnConsume())
      getPerMessageProperties().applyPerMessageProperties(adaptrisMessage, message);
    getUserDataTranslator().translate(message, adaptrisMessage);
    
    return adaptrisMessage;
  }
  
  @Override
  public XMLMessage translate(AdaptrisMessage message) throws Exception {
    XMLMessage solaceMessage = performPayloadTranslation(message);
    if(getApplyPerMessagePropertiesOnProduce())
      getPerMessageProperties().applyPerMessageProperties(solaceMessage, message);
    getUserDataTranslator().translate(message, solaceMessage, metadataFilter(), getTypeMappings());
    
    return solaceMessage;
  }
  
  protected abstract XMLMessage performPayloadTranslation(AdaptrisMessage source);

  protected abstract void performPayloadTranslation(XMLMessage source, AdaptrisMessage destination);
  
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
  
  public MetadataFilter metadataFilter() {
    return ObjectUtils.defaultIfNull(getMetadataFilter(), DEFAULT_FILTER);
  }

}
