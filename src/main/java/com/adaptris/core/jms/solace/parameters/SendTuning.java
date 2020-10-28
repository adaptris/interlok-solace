package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

@XStreamAlias("solace-send-tuning")
public class SendTuning implements Parameter {
  
  /**
   * This property specifies the duration of the acknowledgement timer in milliseconds.
   */
  @Getter
  @Setter
  private Integer sendAdAckTimerInMillis;
  /**
   * The maximum number of times the API can resend a message if not acknowledged by the appliance, or if a resend is required on reconnection.
   */
  @Getter
  @Setter
  private Integer sendAdMaxResends;
  /**
   * This property specifies the size of the sliding acknowledgement window for non-persistent messages.
   */
  @Getter
  @Setter
  private Integer sendAdWindowSize;
  /**
   * This property is used to specify the socket’s send buffer size in bytes.
   */
  @Getter
  @Setter
  private Integer sendBufferSize;
  
  @Override
  public void apply(SolConnectionFactory cf) {
    cf.setSendADAckTimerInMillis(getSendAdAckTimerInMillis());
    cf.setSendADMaxResends(getSendAdMaxResends());
    cf.setSendADWindowSize(getSendAdWindowSize());
    cf.setSendBufferSize(getSendBufferSize());
  }

}
