package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("kerberos")
public class Kerberos implements Parameter {
  protected Boolean krbMutualAuthentication;
  protected String krbServiceName;
  
  @Override
  public void apply(SolConnectionFactory cf) {
    cf.setKRBMutualAuthentication(getKrbMutualAuthentication());
    cf.setKRBServiceName(getKrbServiceName());
  }

  public Boolean getKrbMutualAuthentication() {
    return krbMutualAuthentication;
  }

  /**
   * This property is used to indicate that mutual authentication is to be used when Kerberos is enabled.
   */
  public void setKrbMutualAuthentication(Boolean krbMutualAuthentication) {
    this.krbMutualAuthentication = krbMutualAuthentication;
  }

  public String getKrbServiceName() {
    return krbServiceName;
  }

  /**
   * This property is used to specify the ServiceName portion of the Service Principal Name (SPN) that has a format of ServiceName/ApplianceName@REALM.
   */
  public void setKrbServiceName(String krbServiceName) {
    this.krbServiceName = krbServiceName;
  }

}