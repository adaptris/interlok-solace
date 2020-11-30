package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

@XStreamAlias("solace-client")
public class Client implements Parameter {

  /**
   * This property is used to specify the application description on the appliance for the data connection.
   */
  @Getter
  @Setter
  private String clientDescription;
  
  /**
   * This property is used to specify the client name on the appliance for the data connection.
   */
  @Getter
  @Setter
  private String clientId;
  
  @Override
  public void apply(SolConnectionFactory cf) {
    if(getClientDescription() != null) {
      cf.setClientDescription(getClientDescription());
    }
    
    if(getClientId() != null) {
      cf.setClientID(getClientId());
    }
  }

}
