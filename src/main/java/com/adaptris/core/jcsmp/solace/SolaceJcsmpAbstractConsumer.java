package com.adaptris.core.jcsmp.solace;

import java.util.function.Consumer;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumerImp;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.jcsmp.solace.translator.SolaceJcsmpMessageTranslator;
import com.adaptris.core.jcsmp.solace.translator.SolaceJcsmpTextMessageTranslator;
import com.adaptris.core.util.ExceptionHelper;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.Queue;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class SolaceJcsmpAbstractConsumer  extends AdaptrisMessageConsumerImp implements SolaceJcsmpReceiverStarter {

  private static final String DEFAULT_ENDPOINT_PERMISSIONS = "CONSUME";

  private static final String DEFAULT_ENDPOINT_ACCESS_TYPE = "EXCLUSIVE";

  private static final String DEFAULT_ACKNOWLEDGE_MODE = "CLIENT";

  /**
   * The message translator is responsible for translating the Solace JCSMP message object
   * into an {@link AdaptrisMessage} and the reverse.  The translator will typically handle the payload and the headers/metadata.
   * @param messageTranslator
   */
  @NotNull
  @AutoPopulated
  @Getter
  @Setter
  private SolaceJcsmpMessageTranslator messageTranslator;
  
  /**
   * <p>"CONSUME" / "DELETE" / "READ_ONLY" / "NONE" / "MODIFY_TOPIC""</p>
   * <p>This must match your end-point permissions on the Solace queue/topic.</p>
   * @param endpointPermissions
   */
  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = "CONSUME")
  @InputFieldHint(style="com.adaptris.core.jcsmp.solace.permissions")
  @Pattern(regexp = "CONSUME|DELETE|READ_ONLY|NONE|MODIFY_TOPIC")
  @Getter
  @Setter
  private String endpointPermissions;
  
  /**
   * <p>"EXCLUSIVE" / "NONEXCLUSIVE"</p>
   * <p>This must match your end-point configuration on the Solace queue/topic.</p>
   * @param endpointAccessType
   */
  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = "EXCLUSIVE")
  @InputFieldHint(style="com.adaptris.core.jcsmp.solace.accessType")
  @Pattern(regexp = "NONEXCLUSIVE|EXCLUSIVE")
  @Getter
  @Setter
  private String endpointAccessType;
  
  /**
   * <p>"CLIENT" / "AUTO"</p>
   * <p>Client acknowledge mode means Interlok will handle the Acknowledgements after the workflow has finished or the producer
   * gives us a successful async callback.</p>
   * @param acknowledgeMode
   */
  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = "CLIENT")
  @InputFieldHint(style="com.adaptris.core.jcsmp.solace.ackMode")
  @Pattern(regexp = "CLIENT|AUTO")
  @Getter
  @Setter
  private String acknowledgeMode;

  /**
   * If set to 'true' will commit or rollback the consumed message upon success or failure during processing.
   */
  @AdvancedConfig(rare=true)
  @AutoPopulated
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean transacted;
  
  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private transient SolaceJcsmpSessionHelper sessionHelper;
  
  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private transient JCSMPFactory jcsmpFactory;
  
  public enum permissions {
    CONSUME {
      @Override
      int value() { return EndpointProperties.PERMISSION_CONSUME; }
    },
    DELETE {
      @Override
      int value() { return EndpointProperties.PERMISSION_DELETE; }
    }, 
    READ_ONLY {
      @Override
      int value() { return EndpointProperties.PERMISSION_READ_ONLY; }
    },
    NONE {
      @Override
      int value() { return EndpointProperties.PERMISSION_NONE; }
    },
    MODIFY_TOPIC {
      @Override
      int value() { return EndpointProperties.PERMISSION_MODIFY_TOPIC; }
    };
    abstract int value();
  }
  
  public enum accessType {
    EXCLUSIVE {
      @Override
      int value() { return EndpointProperties.ACCESSTYPE_EXCLUSIVE; }
    },
    NONEXCLUSIVE {
      @Override
      int value() { return EndpointProperties.ACCESSTYPE_NONEXCLUSIVE; }
    };
    abstract int value();
  }
  
  public enum ackMode {
    CLIENT {
      @Override
      String value() { return JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT; }
    },
    AUTO {
      @Override
      String value() { return JCSMPProperties.SUPPORTED_MESSAGE_ACK_AUTO; }
    };
    abstract String value();
  }
  
  public SolaceJcsmpAbstractConsumer() {
    setMessageTranslator(new SolaceJcsmpTextMessageTranslator());
    setSessionHelper(new SolaceJcsmpSessionHelper());
  }
  
  @Override
  public void onException(JCSMPException exception) {
    // Assumes a connection error handler...
    log.error("Exception received from the JCSMP consumer, firing connection error handler.", exception);
    this.retrieveConnection(SolaceJcsmpConnection.class).getConnectionErrorHandler().handleConnectionException();
  }

  @Override
  public void onReceive(BytesXMLMessage message) {
    try {
      AdaptrisMessage adaptrisMessage = getMessageTranslator().translate(message);
      
      Consumer<AdaptrisMessage> successCallback = adpMessage -> {
        if(acknowledgeMode().equals(ackMode.CLIENT)) {
          getSessionHelper().commit(message);
        }
      };
      
      Consumer<AdaptrisMessage> failureCallback = adpMessage -> {
        log.error("Message failed.  Will not acknowledge.", adaptrisMessage.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE));
        getSessionHelper().rollback();
      };
      
      retrieveAdaptrisMessageListener().onAdaptrisMessage(adaptrisMessage, successCallback, failureCallback);
    } catch (Exception e) {
      log.error("Failed to translate message.", e);
    }
  }
  
  @Override
  public void init() throws CoreException {
    getSessionHelper().setConnection(retrieveConnection(SolaceJcsmpConnection.class));
    getSessionHelper().setTransacted(transacted());
  }
  
  @Override
  public void start() throws CoreException {
    try {
      startReceive();
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException("JCSMP Consumer, failed to start.", e);
    }
  }
  
  @Override
  public void stop() {
    getSessionHelper().close();
  }
  
  protected ConsumerFlowProperties createConsumerFlowProperties(Queue queue) {
    final ConsumerFlowProperties flowProps = new ConsumerFlowProperties();
    flowProps.setEndpoint(queue);
    if(transacted())
      flowProps.setStartState(true);
    else
      flowProps.setAckMode(acknowledgeMode().value());
    
    return flowProps;
  }
  
  protected EndpointProperties createEndpointProperties() {
    final EndpointProperties endpointProps = new EndpointProperties();
    // set queue permissions to "consume" and access-type to "exclusive"
    endpointProps.setPermission(endpointPermissions().value());
    endpointProps.setAccessType(endpointAccessType().value());
    
    return endpointProps;
  }

  @Override
  public abstract void startReceive() throws Exception;

  JCSMPFactory jcsmpFactory() {
    return ObjectUtils.defaultIfNull(getJcsmpFactory(), JCSMPFactory.onlyInstance());
  }

  permissions endpointPermissions() {
    return permissions.valueOf(ObjectUtils.defaultIfNull(getEndpointPermissions(), DEFAULT_ENDPOINT_PERMISSIONS));
  }
  
  accessType endpointAccessType() {
    return accessType.valueOf(ObjectUtils.defaultIfNull(getEndpointAccessType(), DEFAULT_ENDPOINT_ACCESS_TYPE));
  }
  
  ackMode acknowledgeMode() {
    return ackMode.valueOf(ObjectUtils.defaultIfNull(getAcknowledgeMode(), DEFAULT_ACKNOWLEDGE_MODE));
  }

  boolean transacted() {
    return ObjectUtils.defaultIfNull(getTransacted(), false);
  }

}
