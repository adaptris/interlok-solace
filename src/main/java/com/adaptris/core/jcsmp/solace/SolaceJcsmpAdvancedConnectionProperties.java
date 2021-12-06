package com.adaptris.core.jcsmp.solace;

import static com.solacesystems.jcsmp.JCSMPProperties.ACK_EVENT_MODE;
import static com.solacesystems.jcsmp.JCSMPProperties.APPLICATION_DESCRIPTION;
import static com.solacesystems.jcsmp.JCSMPProperties.CALCULATE_MESSAGE_EXPIRATION;
import static com.solacesystems.jcsmp.JCSMPProperties.CLIENT_CHANNEL_PROPERTIES;
import static com.solacesystems.jcsmp.JCSMPProperties.CLIENT_NAME;
import static com.solacesystems.jcsmp.JCSMPProperties.GD_RECONNECT_FAIL_ACTION;
import static com.solacesystems.jcsmp.JCSMPProperties.GD_RECONNECT_FAIL_ACTION_AUTO_RETRY;
import static com.solacesystems.jcsmp.JCSMPProperties.GD_RECONNECT_FAIL_ACTION_DISCONNECT;
import static com.solacesystems.jcsmp.JCSMPProperties.GENERATE_RCV_TIMESTAMPS;
import static com.solacesystems.jcsmp.JCSMPProperties.GENERATE_SENDER_ID;
import static com.solacesystems.jcsmp.JCSMPProperties.GENERATE_SEND_TIMESTAMPS;
import static com.solacesystems.jcsmp.JCSMPProperties.GENERATE_SEQUENCE_NUMBERS;
import static com.solacesystems.jcsmp.JCSMPProperties.IGNORE_DUPLICATE_SUBSCRIPTION_ERROR;
import static com.solacesystems.jcsmp.JCSMPProperties.IGNORE_SUBSCRIPTION_NOT_FOUND_ERROR;
import static com.solacesystems.jcsmp.JCSMPProperties.KRB_SERVICE_NAME;
import static com.solacesystems.jcsmp.JCSMPProperties.MESSAGE_ACK_MODE;
import static com.solacesystems.jcsmp.JCSMPProperties.MESSAGE_CALLBACK_ON_REACTOR;
import static com.solacesystems.jcsmp.JCSMPProperties.NO_LOCAL;
import static com.solacesystems.jcsmp.JCSMPProperties.PUB_ACK_TIME;
import static com.solacesystems.jcsmp.JCSMPProperties.PUB_ACK_WINDOW_SIZE;
import static com.solacesystems.jcsmp.JCSMPProperties.PUB_USE_INTERMEDIATE_DIRECT_BUF;
import static com.solacesystems.jcsmp.JCSMPProperties.REAPPLY_SUBSCRIPTIONS;
import static com.solacesystems.jcsmp.JCSMPProperties.SUB_ACK_TIME;
import static com.solacesystems.jcsmp.JCSMPProperties.SUB_ACK_WINDOW_SIZE;
import static com.solacesystems.jcsmp.JCSMPProperties.SUB_ACK_WINDOW_THRESHOLD;
import static com.solacesystems.jcsmp.JCSMPProperties.SUPPORTED_ACK_EVENT_MODE_PER_MSG;
import static com.solacesystems.jcsmp.JCSMPProperties.SUPPORTED_ACK_EVENT_MODE_WINDOWED;
import static com.solacesystems.jcsmp.JCSMPProperties.SUPPORTED_MESSAGE_ACK_AUTO;
import static com.solacesystems.jcsmp.JCSMPProperties.SUPPORTED_MESSAGE_ACK_CLIENT;

import javax.validation.constraints.Pattern;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.solacesystems.jcsmp.JCSMPChannelProperties;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Advanced properties specifically for {@Link SolaceJcsmpConnection}
 * </p>
 * @author Aaron
 * @config solace-jcsmp-connection-properties
 */
@AdapterComponent
@ComponentProfile(summary="Advanced connection properties for the JCSMP connection.", tag="connection,solace,jcsmp", since="4.3")
@XStreamAlias("solace-jcsmp-connection-properties")
public class SolaceJcsmpAdvancedConnectionProperties {
  
