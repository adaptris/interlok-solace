package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

@XStreamAlias("solace-keepalive")
public class Keepalive implements Parameter {
  
  /**
   * This property is used to enable/disable keep alives.
   */
  @Getter
  @Setter
  private Boolean keepAlives;
  /**
   * This property is used to specify the allowed number of consecutive keep-alive messages for which no response is received before the connection is closed by the API.
   */
  @Getter
  @Setter
  private Integer keepAliveCountMax;
  /**
   * This property is used to specify the interval between keep alives in milliseconds.
   */
  @Getter
  @Setter
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
  
}
