package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("solace-receive-flags")
public class ReceiveFlags implements Parameter {
  private Boolean deliverToOne;
  private Boolean deliverToOneOverride;

  @Override
  public void apply(SolConnectionFactory cf) {
    cf.setDeliverToOne(getDeliverToOne());
    cf.setDeliverToOneOverride(getDeliverToOneOverride());
  }

  public Boolean getDeliverToOne() {
    return deliverToOne;
  }

  /**
   * This property specifies whether sent messages are deliver to one.
   */
  public void setDeliverToOne(Boolean deliverToOne) {
    this.deliverToOne = deliverToOne;
  }

  public Boolean getDeliverToOneOverride() {
    return deliverToOneOverride;
  }

  /**
   * This property specifies whether to override deliver to one settings.
   */
  public void setDeliverToOneOverride(Boolean deliverToOneOverride) {
    this.deliverToOneOverride = deliverToOneOverride;
  }

}
