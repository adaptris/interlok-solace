package com.adaptris.core.jcsmp.solace.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.CoreException;
import com.solacesystems.jcsmp.JCSMPProperties;

public class CertificateAuthenticationProviderTest {
  
  private static final String TRUST_STORE_LOC = "./trustStore";
  private static final String KEY_STORE_LOC = "./keyStore";
  private static final String TRUST_STORE_PASSWORD = "trustStorePassword";
  private static final String KEY_STORE_PASSWORD = "keyStorePassword";
  private static final Boolean VALIDATE_CERT = false;
  private static final Boolean VALIDATE_CERT_DATE = false;
  
  private static final String TRUST_STORE_PASSWORD_EXCEPTION = "PW:trustStorePassword";
  private static final String KEY_STORE_PASSWORD_EXCEPTION = "PW:keyStorePassword";
  
  private static final String DEFAULT_TRUST_STORE_LOC = "";
  private static final String DEFAULT_KEY_STORE_LOC = "";
  private static final Boolean DEFAULT_VALIDATE_CERT = true;
  private static final Boolean DEFAULT_VALIDATE_CERT_DATE = true;
  
  private CertificateAuthenticationProvider authenticationProvider;
  
  @Before
  public void setUp() throws Exception {
    authenticationProvider = new CertificateAuthenticationProvider();
  }
  
  @After
  public void tearDown() throws Exception {
    
  }

  @Test
  public void testSetAllProperties() throws Exception {
    authenticationProvider.setValidateCertificate(VALIDATE_CERT);
    authenticationProvider.setValidateCertificateDate(VALIDATE_CERT_DATE);
    authenticationProvider.setSslKeyStoreLocation(KEY_STORE_LOC);
    authenticationProvider.setSslKeyStorePassword(KEY_STORE_PASSWORD);
    authenticationProvider.setSslTrustStoreLocation(TRUST_STORE_LOC);
    authenticationProvider.setSslTrustStorePassword(TRUST_STORE_PASSWORD);
    
    JCSMPProperties properties = authenticationProvider.setConnectionProperties();
    
    assertEquals(JCSMPProperties.AUTHENTICATION_SCHEME_CLIENT_CERTIFICATE, properties.getProperty(JCSMPProperties.AUTHENTICATION_SCHEME));
    
    assertEquals(VALIDATE_CERT, properties.getProperty(JCSMPProperties.SSL_VALIDATE_CERTIFICATE));
    assertEquals(VALIDATE_CERT_DATE, properties.getProperty(JCSMPProperties.SSL_VALIDATE_CERTIFICATE_DATE));
    assertEquals(TRUST_STORE_LOC, properties.getProperty(JCSMPProperties.SSL_TRUST_STORE));
    assertEquals(TRUST_STORE_PASSWORD, properties.getProperty(JCSMPProperties.SSL_TRUST_STORE_PASSWORD));
    assertEquals(KEY_STORE_LOC, properties.getProperty(JCSMPProperties.SSL_KEY_STORE));
    assertEquals(KEY_STORE_PASSWORD, properties.getProperty(JCSMPProperties.SSL_KEY_STORE_PASSWORD));
  }
  
  @Test
  public void testSetNoProperties() throws Exception {
    JCSMPProperties properties = authenticationProvider.setConnectionProperties();
    
    assertEquals(JCSMPProperties.AUTHENTICATION_SCHEME_CLIENT_CERTIFICATE, properties.getProperty(JCSMPProperties.AUTHENTICATION_SCHEME));
    
    assertEquals(DEFAULT_VALIDATE_CERT, properties.getProperty(JCSMPProperties.SSL_VALIDATE_CERTIFICATE));
    assertEquals(DEFAULT_VALIDATE_CERT_DATE, properties.getProperty(JCSMPProperties.SSL_VALIDATE_CERTIFICATE_DATE));
    assertEquals(DEFAULT_TRUST_STORE_LOC, properties.getProperty(JCSMPProperties.SSL_TRUST_STORE));
    assertNull(properties.getProperty(JCSMPProperties.SSL_TRUST_STORE_PASSWORD));
    assertEquals(DEFAULT_KEY_STORE_LOC, properties.getProperty(JCSMPProperties.SSL_KEY_STORE));
    assertNull(properties.getProperty(JCSMPProperties.SSL_KEY_STORE_PASSWORD));
  }
  
  @Test
  public void testTrustStorePasswordException() throws Exception {
    authenticationProvider.setSslTrustStorePassword(TRUST_STORE_PASSWORD_EXCEPTION);
    
    try {
      authenticationProvider.setConnectionProperties();
      fail("Invalid password should throw exception");
    } catch (CoreException ex) {
      // expected
      
    }
  }
  
  @Test
  public void testKeyStorePasswordException() throws Exception {
    authenticationProvider.setSslKeyStorePassword(KEY_STORE_PASSWORD_EXCEPTION);
    
    try {
      authenticationProvider.setConnectionProperties();
      fail("Invalid password should throw exception");
    } catch (CoreException ex) {
      // expected
      
    }
  }
}
