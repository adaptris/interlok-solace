package com.adaptris.core.jms.xa.solace;

import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;

import com.adaptris.core.jms.solace.BasicSolaceImplementation;
import com.adaptris.xa.jms.XAVendorImplementation;
import com.solacesystems.jms.SolJmsUtility;
import com.solacesystems.jms.SolXAConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("xa-basic-solace-implementation")
public class XaBasicSolaceImplementation extends BasicSolaceImplementation implements XAVendorImplementation {

  @Override
  public XAConnectionFactory createXAConnectionFactory() throws JMSException {
    try {
      SolXAConnectionFactory connectionFactory = SolJmsUtility.createXAConnectionFactory();
      connectionFactory.setHost(getBrokerUrl());
      connectionFactory.setVPN(getMessageVpn());
      connectionFactory.setDirectOptimized(false);
      connectionFactory.setDirectTransport(false);

      return connectionFactory;
    } catch (JMSException e) {
      throw e;
    } catch (Exception e) {
      JMSException ex = new JMSException("Unexpected Exception creating Solace connectionfactory");
      ex.setLinkedException(e);
      throw ex;
    }
  }

  @Override
  public XASession createXASession(XAConnection connection) throws JMSException {
    log.info("Creating a new XA session.");
    XASession session = ((XAConnection)connection).createXASession();
    applyVendorSessionProperties(session);
    return session;
  }

}
