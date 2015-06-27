package com.adaptris.core.jms.solace;

import static org.junit.Assert.*;

import javax.jms.JMSException;

import org.junit.Test;

import com.solacesystems.jms.SolConnectionFactory;

public class BasicSolaceImplementationTest {
  
  @Test
  public void testBasicProperties() throws JMSException {
    final String HOSTNAME = "tcp://hostname";
    final String MESSAGE_VPN = "vpn1";
    final Integer PORT = 12345;
    
    BasicSolaceImplementation sol = new BasicSolaceImplementation();
    sol.setHostname(HOSTNAME);
    sol.setMessageVpn(MESSAGE_VPN);
    sol.setPort(PORT);
    
    SolConnectionFactory cf = sol.createConnectionFactory();
    
    assertEquals(HOSTNAME, cf.getHost());
    assertEquals(MESSAGE_VPN, cf.getVPN());
    assertEquals(PORT, cf.getPort());
  }
  
}