  /**
   * (Optional) Indicates whether the acknowledgement of receiving a message is done by JCSMP automatically 
   * or by the application explicitly calling XMLMessage.ackMessage(). If message acknowledgement mode should be handled automatically, 
   * then set to "false".  If however you wish for the more explicit client mode, set to "true".
   */
  @Setter
  @Getter
  @InputFieldDefault(value = "false")
  private boolean clientAckMode;
  
  /**
   * (Optional) The client description to report to an appliance. The maximum length is 254 ASCII characters. Setting this property is optional.
   */
  @Setter
  @Getter
  private String applicationDescription;
  
  /**
   * (Optional) A unique client name to use to register to the appliance. If specified, it must be a valid Topic name, and a maximum of 
   * 160 bytes in length when encoded as UTF-8.  
   * If a value of "" is specified, the default (auto-generated) client name is applied.
   */
  @Getter
  @Setter
  private String clientName;
  
  /**
   * (Optional) Indicates whether the client name should be included in the SenderID message header parameter.
   */
  @Setter
  @Getter
  @InputFieldDefault(value = "false")
  private boolean generateSenderId;
  
  /**
   * The size of the sliding subscriber ACK window. The valid range is 1-255.
   */
  @Setter
  @Getter
  @InputFieldDefault(value = "255")
  private int subAckWindowSize;
  
  /**
   * The size of the sliding publisher window for Guaranteed messages. The Guaranteed Message Publish Window Size 
   * property limits the maximum number of messages that can be published before the API must receive an 
   * acknowledgement from the appliance. The valid range is 1-255.
   */
  @Setter
  @Getter
  @InputFieldDefault(value = "1")
  private int pubAckWindowSize;
  
  /**
   * The duration of the subscriber ACK timer (in milliseconds). The valid range is 20-1500.
   */
  @Setter
  @Getter
  @InputFieldDefault(value = "1000")
  private int subAckTime;
  
