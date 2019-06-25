package com.adaptris.core.jms.solace;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;

import javax.jms.JMSException;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.ObjectUtils;
import org.hibernate.validator.constraints.NotBlank;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.Removal;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.UrlVendorImplementation;
import com.adaptris.core.jms.VendorImplementationBase;
import com.adaptris.core.licensing.License;
import com.adaptris.core.licensing.License.LicenseType;
import com.adaptris.core.licensing.LicenseChecker;
import com.adaptris.core.licensing.LicensedComponent;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.util.NumberUtils;
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
public class BasicSolaceImplementation extends UrlVendorImplementation implements LicensedComponent {
  
  private static final int DEFAULT_SMF_PORT = 55555;
  private static boolean warningLogged = false;
  @Deprecated
  @Removal(message = "Use broker-url instead", version = "3.11.0")
  private String hostname;
  
  @Deprecated
  @Removal(message = "Use broker-url instead", version = "3.11.0")
  private Integer port;
  
  @NotBlank
  @AutoPopulated
  @InputFieldDefault(value = "default")
  private String messageVpn;
  
  @Override
  public SolConnectionFactory createConnectionFactory() throws JMSException {
    if (isBlank(getBrokerUrl()) && isNotBlank(getHostname())) {
      LoggingHelper.logWarning(warningLogged, () -> {
        warningLogged = true;
      }, "hostname and port are deprecated; please use broker-url instead");
      setBrokerUrl(getHostname() + (configuredLegacyPort() != DEFAULT_SMF_PORT ? ":" + configuredLegacyPort() : ""));
    }
    
    try {
      SolConnectionFactory connnectionFactory = SolJmsUtility.createConnectionFactory();
      connnectionFactory.setHost(getBrokerUrl());
      connnectionFactory.setVPN(messageVpn());
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
    return String.format("Solace host: %s; Message vpn: %s", getBrokerUrl(), getMessageVpn());
  }

  @Deprecated
  @Removal(message = "Use broker-url instead", version = "3.11.0")
  public String getHostname() {
    return hostname;
  }

  /**
   * Appliance or VMR host name, must be prefixed with smf://
   * @param hostname
   */
  @Deprecated
  @Removal(message = "Use broker-url instead", version = "3.11.0")
  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  @Deprecated
  @Removal(message = "Use broker-url instead", version = "3.11.0")
  public Integer getPort() {
    return port;
  }

  /**
   * Port number.
   * 
   * @param port
   */
  @Deprecated
  @Removal(message = "Use broker-url instead", version = "3.11.0")
  public void setPort(Integer port) {
    this.port = port;
  }

  private int configuredLegacyPort() {
    return NumberUtils.toIntDefaultIfNull(getPort(), DEFAULT_SMF_PORT);
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

  private String messageVpn() {
    return ObjectUtils.defaultIfNull(getMessageVpn(), "default");
  }

  @Override
  public boolean connectionEquals(VendorImplementationBase other) {
    if (other instanceof BasicSolaceImplementation) {
      BasicSolaceImplementation rhs = (BasicSolaceImplementation) other;
      return new EqualsBuilder()
        .append(getBrokerUrl(), rhs.getBrokerUrl())
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
