package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

@XStreamAlias("solace-send-flags")
public class SendFlags implements Parameter {
  
  /**
   * This property specifies whether sent messages are eligible for the dead message queue.
   */
  @Getter
  @Setter
  private Boolean dmqEligible;
  /**
   * This property specifies whether sent messages are marked as eligible for eliding.
   */
  @Getter
  @Setter
  private Boolean elidingEligible;
  /**
   * This property is used to Indicates whether the client name should be included in the SenderID message header parameter.
   */
  @Getter
  @Setter
  private Boolean generateSenderID;
  /**
   * This property specifies whether sent text messages have an XML payload.
   */
  @Getter
  @Setter
  private Boolean xmlPayload;

  @Override
  public void apply(SolConnectionFactory cf) {
    cf.setDmqEligible(getDmqEligible());
    cf.setElidingEligible(getElidingEligible());
    cf.setGenerateSenderID(getGenerateSenderID());
    cf.setXmlPayload(getXmlPayload());    
  }

}
