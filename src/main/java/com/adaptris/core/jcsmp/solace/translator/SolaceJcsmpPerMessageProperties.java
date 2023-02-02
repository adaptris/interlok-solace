package com.adaptris.core.jcsmp.solace.translator;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.AdaptrisMessage;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.SDTException;
import com.solacesystems.jcsmp.User_Cos;
import com.solacesystems.jcsmp.XMLMessage;

import lombok.Getter;
import lombok.Setter;

public class SolaceJcsmpPerMessageProperties {
  
  /**
   * Set the ACK Immediately message property.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String ackImmediately;
  
  /**
   * Sets the message ID (a string for an application-specific message identifier).
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String applicationMessageId;
  
  /**
   * Sets the reply field of the message.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String asReplyMessage;
  
  /**
   * Sets the correlation ID.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String correlationId;
  
  /**
   * Sets the correlation key.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String correlationKey;
  
  /**
   * Sets the Class of Service (CoS) value for this message. 1 being the lowest class of service and 3 being the highest.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String classOfService;
  
  /**
   * Sets the delivery mode of the message.  Should be one of the following; DIRECT|NON_PERSISTENT|PERSISTENT
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String deliveryMode;
  
  /**
   * Set the message to be eligible to be moved to a Dead Message Queue.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String dmqEligible;
  
  /**
   * Sets whether the message is eligible for eliding.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String elidingEligible;
  
  /**
   * The UTC time (in milliseconds, from midnight, January 1, 1970 UTC) when the message is supposed to expire.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String expiration;
  
  /**
   * Sets the HTTP content type encoding value for interaction with an HTTP client.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String httpContentEncoding;
  
  /**
   * Sets the HTTP content type value for interaction with an HTTP client.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String httpContentType;
  
  /**
   * A message can optionally have priority set.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String priority;
  
  /**
   * Sets the replyTo destination (queue) for the message.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String replyToQueue;
  
  /**
   * Sets the replyTo destination (topic) for the message.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String replyToTopic;
  
  /**
   * Appends a String to the session's default ReplyTo base topic and creates a ReplyTo Topic Destination.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String replyToSuffix;
  
  /**
   * Sets the Sender ID for the message.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String senderId;
  
  /**
   * Allows the application to set the send timestamp, if so the API will not generate a value.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String senderTimestamp;
  
  /**
   * Sets the sequence number.
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String sequenceNumber;
  
  /**
   * The number of milliseconds before the message is discarded or moved to a Dead Message Queue.
   * 
   */
  @Getter
  @Setter
  @InputFieldHint(expression=true)
  private String timeToLive;
  
  public void applyPerMessageProperties(AdaptrisMessage destMessage, XMLMessage sourceMessage) throws SDTException {
    if((sourceMessage.getApplicationMessageId() != null) && (getApplicationMessageId() != null))
      destMessage.addMetadata(getApplicationMessageId(), sourceMessage.getApplicationMessageId());
    
    destMessage.addMetadata("discardIndication", Boolean.toString(sourceMessage.getDiscardIndication()));
    destMessage.addMetadata("ackMessageId", Long.toString(sourceMessage.getAckMessageId()));
    
    if(sourceMessage.getApplicationMessageType() != null)
      destMessage.addMetadata("applicationMessageType", sourceMessage.getApplicationMessageType());
    
    if(sourceMessage.getCacheRequestId() != null)
      destMessage.addMetadata("cacheRequestId", Long.toString(sourceMessage.getCacheRequestId()));
    destMessage.addMetadata("contentLength", Long.toString(sourceMessage.getContentLength()));
    
    if((sourceMessage.getCorrelationId() != null) && (getCorrelationId() != null))
      destMessage.addMetadata(getCorrelationId(), sourceMessage.getCorrelationId());
    
    if((sourceMessage.getCorrelationKey() != null) && (getCorrelationKey() != null))
      destMessage.addMetadata(getCorrelationKey(), sourceMessage.getCorrelationKey().toString());
    
    if((sourceMessage.getCos() != null) && (getClassOfService() != null))
      destMessage.addMetadata(getClassOfService(), sourceMessage.getCos().equals(User_Cos.USER_COS_1) ? "1" : sourceMessage.getCos().equals(User_Cos.USER_COS_2) ? "2" : "3");
    
    if((sourceMessage.getDeliveryMode() != null) && (getDeliveryMode() != null))
      destMessage.addMetadata(getDeliveryMode(), sourceMessage.getDeliveryMode().name());
    
    if(getAckImmediately() != null)
      destMessage.addMetadata(getAckImmediately(), Boolean.toString(sourceMessage.isAckImmediately()));
    destMessage.addMetadata("cacheMessage", Boolean.toString(sourceMessage.isCacheMessage()));
    if(getElidingEligible() != null)
      destMessage.addMetadata(getElidingEligible(), Boolean.toString(sourceMessage.isElidingEligible()));
    if(getDmqEligible() != null)
      destMessage.addMetadata(getDmqEligible(), Boolean.toString(sourceMessage.isDMQEligible()));
    if(getAsReplyMessage() != null)
      destMessage.addMetadata(getAsReplyMessage(), Boolean.toString(sourceMessage.isReplyMessage()));
    
    destMessage.addMetadata("structuredMessage", Boolean.toString(sourceMessage.isStructuredMsg()));
    destMessage.addMetadata("suspect", Boolean.toString(sourceMessage.isSuspect()));
    
    if(getExpiration() != null)
      destMessage.addMetadata(getExpiration(), Long.toString(sourceMessage.getExpiration()));
    
    if((sourceMessage.getHTTPContentEncoding() != null) && (getHttpContentEncoding() != null))
      destMessage.addMetadata(getHttpContentEncoding(), sourceMessage.getHTTPContentEncoding());
    
    if((sourceMessage.getHTTPContentType() != null) && (getHttpContentType() != null))
      destMessage.addMetadata(getHttpContentType(), sourceMessage.getHTTPContentType());
    
    if(getPriority() != null)
      destMessage.addMetadata(getPriority(), Integer.toString(sourceMessage.getPriority()));
    
    destMessage.addMetadata("receiveTimestamp", Long.toString(sourceMessage.getReceiveTimestamp()));
    destMessage.addMetadata("redelivered", Boolean.toString(sourceMessage.getRedelivered()));
    
    if(sourceMessage.getReplyTo() != null)
      destMessage.addMetadata("replyTo", sourceMessage.getReplyTo().getName());
    
    if((sourceMessage.getReplyToSuffix() != null) && (getReplyToSuffix() != null))
      destMessage.addMetadata(getReplyToSuffix(), sourceMessage.getReplyToSuffix());
    
    if((sourceMessage.getSenderId() != null) && (getSenderId() != null))
      destMessage.addMetadata(getSenderId(), sourceMessage.getSenderId());
    
    if((sourceMessage.getSenderTimestamp() != null) && (getSenderTimestamp() != null))
      destMessage.addMetadata(getSenderTimestamp(), Long.toString(sourceMessage.getSenderTimestamp()));
    
    if((sourceMessage.getSequenceNumber() != null) && (getSequenceNumber() != null))
      destMessage.addMetadata(getSequenceNumber(), Long.toString(sourceMessage.getSequenceNumber()));
    
    if(getTimeToLive() != null)
      destMessage.addMetadata(getTimeToLive(), Long.toString(sourceMessage.getTimeToLive()));
  }
  
