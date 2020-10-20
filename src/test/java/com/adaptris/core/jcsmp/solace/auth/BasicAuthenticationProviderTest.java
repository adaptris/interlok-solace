package com.adaptris.core.jcsmp.solace.auth;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.CoreException;
import com.solacesystems.jcsmp.JCSMPProperties;

public class BasicAuthenticationProviderTest {
  
  private static final String USERNAME = "myUserName";
  private static final String DEFAULT_USERNAME = "";
  
  private static final String PASSWORD = "myPassword";
  private static final String DEFAULT_PASSWORD = "";
  
  private static final String PASSWORD_WITH_EXCEPTION = "PW:myPassword";
  
  private BasicAuthenticationProvider authenticationProvider;
  
  @Before
  public void setUp() throws Exception {
    authenticationProvider = new BasicAuthenticationProvider();
  }
  
  @After
  public void tearDown() throws Exception {
    
  }
  
  @Test
  public void testSetSuppliedCredentials() throws Exception {
    authenticationProvider.setUsername(USERNAME);
    authenticationProvider.setPassword(PASSWORD);
    
    JCSMPProperties properties = authenticationProvider.setConnectionProperties();
    
    assertEquals(JCSMPProperties.AUTHENTICATION_SCHEME_BASIC, properties.getProperty(JCSMPProperties.AUTHENTICATION_SCHEME));
    
    assertEquals(USERNAME, properties.getProperty(JCSMPProperties.USERNAME));
    assertEquals(PASSWORD, properties.getProperty(JCSMPProperties.PASSWORD));
  }
  
  @Test
  public void testSetUnSuppliedCredentials() throws Exception {
    JCSMPProperties properties = authenticationProvider.setConnectionProperties();
    
    assertEquals(JCSMPProperties.AUTHENTICATION_SCHEME_BASIC, properties.getProperty(JCSMPProperties.AUTHENTICATION_SCHEME));
    
    assertEquals(DEFAULT_USERNAME, properties.getProperty(JCSMPProperties.USERNAME));
    assertEquals(DEFAULT_PASSWORD, properties.getProperty(JCSMPProperties.PASSWORD));
  }

  @Test
  public void testPasswordException() throws Exception {
    authenticationProvider.setUsername(USERNAME);
    authenticationProvider.setPassword(PASSWORD_WITH_EXCEPTION);
    
    try {
      authenticationProvider.setConnectionProperties();
      fail("Invalid password should throw exception");
    } catch (CoreException ex) {
      // expected
      
    }
  }
}
