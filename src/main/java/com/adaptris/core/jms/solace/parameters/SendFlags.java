package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("solace-send-flags")
public class SendFlags implements Parameter {
  private Boolean dmqEligible;
  private Boolean elidingEligible;
  private Boolean generateSenderID;
  private Boolean xmlPayload;

  @Override
  public void apply(SolConnectionFactory cf) {
    cf.setDmqEligible(getDmqEligible());
    cf.setElidingEligible(getElidingEligible());
    cf.setGenerateSenderID(getGenerateSenderID());
    cf.setXmlPayload(getXmlPayload());    
  }
  
  public Boolean getDmqEligible() {
    return dmqEligible;
  }

  /**
   * This property specifies whether sent messages are eligible for the dead message queue.
   */
  public void setDmqEligible(Boolean dmqEligible) {
    this.dmqEligible = dmqEligible;
  }

  public Boolean getElidingEligible() {
    return elidingEligible;
  }

  /**
   * This property specifies whether sent messages are marked as eligible for eliding.
   */
  public void setElidingEligible(Boolean elidingEligible) {
    this.elidingEligible = elidingEligible;
  }

  public Boolean getGenerateSenderID() {
    return generateSenderID;
  }

  /**
   * This property is used to Indicates whether the client name should be included in the SenderID message header parameter.
   */
  public void setGenerateSenderID(Boolean generateSenderID) {
    this.generateSenderID = generateSenderID;
  }

  public Boolean getXmlPayload() {
    return xmlPayload;
  }

  /**
   * This property specifies whether sent text messages have an XML payload.
   */
  public void setXmlPayload(Boolean xmlPayload) {
    this.xmlPayload = xmlPayload;
  }

}
