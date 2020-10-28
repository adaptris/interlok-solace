package com.adaptris.core.jms.solace.parameters;

import com.adaptris.annotation.InputFieldHint;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.password.Password;
import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

@XStreamAlias("solace-ssl")
public class SSL implements Parameter {
  
  /**
   * This property specifies a comma separated list of the cipher suites to enable in order of preference.
   */
  @Getter
  @Setter
  private String cipherSuites;
  /**
   * This property is used to specify a comma separated list of encryption protocol(s) to exclude from use.
   */
  @Getter
  @Setter
  private String excludedProtocols;

  @Getter
  @Setter
  private KeyStore keyStore;
  /**
   * This property specifies which private key in the key store is to be used for the client certificate authentication.
   */
  @Getter
  @Setter
  private String privateKeyAlias;
  /**
   * This property specifies the password used to decipher the client certificate private key from the key store.
   */
  @Getter
  @Setter
  @InputFieldHint(style = "PASSWORD", external = true)
  private String privateKeyPassword;
  /**
   * This property is used to specify a comma separated list of encryption protocol(s) to use.
   */
  @Getter
  @Setter
  private String protocol;
  /**
   * This property specifies the list of acceptable common names for matching in server certificates.
   */
  @Getter
  @Setter
  private String trustedCommonNameList;
  /**
   * This property specifies the trust store to use
   */
  @Getter
  @Setter
  private KeyStore trustStore;
  /**
   * This property is used to indicate that the API should validate server certificates with certificates in the trust store.
   */
  @Getter
  @Setter
  private Boolean validateCertificate;
  /**
   * This property is used to indicate that the session connection should fail when an expired certificate or a certificate not yet in effect is received.
   */
  @Getter
  @Setter
  private Boolean validateCertificateDate;
  
  @Override
  public void apply(SolConnectionFactory cf) throws Exception {
    if(getCipherSuites() != null) {
      cf.setSSLCipherSuites(getCipherSuites());
    }
    
    if(getExcludedProtocols() != null) {
      cf.setSSLExcludedProtocols(getExcludedProtocols());
    }
    
    if(getKeyStore() != null) {
      cf.setSSLKeyStore(getKeyStore().getFilename());
      cf.setSSLKeyStoreFormat(getKeyStore().getFormat());
      cf.setSSLKeyStorePassword(Password.decode(ExternalResolver.resolve(getKeyStore().getPassword())));
      cf.setSSLPrivateKeyAlias(getPrivateKeyAlias());
      cf.setSSLPrivateKeyPassword(Password.decode(ExternalResolver.resolve(getPrivateKeyPassword())));
    }

    if(getProtocol() != null) {
      cf.setSSLProtocol(getProtocol());
    }
    
    if(getTrustedCommonNameList() != null) {
      cf.setSSLTrustedCommonNameList(getTrustedCommonNameList());
    }
    
    if(getTrustStore() != null) {
      cf.setSSLTrustStore(getTrustStore().getFilename());
      cf.setSSLTrustStoreFormat(getTrustStore().getFormat());
      cf.setSSLTrustStorePassword(Password.decode(getTrustStore().getPassword()));
    }

    if(getValidateCertificate() != null) {
      cf.setSSLValidateCertificate(getValidateCertificate());
    }
    
    if(getValidateCertificateDate() != null) {
      cf.setSSLValidateCertificateDate(getValidateCertificateDate());
    }
  }
  
}
