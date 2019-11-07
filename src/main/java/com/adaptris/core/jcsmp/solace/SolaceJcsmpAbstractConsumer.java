package com.adaptris.core.jcsmp.solace;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumerImp;
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

  private static final permissions DEFAULT_ENDPOINT_PERMISSIONS = permissions.CONSUME;

  private static final accessType DEFAULT_ENDPOINT_ACCESS_TYPE = accessType.NONEXCLUSIVE;

  private static final ackMode DEFAULT_ACKNOWLEDGE_MODE = ackMode.CLIENT;

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
  @InputFieldDefault(value = "NONEXCLUSIVE")
  @InputFieldHint(style="com.adaptris.core.jcsmp.solace.accessType")
  @Pattern(regexp = "NONEXCLUSIVE|EXCLUSIVE")
  private String endpointAccessType;
  
  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = "CLIENT")
  @InputFieldHint(style="com.adaptris.core.jcsmp.solace.ackMode")
  @Pattern(regexp = "CLIENT|AUTO")
  private String acknowledgeMode;
  
  private transient SolaceJcsmpMessageAcker messageAcker;
  
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
    this.setMessageTranslator(new SolaceJcsmpBytesMessageTranslator());
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
      Timer.start("OnReceive", 1000);
      AdaptrisMessage adaptrisMessage = getMessageTranslator().translate(message);
      getMessageAcker().addUnacknowledgedMessage(message, adaptrisMessage.getUniqueId());
      
      retrieveAdaptrisMessageListener().onAdaptrisMessage(adaptrisMessage);
      Timer.stopAndLog("OnReceive");
    } catch (Exception e) {
      log.error("Failed to translate message.", e);
    }
  }

  @Override
  public void prepare() throws CoreException {
    
  }
  
  @Override
  public void start() throws CoreException {
    try {
      this.startReceive();
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException("JCSMP Consumer, failed to start.", e);
    }
  }
  
  @Override
  public void stop() {
    this.getCurrentSession().closeSession();
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
    return ObjectUtils.defaultIfNull(this.getJcsmpFactory(), JCSMPFactory.onlyInstance());
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

  public void setMessageTranslator(SolaceJcsmpMessageTranslator messageTranslator) {
    this.messageTranslator = messageTranslator;
  }

  SolaceJcsmpMessageAcker getMessageAcker() {
    return messageAcker;
  }

  void setMessageAcker(SolaceJcsmpMessageAcker messageAcker) {
    this.messageAcker = messageAcker;
  }

  permissions endpointPermissions() {
    return ObjectUtils.defaultIfNull(permissions.valueOf(this.getEndpointPermissions()), DEFAULT_ENDPOINT_PERMISSIONS);
  }
  
  public String getEndpointPermissions() {
    return endpointPermissions;
  }

  public void setEndpointPermissions(String endpointPermissions) {
    this.endpointPermissions = endpointPermissions;
  }

  accessType endpointAccessType() {
    return ObjectUtils.defaultIfNull(accessType.valueOf(this.getEndpointAccessType()), DEFAULT_ENDPOINT_ACCESS_TYPE);
  }
  
  public String getEndpointAccessType() {
    return endpointAccessType;
  }

  public void setEndpointAccessType(String endpointAccessType) {
    this.endpointAccessType = endpointAccessType;
  }

  ackMode acknowledgeMode() {
    return ObjectUtils.defaultIfNull(ackMode.valueOf(this.getAcknowledgeMode()), DEFAULT_ACKNOWLEDGE_MODE);
  }
  
  public String getAcknowledgeMode() {
    return acknowledgeMode;
  }

  public void setAcknowledgeMode(String acknowledgeMode) {
    this.acknowledgeMode = acknowledgeMode;
  }
}
