package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("receive-tuning")
public class ReceiveTuning implements Parameter {
  private Integer receiveAdAckThreshold;
  private Integer receiveAdAckTimerInMillis;
  private Integer receiveAdWindowSize;
  private Integer receiveBufferSize;
  private Integer subscriberLocalPriority;
  private Integer subscriberNetworkPriority;
  
  @Override
  public void apply(SolConnectionFactory cf) {
    cf.setReceiveAdAckThreshold(getReceiveAdAckThreshold());
    cf.setReceiveADAckTimerInMillis(getReceiveAdAckTimerInMillis());
    cf.setReceiveADWindowSize(getReceiveAdWindowSize());
    cf.setReceiveBufferSize(getReceiveBufferSize());
    cf.setSubscriberLocalPriority(getSubscriberLocalPriority());
    cf.setSubscriberNetworkPriority(getSubscriberNetworkPriority());
  }
  
  public Integer getReceiveAdAckThreshold() {
    return receiveAdAckThreshold;
  }

  /**
   * This property specifies the threshold for sending an acknowledgement to the appliance.
   */
  public void setReceiveAdAckThreshold(Integer receiveAdAckThreshold) {
    this.receiveAdAckThreshold = receiveAdAckThreshold;
  }

  public Integer getReceiveAdAckTimerInMillis() {
    return receiveAdAckTimerInMillis;
  }

  /**
   * This property specifies the duration of the acknowledgement timer.
   */
  public void setReceiveAdAckTimerInMillis(Integer receiveAdAckTimerInMillis) {
    this.receiveAdAckTimerInMillis = receiveAdAckTimerInMillis;
  }

  public Integer getReceiveAdWindowSize() {
    return receiveAdWindowSize;
  }

  /**
   * This property specifies the size of the sliding acknowledgement window.
   */
  public void setReceiveAdWindowSize(Integer receiveAdWindowSize) {
    this.receiveAdWindowSize = receiveAdWindowSize;
  }

  public Integer getReceiveBufferSize() {
    return receiveBufferSize;
  }

  /**
   * This property is used to configure the socket’s receive buffer size in bytes.
   */
  public void setReceiveBufferSize(Integer receiveBufferSize) {
    this.receiveBufferSize = receiveBufferSize;
  }

  public Integer getSubscriberLocalPriority() {
    return subscriberLocalPriority;
  }

  /**
   * This property specifies the priority that a client’s subscriptions have for receiving Deliver-To-One messages published on the Solace appliance that the client is directly connected to.
   */
  public void setSubscriberLocalPriority(Integer subscriberLocalPriority) {
    this.subscriberLocalPriority = subscriberLocalPriority;
  }

  public Integer getSubscriberNetworkPriority() {
    return subscriberNetworkPriority;
  }

  /**
   * This property specifies the priority that a client’s subscriptions have for receiving Deliver-To-One messages published by other Solace appliances in the Solace Messaging Platform.
   */
  public void setSubscriberNetworkPriority(Integer subscriberNetworkPriority) {
    this.subscriberNetworkPriority = subscriberNetworkPriority;
  }

}
