package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

@XStreamAlias("solace-receive-flags")
public class ReceiveFlags implements Parameter {
  
  /**
   * This property specifies whether sent messages are deliver to one.
   */
  @Getter
  @Setter
  private Boolean deliverToOne;
  
  /**
   * This property specifies whether to override deliver to one settings.
   */
  @Getter
  @Setter
  private Boolean deliverToOneOverride;

  @Override
  public void apply(SolConnectionFactory cf) {
    cf.setDeliverToOne(getDeliverToOne());
    cf.setDeliverToOneOverride(getDeliverToOneOverride());
  }

}
