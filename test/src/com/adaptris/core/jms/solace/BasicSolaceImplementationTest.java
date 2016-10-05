package com.adaptris.core.jms.solace;

import static org.junit.Assert.*;

import javax.jms.JMSException;

import org.junit.Test;

import com.solacesystems.jms.SolConnectionFactory;

public class BasicSolaceImplementationTest {
  
  @Test
  public void testBasicProperties() throws JMSException {
    final String BROKER_URL = "tcp://hostname:12345";
    final String MESSAGE_VPN = "vpn1";
    
    BasicSolaceImplementation sol = new BasicSolaceImplementation();
    sol.setBrokerUrl(BROKER_URL);
    sol.setMessageVpn(MESSAGE_VPN);
    
    SolConnectionFactory cf = sol.createConnectionFactory();
    
    assertEquals(BROKER_URL, cf.getHost());
    assertNull(cf.getPort());
    assertEquals(MESSAGE_VPN, cf.getVPN());
  }

  @Test
  public void testHostnameMigratesToBrokerUrl() throws JMSException {
    final String BROKER_URL = "tcp://hostname:12345";
    final String MESSAGE_VPN = "vpn1";
    
    BasicSolaceImplementation sol = new BasicSolaceImplementation();
    sol.setHostname(BROKER_URL);
    sol.setMessageVpn(MESSAGE_VPN);
    
    SolConnectionFactory cf = sol.createConnectionFactory();
    
    assertEquals(BROKER_URL, sol.getBrokerUrl());
    assertEquals(BROKER_URL, cf.getHost());
    assertNull(cf.getPort());
    assertEquals(MESSAGE_VPN, cf.getVPN());
  }
  
  @Test
  public void testHostnameAndPortMigratesToBrokerUrl() throws JMSException {
    final String BROKER_URL = "tcp://hostname";
    final String MESSAGE_VPN = "vpn1";
    final Integer PORT = 12345;
    
    BasicSolaceImplementation sol = new BasicSolaceImplementation();
    sol.setHostname(BROKER_URL);
    sol.setPort(PORT);
    sol.setMessageVpn(MESSAGE_VPN);
    
    SolConnectionFactory cf = sol.createConnectionFactory();
    
    assertEquals(BROKER_URL + ":" + PORT, sol.getBrokerUrl());
    assertEquals(BROKER_URL + ":" + PORT, cf.getHost());
    assertNull(cf.getPort());
    assertEquals(MESSAGE_VPN, cf.getVPN());
  }
  
  @Test
  public void testBrokerDetailsForLogging() {
    AdvancedSolaceImplementation sol = new AdvancedSolaceImplementation();
    sol.setBrokerUrl("tcp://somewhere");
    sol.setMessageVpn("somevpn");
    
    assertEquals("Solace host: tcp://somewhere; Message vpn: somevpn", sol.retrieveBrokerDetailsForLogging());
  }

}
