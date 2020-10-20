package com.adaptris.core.jcsmp.solace.auth;

import java.util.Optional;
import java.util.function.Function;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.CoreException;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * A certificate based {@link AuthenticationProvider}.
 * </p>
 * @author aaron
 * @config solace-jcsmp-certificate-authentication
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP certificate authentication provider.", tag="authentication,certificate,solace,jcsmp", since="3.11.1")
@XStreamAlias("solace-jcsmp-certificate-authentication")
public class CertificateAuthenticationProvider implements AuthenticationProvider {

  private Boolean validateCertificate;
  
  private Boolean validateCertificateDate;
  
  private String sslTrustStoreLocation;
  
  @InputFieldHint(style = "PASSWORD", external=true)
  private String sslTrustStorePassword;
  
  private String sslKeyStoreLocation;
  
  @InputFieldHint(style = "PASSWORD", external=true)
  private String sslKeyStorePassword;
  
  @Override
  public JCSMPProperties setConnectionProperties() throws CoreException {
    JCSMPProperties properties = new JCSMPProperties();
    try {
      properties.setProperty(JCSMPProperties.AUTHENTICATION_SCHEME, JCSMPProperties.AUTHENTICATION_SCHEME_CLIENT_CERTIFICATE);
      
      setProperty(properties, JCSMPProperties.SSL_VALIDATE_CERTIFICATE, 
          noOpFunction(), 
          Optional.ofNullable(getValidateCertificate()));
      setProperty(properties, JCSMPProperties.SSL_VALIDATE_CERTIFICATE_DATE, 
          noOpFunction(), 
          Optional.ofNullable(getValidateCertificateDate()));
      setProperty(properties, JCSMPProperties.SSL_TRUST_STORE, 
          noOpFunction(), 
          Optional.ofNullable(getSslTrustStoreLocation()));
      setProperty(properties, JCSMPProperties.SSL_TRUST_STORE_PASSWORD, 
          decodeFunction(), 
          Optional.ofNullable(getSslTrustStorePassword()));
      setProperty(properties, JCSMPProperties.SSL_KEY_STORE, 
          noOpFunction(), 
          Optional.ofNullable(getSslKeyStoreLocation()));
      setProperty(properties, JCSMPProperties.SSL_KEY_STORE_PASSWORD, 
          decodeFunction(), 
          Optional.ofNullable(getSslKeyStorePassword()));

    } catch (Exception pe) {
      throw new CoreException(pe);
    }
    return properties;
  }

  private void setProperty(JCSMPProperties properties, String propertyName, Function<Object, Object> function, Optional<Object> optionalValue) {
    optionalValue
        .map(function)
        .ifPresent( value -> properties.setProperty(propertyName, optionalValue.get()));
  }
  
  private Function<Object, Object> decodeFunction() {
    return encodedPassword -> {
      try {
        return Password.decode(ExternalResolver.resolve((String) encodedPassword));
      } catch (PasswordException e) {
        throw new RuntimeException(e);
      }
    };
  }
  
  private Function<Object, Object> noOpFunction() {
    return value -> value;
  }

  public Boolean getValidateCertificate() {
    return validateCertificate;
  }

  public Boolean getValidateCertificateDate() {
    return validateCertificateDate;
  }

  public String getSslTrustStoreLocation() {
    return sslTrustStoreLocation;
  }

  public String getSslTrustStorePassword() {
    return sslTrustStorePassword;
  }

  public String getSslKeyStoreLocation() {
    return sslKeyStoreLocation;
  }

  public String getSslKeyStorePassword() {
    return sslKeyStorePassword;
  }

  /**
   * The Solace session property to determine whether to validate the client certificate.
   * @param validateCertificate
   */
  public void setValidateCertificate(Boolean validateCertificate) {
    this.validateCertificate = validateCertificate;
  }

  /**
   * The Solace session property to determine whether to validate the client certificate date.
   * @param validateCertificate
   */
  public void setValidateCertificateDate(Boolean validateCertificateDate) {
    this.validateCertificateDate = validateCertificateDate;
  }

  /**
   * The Solace client property to specify the location of your trust store.
   * @param validateCertificate
   */
  public void setSslTrustStoreLocation(String sslTrustStoreLocation) {
    this.sslTrustStoreLocation = sslTrustStoreLocation;
  }

  /**
   * The Solace client property to specify the password of your trust store.
   * Note this password can also be encoded using the appropriate {@link com.adaptris.security.password.Password} 
   * @param validateCertificate
   */
  public void setSslTrustStorePassword(String sslTrustStorePassword) {
    this.sslTrustStorePassword = sslTrustStorePassword;
  }

  /**
   * The Solace client property to specify the location of your key store.
   * @param validateCertificate
   */
  public void setSslKeyStoreLocation(String sslKeyStoreLocation) {
    this.sslKeyStoreLocation = sslKeyStoreLocation;
  }

  /**
   * The Solace client property to specify the password of your key store.
   * Note this password can also be encoded using the appropriate {@link com.adaptris.security.password.Password} 
   * @param validateCertificate
   */
  public void setSslKeyStorePassword(String sslKeyStorePassword) {
    this.sslKeyStorePassword = sslKeyStorePassword;
  }

}