  /**
   * The duration of the publisher acknowledgement timer (in milliseconds). When a published 
   * message is not acknowledged within the time specified for this timer, the API automatically 
   * retransmits the message. The valid range is 20-60000.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="2000")
  private int pubAckTime;
  
  /**
   * The threshold for sending an acknowledgement, configured as a percentage. 
   * The API sends a transport acknowledgment every N messages where N is calculated as this 
   * percentage of the flow window size if the endpoint's max-delivered-unacked-msgs-per-flow 
   * setting at bind time is greater than or equal to the transport window size. Otherwise, N is 
   * calculated as this percentage of the endpoint's max-delivered-unacked-msgs-per-flow 
   * setting at bind time. The valid range is 1-75.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="60")
  private int subAckWindowThreshold;
  
  /**
   * (Optional) Indicates whether to generate a receive timestamp on incoming messages.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="false")
  private boolean generateRcvTimestamps;
  
  /**
   * (Optional) Indicates whether to generate a send timestamp in outgoing messages.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="false")
  private boolean generateSendTimestamps;
  
  /**
   * (Optional) Indicates whether to generate a sequence number in outgoing messages.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="false")
  private boolean generateSequenceNumbers;
  
  /**
   * (Optional) Indicates whether to calculate message expiration time in 
   * outgoing messages and incoming messages.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="false")
  private boolean calculateMessageExpiration;
  
  /**
   * If enabled, the API maintains a local cache of subscriptions and reapplies them when 
   * the subscriber connection is reestablished. Reapply subscriptions will only apply 
   * direct topic subscriptions unpon a Session reconnect. It will not reapply topic 
   * subscriptions on durable and non-durable endpoints.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="false")
  private boolean reapplySubscriptions;
  
  /**
   * Defaults to true. If enabled, during send operations, the XMLMessageProducer concatenates 
   * all published data to an intermediate direct ByteBuffer in the API, and writes only 
   * that buffer to the socket instead of performing a vectored write(ByteBuffer[]) operation.
   * This can result in higher throughput for certain send operations. It can, however, lead to 
   * performance degradation for some scenarios with large messages or sendMultiple(...) 
   * operations, especially if messages are already using direct ByteBuffers for data storage. 
   * This property should be set to false in those conditions.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="true")
  private boolean pubUseIntermediateDirectBuf = true;
  
  /**
   * If enabled, messages delivered asynchronously to an XMLMessageListener are delivered directly from the I/O 
   * thread instead of a consumer notification thread. An application making use of this setting MUST 
   * return quickly from the onReceive() callback, and MUST NOT call ANY session methods from the I/O thread.
   * The above means that calling any blocking methods from an XMLMessageListener callback is not 
   * supported and may cause an application to deadlock.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="false")
  private boolean messageCallbackOnReactor;
  
  /**
   * On addSubscription(Subscription), addSubscription(Endpoint, Subscription, int), or addSubscriptions(XpeList), ignore errors caused by subscriptions being already present.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="false")
  private boolean ignoreDuplicateSubscriptionError;
  
  /**
   * On removeSubscription(Subscription), removeSubscription(Endpoint, Subscription, int), or removeSubscriptions(XpeList), ignore errors caused by subscriptions not being found.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="false")
  private boolean ignoreSubscriptionNotFoundError;
  
  /**
   * If this property is true, messages published on the session will not be received on 
   * the same session even if the client has a subscription that matches the published topic. 
   * If this restriction is requested and the appliance does not have No Local support, 
   * the session connect will fail.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="false")
  private boolean noLocal;
  
  /**
   * SUPPORTED_ACK_EVENT_MODE_PER_MSG  or  SUPPORTED_ACK_EVENT_MODE_WINDOWED
   * If this property is set to SUPPORTED_ACK_EVENT_MODE_PER_MSG, the message acknowledgement 
   * event acknowledges a single published Guaranteed message; if this property is set to 
   * SUPPORTED_ACK_EVENT_MODE_WINDOWED, the message acknowledgement event acknowledges a 
   * range of published Guaranteed messages.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="SUPPORTED_ACK_EVENT_MODE_PER_MSG")
  @Pattern(regexp="SUPPORTED_ACK_EVENT_MODE_PER_MSG|SUPPORTED_ACK_EVENT_MODE_WINDOWED")
  private String ackEventMode;
  
  /**
   * This property is used to specify the ServiceName portion of the Service Principal Name (SPN) that has a format of ServiceName/ApplianceName@REALM.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="solace")
  private String krbServiceName;
  
  /**
   * GD_RECONNECT_FAIL_ACTION_AUTO_RETRY  or  GD_RECONNECT_FAIL_ACTION_DISCONNECT
   * Defines the behavior when the API is unable to reconnect guaranteed delivery after reconnecting the session. 
   * This may occur if the session is configured with a host list where each Solace router in the host list is unaware 
   * of state on the previous router. It can also occur if the time to reconnect to the same router exceeds the publisher flow timeout on the router.
   */
  @Setter
  @Getter
  @Pattern(regexp = "GD_RECONNECT_FAIL_ACTION_AUTO_RETRY|GD_RECONNECT_FAIL_ACTION_DISCONNECT")
  @InputFieldDefault(value="GD_RECONNECT_FAIL_ACTION_AUTO_RETRY")
  private String gdReconnectFailAction;
  
  /**
   * Timeout value (in ms) for creating an initial connection to the appliance. Valid values are >= 0. 0 means an infinite timeout.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="30000")
  private int channelConnectTimeoutInMillis;
  
  /**
   * Timeout value (in ms) for reading a reply from the appliance. Valid values are > 0.
   * Note: For subscriber control channels, the default is 120000 ms.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="10000")
  private int channelReadTimeoutInMillis;
  
  /**
   * The number of times to attempt and retry a connection to the host appliance (or list of appliances) during initial connection setup. 
   * Valid values are >= -1. Zero means no automatic connection retries (that is, try once and give up). -1 means "retry forever".
   */
  @Setter
  @Getter
  @InputFieldDefault(value="0")
  private int channelConnectRetries;
  
  /**
   * The number of times to attempt to reconnect to the appliance (or list of appliances) after an initial connected session goes down. 
   * Valid values are >= -1. -1 means "retry forever". 0 means no automatic reconnection retries (that is, try once and give up).
   */
  @Setter
  @Getter
  @InputFieldDefault(value="3")
  private int channelReconnectRetries;
  