  public void applyPerMessageProperties(XMLMessage destMessage, AdaptrisMessage sourceMessage) throws SDTException {
    if(getAckImmediately() != null) 
      destMessage.setAckImmediately(Boolean.parseBoolean(sourceMessage.resolve(getAckImmediately())));
    
    if(getApplicationMessageId() != null)
      destMessage.setApplicationMessageId(sourceMessage.resolve(getApplicationMessageId()));
    
    if(getAsReplyMessage() != null)
      destMessage.setAsReplyMessage(Boolean.parseBoolean(sourceMessage.resolve(getAsReplyMessage())));
    
    if(getCorrelationId() != null)
      destMessage.setCorrelationId(sourceMessage.resolve(getCorrelationId()));
    
    if(getCorrelationKey() != null)
      destMessage.setCorrelationKey(sourceMessage.resolve(getCorrelationKey()));
    
    if(getClassOfService() != null) {
      String resolvedValue = sourceMessage.resolve(getClassOfService());
      destMessage.setCos(resolvedValue.equals("1") ? User_Cos.USER_COS_1 : resolvedValue.equals("2") ? User_Cos.USER_COS_2 : User_Cos.USER_COS_3);
    }
    
    if(getDeliveryMode() != null) {
      String resolvedValue = sourceMessage.resolve(getDeliveryMode());
      destMessage.setDeliveryMode(resolvedValue.equals("DIRECT") ? DeliveryMode.DIRECT : resolvedValue.equals("NON_PERSISTENT") ? DeliveryMode.NON_PERSISTENT : DeliveryMode.PERSISTENT);
    }
    
    if(getDmqEligible() != null) 
      destMessage.setDMQEligible(Boolean.parseBoolean(sourceMessage.resolve(getDmqEligible())));
    
    if(getElidingEligible() != null) 
      destMessage.setElidingEligible(Boolean.parseBoolean(sourceMessage.resolve(getElidingEligible())));
    
    if(getExpiration() != null) 
      destMessage.setExpiration(Long.parseLong(sourceMessage.resolve(getExpiration())));
    
    if(getHttpContentEncoding() != null)
      destMessage.setHTTPContentEncoding(sourceMessage.resolve(getHttpContentEncoding()));
    
    if(getHttpContentType() != null)
      destMessage.setHTTPContentType(sourceMessage.resolve(getHttpContentType()));
    
    if(getPriority() != null)
      destMessage.setPriority(Integer.parseInt(sourceMessage.resolve(getPriority())));
    
    if(getReplyToQueue() != null)
      destMessage.setReplyTo(JCSMPFactory.onlyInstance().createQueue(sourceMessage.resolve(getReplyToQueue())));
    
    if(getReplyToTopic() != null)
      destMessage.setReplyTo(JCSMPFactory.onlyInstance().createTopic(sourceMessage.resolve(getReplyToTopic())));
    
    if(getReplyToSuffix() != null)
      destMessage.setReplyToSuffix(sourceMessage.resolve(getReplyToSuffix()));
    
    if(getSenderId() != null)
      destMessage.setSenderId(sourceMessage.resolve(getSenderId()));
    
    if(getSenderTimestamp() != null)
      destMessage.setSenderTimestamp(Long.parseLong(sourceMessage.resolve(getSenderTimestamp())));
    
    if(getSequenceNumber() != null)
      destMessage.setSequenceNumber(Long.parseLong(sourceMessage.resolve(getSequenceNumber())));
    
    if(getTimeToLive() != null)
      destMessage.setTimeToLive(Long.parseLong(sourceMessage.resolve(getTimeToLive())));
    
  }
  
}
