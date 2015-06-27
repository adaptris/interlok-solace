package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("solace-keepalive")
public class Keepalive implements Parameter {
  private Boolean keepAlives;
  private Integer keepAliveCountMax;
  private Integer keepAliveIntervalInMillis;
  
  @Override
  public void apply(SolConnectionFactory cf) {
    if(getKeepAlives() != null) {
      cf.setKeepAlives(getKeepAlives());
    }
    
    if(getKeepAliveCountMax() != null) {
      cf.setKeepAliveCountMax(getKeepAliveCountMax());
    }
    
    if(getKeepAliveIntervalInMillis() != null) {
      cf.setKeepAliveIntervalInMillis(getKeepAliveIntervalInMillis());
    }
  }

  public Boolean getKeepAlives() {
    return keepAlives;
  }

  /**
   * This property is used to enable/disable keep alives.
   */
  public void setKeepAlives(Boolean keepAlives) {
    this.keepAlives = keepAlives;
  }

  public Integer getKeepAliveCountMax() {
    return keepAliveCountMax;
  }

  /**
   * This property is used to specify the allowed number of consecutive keep-alive messages for which no response is received before the connection is closed by the API.
   */
  public void setKeepAliveCountMax(Integer keepAliveCountMax) {
    this.keepAliveCountMax = keepAliveCountMax;
  }

  public Integer getKeepAliveIntervalInMillis() {
    return keepAliveIntervalInMillis;
  }

  /**
   * This property is used to specify the interval between keep alives in milliseconds.
   */
  public void setKeepAliveIntervalInMillis(Integer keepAliveIntervalInMillis) {
    this.keepAliveIntervalInMillis = keepAliveIntervalInMillis;
  }
  
}