  /**
   * When using a host list for the HOST property, this property defines how many
   * times to try to connect or reconnect to a single host before moving to the
   * next host in the list. NOTE: This property works in conjunction with the
   * connect and reconnect retries settings; it does not replace them. Valid
   * values are >= -1. 0 means make a single connection attempt (that is, 0
   * retries). -1 means attempt an infinite number of reconnect retries (that is,
   * the API only tries to connect or reconnect to first host listed.)
   */
  
  @Setter
  @Getter
  @InputFieldDefault(value="0")
  private int channelConnectRetriesPerHost;
  
  /**
   * How much time in (MS) to wait between each attempt to connect or reconnect to
   * a host. If a connect or reconnect attempt to host is not successful, the API
   * waits for the amount of time set for reconnectRetryWaitInMillis, and then
   * makes another connect or reconnect attempt. Note: connectRetriesPerHost sets
   * how many connection or reconnection attempts can be made before moving on to
   * the next host in the list. See the HOST for more details on the reconnect
   * logic.
   * 
   * Valid values are 0 - 60000, inclusively.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="3000")
  private int channelReconnectRetryWaitInMillis;
  
  /**
   * The amount of time (in ms) to wait between sending out keep-alive messages.
   * Typically, this feature should be enabled for message receivers. Valid values
   * are >= 50. 0 disables the keepalive function. Note: If using compression on a
   * session, the configured KeepAlive interval should be longer than the maximum
   * time required to compress the largest message likely to be published.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="3000")
  private int channelKeepAliveIntervalInMillis;
  
  /**
   * The maximum number of consecutive keep-alive messages that can be sent without receiving a response before the connection is closed by the API.
   * Valid values are >= 3.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="10")
  private int channelKeepAliveLimit;
  
  /**
   * The size (in bytes) of the send socket buffer.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="65536")
  private int channelSendBuffer;
  
  /**
   * The size (in bytes) of the receive socket buffer.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="65536")
  private int channelReceiveBuffer;
  
  /**
   * Whether to set the TCP_NODELAY option. When enabled, this option disables the Nagle's algorithm.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="true")
  private boolean channelTcpNoDelay;
  
  /**
   * A compressionLevel setting of 1-9 sets the ZLIB compression level to use; a
   * setting of 0 disables compression entirely. From the ZLIB manual: [...] 1
   * gives best speed, 9 gives best compression, 0 gives no compression at all
   * (the input data is simply copied a block at a time).
   * 
   * Note: If using compression on a session, the configured KeepAlive interval
   * should be longer than the maximum time required to compress the largest
   * message likely to be published.
   */
  @Setter
  @Getter
  @InputFieldDefault(value="0")
  private int channelCompressionLevel;
  
  public SolaceJcsmpAdvancedConnectionProperties() {
    setSubAckWindowSize(255);
    setPubAckWindowSize(1);
    setSubAckTime(1000);
    setPubAckTime(2000);
    setSubAckWindowThreshold(60);
    setPubUseIntermediateDirectBuf(true);
    setAckEventMode("SUPPORTED_ACK_EVENT_MODE_PER_MSG");
    setGdReconnectFailAction("GD_RECONNECT_FAIL_ACTION_AUTO_RETRY");
    setChannelConnectTimeoutInMillis(30000);
    setChannelReadTimeoutInMillis(10000);
    setChannelConnectRetries(0);
    setChannelReconnectRetries(3);
    setChannelConnectRetriesPerHost(0);
    setChannelReconnectRetryWaitInMillis(3000);
    setChannelKeepAliveIntervalInMillis(3000);
    setChannelKeepAliveLimit(10);
    setChannelSendBuffer(65536);
    setChannelReceiveBuffer(65536);
    setChannelTcpNoDelay(true);
    setChannelCompressionLevel(0);
  }
  
