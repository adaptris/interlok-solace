package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

@XStreamAlias("solace-receive-tuning")
public class ReceiveTuning implements Parameter {
  
  /**
   * This property specifies the threshold for sending an acknowledgement to the appliance.
   */
  @Getter
  @Setter
  private Integer receiveAdAckThreshold;
  /**
   * This property specifies the duration of the acknowledgement timer.
   */
  @Getter
  @Setter
  private Integer receiveAdAckTimerInMillis;
  /**
   * This property specifies the size of the sliding acknowledgement window.
   */
  @Getter
  @Setter
  private Integer receiveAdWindowSize;
  /**
   * This property is used to configure the socket’s receive buffer size in bytes.
   */
  @Getter
  @Setter
  private Integer receiveBufferSize;
  /**
   * This property specifies the priority that a client’s subscriptions have for receiving Deliver-To-One messages published on the Solace appliance that the client is directly connected to.
   */
  @Getter
  @Setter
  private Integer subscriberLocalPriority;
  /**
   * This property specifies the priority that a client’s subscriptions have for receiving Deliver-To-One messages published by other Solace appliances in the Solace Messaging Platform.
   */
  @Getter
  @Setter
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

}
