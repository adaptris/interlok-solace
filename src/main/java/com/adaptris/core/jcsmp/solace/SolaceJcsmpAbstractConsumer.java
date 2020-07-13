package com.adaptris.core.jcsmp.solace;

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
import com.adaptris.core.jcsmp.solace.util.Timer;
import com.adaptris.core.util.ExceptionHelper;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.ConsumerFlowProperties;
import com.solacesystems.jcsmp.EndpointProperties;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;

public abstract class SolaceJcsmpAbstractConsumer  extends AdaptrisMessageConsumerImp implements SolaceJcsmpReceiverStarter {

  private static final String DEFAULT_ENDPOINT_PERMISSIONS = "CONSUME";

  private static final String DEFAULT_ENDPOINT_ACCESS_TYPE = "EXCLUSIVE";

  private static final String DEFAULT_ACKNOWLEDGE_MODE = "CLIENT";
  
  @AdvancedConfig(rare=true)
  @InputFieldDefault(value = "false")
  private Boolean traceLogTimings;

  @NotNull
  @AutoPopulated
  private SolaceJcsmpMessageTranslator messageTranslator;
  
  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = "CONSUME")
  @InputFieldHint(style="com.adaptris.core.jcsmp.solace.permissions")
  @Pattern(regexp = "CONSUME|DELETE|READ_ONLY|NONE|MODIFY_TOPIC")
  private String endpointPermissions;
  
  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = "EXCLUSIVE")
  @InputFieldHint(style="com.adaptris.core.jcsmp.solace.accessType")
  @Pattern(regexp = "NONEXCLUSIVE|EXCLUSIVE")
  private String endpointAccessType;
  
  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = "CLIENT")
  @InputFieldHint(style="com.adaptris.core.jcsmp.solace.ackMode")
  @Pattern(regexp = "CLIENT|AUTO")
  private String acknowledgeMode;
    
  private transient JCSMPFactory jcsmpFactory;
  
  private transient JCSMPSession currentSession;
  
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
    setMessageTranslator(new SolaceJcsmpBytesMessageTranslator());
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
      Timer.start("OnReceive", 100);
      Timer.start("OnReceive", "TranslateMessage", 100);
      AdaptrisMessage adaptrisMessage = getMessageTranslator().translate(message);
      Timer.stop("OnReceive", "TranslateMessage");
      
      Timer.start("OnReceive", "ProcessMessage", 100);
      retrieveAdaptrisMessageListener().onAdaptrisMessage(adaptrisMessage);
      Timer.stop("OnReceive", "ProcessMessage");
      if(acknowledgeMode().equals(ackMode.CLIENT)) {
        if(!adaptrisMessage.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION)) {
          Timer.start("OnReceive", "AckMessage", 100);
          message.ackMessage();
          Timer.stop("OnReceive", "AckMessage");
        } else {
          log.error("Message failed.  Will not acknowledge.", adaptrisMessage.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION_CAUSE));
        }
      }
      Timer.stop("OnReceive");
      if(traceLogTimings())
        Timer.log("OnReceive");
    } catch (Exception e) {
      log.error("Failed to translate message.", e);
    }
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
    getCurrentSession().closeSession();
  }
  
  protected ConsumerFlowProperties createConsumerFlowProperties(Queue queue) {
    final ConsumerFlowProperties flowProps = new ConsumerFlowProperties();
    flowProps.setEndpoint(queue);
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
  
  JCSMPFactory getJcsmpFactory() {
    return jcsmpFactory;
  }

  void setJcsmpFactory(JCSMPFactory jcsmpFactory) {
    this.jcsmpFactory = jcsmpFactory;
  }

  JCSMPSession getCurrentSession() {
    return currentSession;
  }

  void setCurrentSession(JCSMPSession currentSession) {
    this.currentSession = currentSession;
  }

  public SolaceJcsmpMessageTranslator getMessageTranslator() {
    return messageTranslator;
  }

  /**
   * The message translator is responsible for translating the Solace JCSMP message object
   * into an {@link AdaptrisMessage} and the reverse.  The translator will typically handle the payload and the headers/metadata.
   * @param messageTranslator
   */
  public void setMessageTranslator(SolaceJcsmpMessageTranslator messageTranslator) {
    this.messageTranslator = messageTranslator;
  }

  permissions endpointPermissions() {
    return permissions.valueOf(ObjectUtils.defaultIfNull(getEndpointPermissions(), DEFAULT_ENDPOINT_PERMISSIONS));
  }
  
  public String getEndpointPermissions() {
    return endpointPermissions;
  }

  /**
   * <p>"CONSUME" / "DELETE" / "READ_ONLY" / "NONE" / "MODIFY_TOPIC""</p>
   * <p>This must match your end-point permissions on the Solace queue/topic.</p>
   * @param endpointPermissions
   */
  public void setEndpointPermissions(String endpointPermissions) {
    this.endpointPermissions = endpointPermissions;
  }

  accessType endpointAccessType() {
    return accessType.valueOf(ObjectUtils.defaultIfNull(getEndpointAccessType(), DEFAULT_ENDPOINT_ACCESS_TYPE));
  }
  
  public String getEndpointAccessType() {
    return endpointAccessType;
  }

  /**
   * <p>"EXCLUSIVE" / "NONEXCLUSIVE"</p>
   * <p>This must match your end-point configuration on the Solace queue/topic.</p>
   * @param endpointAccessType
   */
  public void setEndpointAccessType(String endpointAccessType) {
    this.endpointAccessType = endpointAccessType;
  }

  ackMode acknowledgeMode() {
    return ackMode.valueOf(ObjectUtils.defaultIfNull(getAcknowledgeMode(), DEFAULT_ACKNOWLEDGE_MODE));
  }
  
  public String getAcknowledgeMode() {
    return acknowledgeMode;
  }

  /**
   * <p>"CLIENT" / "AUTO"</p>
   * <p>Client acknowledge mode means Interlok will handle the Acknowledgements after the workflow has finished or the producer
   * gives us a successful async callback.</p>
   * @param acknowledgeMode
   */
  public void setAcknowledgeMode(String acknowledgeMode) {
    this.acknowledgeMode = acknowledgeMode;
  }

  public Boolean getTraceLogTimings() {
    return traceLogTimings;
  }

  /**
   * For debugging purposes you may want to see trace logging (in the interlok logs) of the steps
   * this consume will go through to consume, translate and process the incoming messages.  The default value is false.
   * @param traceLogTimings
   */
  public void setTraceLogTimings(Boolean traceLogTimings) {
    this.traceLogTimings = traceLogTimings;
  }
  
  boolean traceLogTimings() {
    return ObjectUtils.defaultIfNull(getTraceLogTimings(), false);
  }
}