  public void applyAdvancedProperties(JCSMPProperties properties) {
    JCSMPChannelProperties channelProps = new JCSMPChannelProperties();
    channelProps.setConnectTimeoutInMillis(getChannelConnectTimeoutInMillis());
    channelProps.setReadTimeoutInMillis(getChannelReadTimeoutInMillis());
    channelProps.setConnectRetries(getChannelConnectRetries());
    channelProps.setReconnectRetries(getChannelReconnectRetries());
    channelProps.setConnectRetriesPerHost(getChannelConnectRetriesPerHost());
    channelProps.setReconnectRetryWaitInMillis(getChannelReconnectRetryWaitInMillis());
    channelProps.setKeepAliveIntervalInMillis(getChannelKeepAliveIntervalInMillis());
    channelProps.setKeepAliveLimit(getChannelKeepAliveLimit());
    channelProps.setSendBuffer(getChannelSendBuffer());
    channelProps.setReceiveBuffer(getChannelReceiveBuffer());
    channelProps.setTcpNoDelay(getChannelTcpNoDelay());
    channelProps.setCompressionLevel(getChannelCompressionLevel());
    
    properties.setProperty(CLIENT_CHANNEL_PROPERTIES, channelProps);
    
    properties.setProperty(MESSAGE_ACK_MODE, getClientAckMode() ? SUPPORTED_MESSAGE_ACK_CLIENT : SUPPORTED_MESSAGE_ACK_AUTO);
    
    if(getApplicationDescription() != null)
      properties.setProperty(APPLICATION_DESCRIPTION, getApplicationDescription());
    
    if(getClientName() != null)
      properties.setProperty(CLIENT_NAME, getClientName());
    
    properties.setBooleanProperty(GENERATE_SENDER_ID, getGenerateSenderId());
    
    if(getSubAckWindowSize() > 0)
      properties.setIntegerProperty(SUB_ACK_WINDOW_SIZE, getSubAckWindowSize());
    
    if(getPubAckWindowSize() > 0)
      properties.setIntegerProperty(PUB_ACK_WINDOW_SIZE, getPubAckWindowSize());
    
    if(getSubAckTime() > 0)
      properties.setIntegerProperty(SUB_ACK_TIME, getSubAckTime());
    
    if(getPubAckTime() > 0)
      properties.setIntegerProperty(PUB_ACK_TIME, getPubAckTime());
    
    if(getSubAckWindowThreshold() > 0)
      properties.setIntegerProperty(SUB_ACK_WINDOW_THRESHOLD, getSubAckWindowThreshold());
    
    properties.setBooleanProperty(GENERATE_RCV_TIMESTAMPS, getGenerateRcvTimestamps());
    
    properties.setBooleanProperty(GENERATE_SEND_TIMESTAMPS, getGenerateSendTimestamps());
    
    properties.setBooleanProperty(GENERATE_SEQUENCE_NUMBERS, getGenerateSequenceNumbers());
    
    properties.setBooleanProperty(CALCULATE_MESSAGE_EXPIRATION, getCalculateMessageExpiration());
    
    properties.setBooleanProperty(REAPPLY_SUBSCRIPTIONS, getReapplySubscriptions());
    
    properties.setBooleanProperty(PUB_USE_INTERMEDIATE_DIRECT_BUF, getPubUseIntermediateDirectBuf());
    
    properties.setBooleanProperty(MESSAGE_CALLBACK_ON_REACTOR, getMessageCallbackOnReactor());
    
    properties.setBooleanProperty(IGNORE_DUPLICATE_SUBSCRIPTION_ERROR, getIgnoreDuplicateSubscriptionError());
    
    properties.setBooleanProperty(IGNORE_SUBSCRIPTION_NOT_FOUND_ERROR, getIgnoreSubscriptionNotFoundError());
    
    properties.setBooleanProperty(NO_LOCAL, getNoLocal());
    
    if(getAckEventMode() != null)
      properties.setProperty(ACK_EVENT_MODE, getAckEventMode().equals("SUPPORTED_ACK_EVENT_MODE_PER_MSG") ? SUPPORTED_ACK_EVENT_MODE_PER_MSG : SUPPORTED_ACK_EVENT_MODE_WINDOWED);
    
    if(getKrbServiceName() != null)
      properties.setProperty(KRB_SERVICE_NAME, getKrbServiceName());
    
    if(getGdReconnectFailAction() != null)   
      properties.setProperty(GD_RECONNECT_FAIL_ACTION, getGdReconnectFailAction().equals("GD_RECONNECT_FAIL_ACTION_AUTO_RETRY") ? GD_RECONNECT_FAIL_ACTION_AUTO_RETRY : GD_RECONNECT_FAIL_ACTION_DISCONNECT);
  }
}
