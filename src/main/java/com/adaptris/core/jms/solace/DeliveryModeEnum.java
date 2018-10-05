package com.adaptris.core.jms.solace;

import javax.jms.DeliveryMode;

public enum DeliveryModeEnum {
  PERSISTENT(DeliveryMode.PERSISTENT),
  NON_PERSISTENT(DeliveryMode.NON_PERSISTENT);
  
  private final int v;
  
  private DeliveryModeEnum(final int v) {
    this.v = v;
  }
  
  public int getDeliveryMode() {
    return v;
  }
}
