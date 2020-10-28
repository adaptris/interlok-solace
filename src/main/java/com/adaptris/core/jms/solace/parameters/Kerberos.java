package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

@XStreamAlias("solace-kerberos")
public class Kerberos implements Parameter {
  
  /**
   * This property is used to indicate that mutual authentication is to be used when Kerberos is enabled.
   */
  @Getter
  @Setter
  protected Boolean krbMutualAuthentication;
  /**
   * This property is used to specify the ServiceName portion of the Service Principal Name (SPN) that has a format of ServiceName/ApplianceName@REALM.
   */
  @Getter
  @Setter
  protected String krbServiceName;
  
  @Override
  public void apply(SolConnectionFactory cf) {
    cf.setKRBMutualAuthentication(getKrbMutualAuthentication());
    cf.setKRBServiceName(getKrbServiceName());
  }

}