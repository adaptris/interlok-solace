package com.adaptris.core.jms.solace.parameters;

import com.adaptris.annotation.InputFieldHint;
import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("ssl")
public class SSL implements Parameter {
  private String cipherSuites;
  
  private String excludedProtocols;

  private KeyStore keyStore;
  
  private String privateKeyAlias;
  
  @InputFieldHint(style = "PASSWORD")
  private String privateKeyPassword;
  
  private String protocol;
  
  private String trustedCommonNameList;
  
  private KeyStore trustStore;
  
  private Boolean validateCertificate;
  
  private Boolean validateCertificateDate;
  
  @Override
  public void apply(SolConnectionFactory cf) {
    if(getCipherSuites() != null) {
      cf.setSSLCipherSuites(getCipherSuites());
    }
    
    if(getExcludedProtocols() != null) {
      cf.setSSLExcludedProtocols(getExcludedProtocols());
    }
    
    if(getKeyStore() != null) {
      cf.setSSLKeyStore(getKeyStore().getFilename());
      cf.setSSLKeyStoreFormat(getKeyStore().getFormat());
      cf.setSSLKeyStorePassword(getKeyStore().getPassword());
      cf.setSSLPrivateKeyAlias(getPrivateKeyAlias());
      cf.setSSLPrivateKeyPassword(getPrivateKeyPassword());
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
      cf.setSSLTrustStorePassword(getTrustStore().getPassword());
    }

    if(getValidateCertificate() != null) {
      cf.setSSLValidateCertificate(getValidateCertificate());
    }
    
    if(getValidateCertificateDate() != null) {
      cf.setSSLValidateCertificateDate(getValidateCertificateDate());
    }
  }

  public String getCipherSuites() {
    return cipherSuites;
  }

  /**
   * This property specifies a comma separated list of the cipher suites to enable in order of preference.
   */
  public void setCipherSuites(String cipherSuites) {
    this.cipherSuites = cipherSuites;
  }

  public String getExcludedProtocols() {
    return excludedProtocols;
  }

  /**
   * This property is used to specify a comma separated list of encryption protocol(s) to exclude from use.
   */
  public void setExcludedProtocols(String excludedProtocols) {
    this.excludedProtocols = excludedProtocols;
  }

  public KeyStore getKeyStore() {
    return keyStore;
  }

  public void setKeyStore(KeyStore keyStore) {
    this.keyStore = keyStore;
  }

  public String getPrivateKeyAlias() {
    return privateKeyAlias;
  }

  /**
   * This property specifies which private key in the key store is to be used for the client certificate authentication.
   */
  public void setPrivateKeyAlias(String privateKeyAlias) {
    this.privateKeyAlias = privateKeyAlias;
  }

  public String getPrivateKeyPassword() {
    return privateKeyPassword;
  }

  /**
   * This property specifies the password used to decipher the client certificate private key from the key store.
   */
  public void setPrivateKeyPassword(String privateKeyPassword) {
    this.privateKeyPassword = privateKeyPassword;
  }

  public String getProtocol() {
    return protocol;
  }

  /**
   * This property is used to specify a comma separated list of encryption protocol(s) to use.
   */
  public void setProtocol(String protocol) {
    this.protocol = protocol;
  }

  public String getTrustedCommonNameList() {
    return trustedCommonNameList;
  }

  /**
   * This property specifies the list of acceptable common names for matching in server certificates.
   */
  public void setTrustedCommonNameList(String trustedCommonNameList) {
    this.trustedCommonNameList = trustedCommonNameList;
  }

  public KeyStore getTrustStore() {
    return trustStore;
  }

  /**
   * This property specifies the trust store to use
   */
  public void setTrustStore(KeyStore trustStore) {
    this.trustStore = trustStore;
  }

  public Boolean getValidateCertificate() {
    return validateCertificate;
  }

  /**
   * This property is used to indicate that the API should validate server certificates with certificates in the trust store.
   */
  public void setValidateCertificate(Boolean validateCertificate) {
    this.validateCertificate = validateCertificate;
  }

  public Boolean getValidateCertificateDate() {
    return validateCertificateDate;
  }

  /**
   * This property is used to indicate that the session connection should fail when an expired certificate or a certificate not yet in effect is received.
   */
  public void setValidateCertificateDate(Boolean validateCertificateDate) {
    this.validateCertificateDate = validateCertificateDate;
  }
  
}
