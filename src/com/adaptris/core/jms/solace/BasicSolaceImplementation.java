package com.adaptris.core.jms.solace;

import javax.jms.JMSException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.core.CoreException;
import com.adaptris.core.jms.VendorImplementation;
import com.adaptris.core.jms.VendorImplementationImp;
import com.adaptris.core.licensing.License;
import com.adaptris.core.licensing.License.LicenseType;
import com.adaptris.core.licensing.LicenseChecker;
import com.adaptris.core.licensing.LicensedComponent;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Solace implementation of <code>VendorImplementation</code>.
 * </p>
 * <p>
 * This vendor implementation is the minimal adapter interface to Solace.
 * </p>
 * <p>
 * <b>This was built against Solace 7.1.0.207</b>
 * </p>
 * <p>
 * 
 * @config basic-solace-implementation
 * @license BASIC
 */
@XStreamAlias("basic-solace-implementation")
public class BasicSolaceImplementation extends VendorImplementationImp implements LicensedComponent {
  @NotBlank
  private String hostname;
  
  private int port = 55555;
  
  @NotBlank
  private String messageVpn = "default";

  @Override
  public SolConnectionFactory createConnectionFactory() throws JMSException {
    try {
      SolConnectionFactory connnectionFactory = SolJmsUtility.createConnectionFactory();
      connnectionFactory.setHost(getHostname());
      connnectionFactory.setPort(getPort());
      connnectionFactory.setVPN(getMessageVpn());
      // Username and password will be set in .connect(username, password)
      return connnectionFactory;
      
      
    } catch (JMSException e) {
      throw e;
    } catch (Exception e) {
      JMSException ex = new JMSException("Unexpected Exception creating Solace connectionfactory");
      ex.setLinkedException(e);
      throw ex;
    }
  }
  
  @Override
  public String retrieveBrokerDetailsForLogging() {
    return String.format("Solace host: %s; Message vpn: %s", getHostname(), getMessageVpn());
  }

  public String getHostname() {
    return hostname;
  }

  /**
   * Appliance or VMR host name, must be prefixed with smf://
   * @param hostname
   */
  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public int getPort() {
    return port;
  }

  /**
   * Port number. Default: 55555
   * @param port
   */
  public void setPort(int port) {
    this.port = port;
  }

  public String getMessageVpn() {
    return messageVpn;
  }

  /**
   * Message VPN name. Default: default
   */
  public void setMessageVpn(String messageVpn) {
    this.messageVpn = messageVpn;
  }

  @Override
  public boolean connectionEquals(VendorImplementation other) {
    if (other instanceof BasicSolaceImplementation) {
      BasicSolaceImplementation rhs = (BasicSolaceImplementation) other;
      return new EqualsBuilder()
        .append(getHostname(), rhs.getHostname())
        .append(getPort(), rhs.getPort())
        .append(getMessageVpn(), rhs.getMessageVpn())
        .isEquals();
    }
    return false;
  }


  @Override
  public void prepare() throws CoreException {
    LicenseChecker.newChecker().checkLicense(this);
    super.prepare();
  }

  @Override
  public boolean isEnabled(License license) {
    return license.isEnabled(LicenseType.Standard);
  }
}
