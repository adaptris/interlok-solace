package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("solace-send-tuning")
public class SendTuning implements Parameter {
  private Integer sendAdAckTimerInMillis;
  private Integer sendAdMaxResends;
  private Integer sendAdWindowSize;
  private Integer sendBufferSize;
  
  @Override
  public void apply(SolConnectionFactory cf) {
    cf.setSendADAckTimerInMillis(getSendAdAckTimerInMillis());
    cf.setSendADMaxResends(getSendAdMaxResends());
    cf.setSendADWindowSize(getSendAdWindowSize());
    cf.setSendBufferSize(getSendBufferSize());
  }
  
  public Integer getSendAdAckTimerInMillis() {
    return sendAdAckTimerInMillis;
  }

  /**
   * This property specifies the duration of the acknowledgement timer in milliseconds.
   */
  public void setSendAdAckTimerInMillis(Integer sendAdAckTimerInMillis) {
    this.sendAdAckTimerInMillis = sendAdAckTimerInMillis;
  }

  public Integer getSendAdMaxResends() {
    return sendAdMaxResends;
  }

  /**
   * The maximum number of times the API can resend a message if not acknowledged by the appliance, or if a resend is required on reconnection.
   */
  public void setSendAdMaxResends(Integer sendAdMaxResends) {
    this.sendAdMaxResends = sendAdMaxResends;
  }

  public Integer getSendAdWindowSize() {
    return sendAdWindowSize;
  }

  /**
   * This property specifies the size of the sliding acknowledgement window for non-persistent messages.
   */
  public void setSendAdWindowSize(Integer sendAdWindowSize) {
    this.sendAdWindowSize = sendAdWindowSize;
  }

  public Integer getSendBufferSize() {
    return sendBufferSize;
  }

  /**
   * This property is used to specify the socket’s send buffer size in bytes.
   */
  public void setSendBufferSize(Integer sendBufferSize) {
    this.sendBufferSize = sendBufferSize;
  }

}
