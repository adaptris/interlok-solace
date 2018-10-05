package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("solace-client")
public class Client implements Parameter {

  private String clientDescription;
  
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

  public String getClientDescription() {
    return clientDescription;
  }

  /**
   * This property is used to specify the application description on the appliance for the data connection.
   */
  public void setClientDescription(String clientDescription) {
    this.clientDescription = clientDescription;
  }

  /**
   * This property is used to specify the client name on the appliance for the data connection.
   */
  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

}
