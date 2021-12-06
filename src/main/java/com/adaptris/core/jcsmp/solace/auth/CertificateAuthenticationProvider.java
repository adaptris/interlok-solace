package com.adaptris.core.jcsmp.solace.auth;

import java.util.Optional;
import java.util.function.Function;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.CoreException;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

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

  /**
   * The Solace session property to determine whether to validate the client certificate.
   * @param validateCertificate
   */
  @Getter
  @Setter
  private Boolean validateCertificate;
  
  /**
   * The Solace session property to determine whether to validate the client certificate date.
   * @param validateCertificate
   */
  @Getter
  @Setter
  private Boolean validateCertificateDate;
  
  /**
   * The Solace client property to specify the location of your trust store.
   * @param validateCertificate
   */
  @Getter
  @Setter
  private String sslTrustStoreLocation;
  
  /**
   * The Solace client property to specify the password of your trust store.
   * Note this password can also be encoded using the appropriate {@link com.adaptris.security.password.Password} 
   * @param validateCertificate
   */
  @InputFieldHint(style = "PASSWORD", external=true)
  @Getter
  @Setter
  private String sslTrustStorePassword;
  
  /**
   * (Optional) This property is used to specify the format of the truststore given in SSL_TRUST_STORE.
   */
  @Getter
  @Setter
  private String sslTrustStoreFormat;
  
  /**
   * This property is used to specify a comma separated list of acceptable common 
   * names for matching with server certificates. The API performs a case insensitive 
   * comparison of the common names provided in this property with the common name in the 
   * server certificate. Note that leading and trailing whitespaces are considered to be 
   * part of the common names and are not ignored. An empty string means accept all common 
   * names. No common name validation will be performed (overriding this property) 
   * if SSL_VALIDATE_CERTIFICATE is set to false.
   */
  @Getter
  @Setter
  private String sslTrustedCommonNameList;
  
  /**
   * The Solace client property to specify the location of your key store.
   * @param validateCertificate
   */
  @Getter
  @Setter
  private String sslKeyStoreLocation;
  
  /**
   * The Solace client property to specify the password of your key store.
   * Note this password can also be encoded using the appropriate {@link com.adaptris.security.password.Password} 
   * @param validateCertificate
   */
  @InputFieldHint(style = "PASSWORD", external=true)
  @Getter
  @Setter
  private String sslKeyStorePassword;
  
  /**
   * (Optional) This property is used to specify the format of the keystore given in SSL_KEY_STORE.
   */
  @Getter
  @Setter
  private String sslKeyStoreFormat;
  
  /**
   * This property is used to specify the alias of the private key to use for client certificate authentication. 
   * If there is only one private key entry in the keystore specified by SSL_KEY_STORE, then this property doesn't have to be set.
   */
  @Getter
  @Setter
  @InputFieldDefault(value="")
  private String sslPrivateKeyAlias;
  
  @Override
  public JCSMPProperties initConnectionProperties() throws CoreException {
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
      setProperty(properties, JCSMPProperties.SSL_TRUST_STORE_FORMAT, 
          noOpFunction(), 
          Optional.ofNullable(getSslTrustStoreFormat()));
      setProperty(properties, JCSMPProperties.SSL_TRUSTED_COMMON_NAME_LIST, 
          noOpFunction(), 
          Optional.ofNullable(getSslTrustedCommonNameList()));
      setProperty(properties, JCSMPProperties.SSL_KEY_STORE, 
          noOpFunction(), 
          Optional.ofNullable(getSslKeyStoreLocation()));
      setProperty(properties, JCSMPProperties.SSL_PRIVATE_KEY_ALIAS, 
          noOpFunction(), 
          Optional.ofNullable(getSslPrivateKeyAlias()));
      setProperty(properties, JCSMPProperties.SSL_KEY_STORE_PASSWORD, 
          decodeFunction(), 
          Optional.ofNullable(getSslKeyStorePassword()));
      setProperty(properties, JCSMPProperties.SSL_KEY_STORE_FORMAT, 
          decodeFunction(), 
          Optional.ofNullable(getSslKeyStoreFormat()));

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

}
