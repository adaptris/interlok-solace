package com.adaptris.core.jms.xa.solace;

import java.util.Iterator;

import javax.jms.JMSException;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.jms.XASession;

import com.adaptris.core.jms.solace.AdvancedSolaceImplementation;
import com.adaptris.core.jms.solace.parameters.Parameter;
import com.adaptris.util.KeyValuePair;
import com.adaptris.xa.jms.XAVendorImplementation;
import com.solacesystems.jms.SolJmsUtility;
import com.solacesystems.jms.SolXAConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("xa-advanced-solace-implementation")
public class XaAdvancedSolaceImplementation extends AdvancedSolaceImplementation implements XAVendorImplementation {

  @Override
  public XAConnectionFactory createXAConnectionFactory() throws JMSException {
    SolXAConnectionFactory connectionFactory = null;
    try {
      connectionFactory = SolJmsUtility.createXAConnectionFactory();
      connectionFactory.setHost(getBrokerUrl());
      connectionFactory.setVPN(getMessageVpn());
      connectionFactory.setDirectOptimized(false);
      connectionFactory.setDirectTransport(false);
      
      if(getAuthenticationScheme() != null) {
        connectionFactory.setAuthenticationScheme(getAuthenticationScheme().getValue());
      }
      connectionFactory.setCompressionLevel(getCompressionLevel());
      
      if(getDeliveryMode() != null) {
        connectionFactory.setDeliveryMode(getDeliveryMode().getDeliveryMode());
      }
      
      connectionFactory.setDirectOptimized(directOptimized());
      connectionFactory.setDirectTransport(directTransport());
      connectionFactory.setDynamicDurables(dynamicDurables());
      connectionFactory.setRespectTTL(getRespectTTL());

      for(Parameter p: getExtraParameters()) {
        p.apply(connectionFactory);
      }
      
      for(Iterator<KeyValuePair> it = getProperties().iterator(); it.hasNext();) {
        KeyValuePair kv = it.next();
        connectionFactory.setProperty(kv.getKey(), kv.getValue());
      }

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
