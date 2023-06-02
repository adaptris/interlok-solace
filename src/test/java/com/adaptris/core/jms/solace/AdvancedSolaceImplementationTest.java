package com.adaptris.core.jms.solace;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import javax.jms.JMSException;

import org.junit.jupiter.api.Test;

import com.adaptris.core.jms.solace.parameters.Client;
import com.adaptris.core.jms.solace.parameters.ConnectionTuning;
import com.adaptris.util.KeyValuePair;
import com.solacesystems.jms.SolConnectionFactory;

public class AdvancedSolaceImplementationTest extends BasicSolaceImplementationTest {

  @Override
  @Test
  public void testBasicProperties() throws JMSException {
    final String BROKER_URL = "tcp://hostname:12345";
    final String MESSAGE_VPN = "vpn1";
    final AuthenticationSchemeEnum AUTHENTICATION_SCHEME = AuthenticationSchemeEnum.AUTHENTICATION_SCHEME_GSS_KRB;
    final Integer COMPRESSION_LEVEL = 3;
    final DeliveryModeEnum DELIVERY_MODE = DeliveryModeEnum.PERSISTENT;

    AdvancedSolaceImplementation sol = new AdvancedSolaceImplementation();
    sol.setBrokerUrl(BROKER_URL);
    sol.setMessageVpn(MESSAGE_VPN);
    sol.setAuthenticationScheme(AUTHENTICATION_SCHEME);
    sol.setCompressionLevel(COMPRESSION_LEVEL);
    sol.setDeliveryMode(DELIVERY_MODE);
    sol.setDirectOptimized(true);
    sol.setDirectTransport(true);
    sol.setDynamicDurables(true);
    sol.setRespectTTL(true);

    SolConnectionFactory cf = sol.createConnectionFactory();
    System.out.println(cf.getPropertyNames());

    assertEquals(BROKER_URL, cf.getHost());
    assertEquals(MESSAGE_VPN, cf.getVPN());
    assertNull(cf.getPort());
    assertEquals(AUTHENTICATION_SCHEME.getValue(), cf.getAuthenticationScheme());
    assertEquals(COMPRESSION_LEVEL, cf.getCompressionLevel());
    assertEquals(DELIVERY_MODE.getDeliveryMode(), cf.getDeliveryMode().intValue());
    assertTrue(cf.getDirectOptimized());
    assertTrue(cf.getDirectTransport());
    assertTrue(cf.getDynamicDurables());
    assertTrue(cf.getRespectTTL());
  }

  @Test
  public void testExtraParameters() throws JMSException {
    final String CLIENT_ID = "clientId";
    final String CLIENT_DESCRIPTION = "description";

    AdvancedSolaceImplementation sol = new AdvancedSolaceImplementation();
    Client client = new Client();
    client.setClientId(CLIENT_ID);
    client.setClientDescription(CLIENT_DESCRIPTION);
    sol.getExtraParameters().add(client);

    ConnectionTuning ct = new ConnectionTuning();
    ct.setConnectRetries(42);
    ct.setConnectRetriesPerHost(34);
    ct.setConnectTimeoutInMillis(34567);
    ct.setReadTimeoutInMillis(2345);
    ct.setReconnectRetries(78);
    ct.setReconnectRetryWaitInMillis(3345);
    ct.setTcpNoDelay(true);
    sol.getExtraParameters().add(ct);

    SolConnectionFactory cf = sol.createConnectionFactory();

    assertEquals(CLIENT_ID, cf.getClientID());
    assertEquals(CLIENT_DESCRIPTION, cf.getClientDescription());

    assertEquals(42, cf.getConnectRetries().intValue());
    assertEquals(34, cf.getConnectRetriesPerHost().intValue());
    assertEquals(34567, cf.getConnectTimeoutInMillis().intValue());
    assertEquals(2345, cf.getReadTimeoutInMillis().intValue());
    assertEquals(78, cf.getReconnectRetries().intValue());
    assertEquals(3345, cf.getReconnectRetryWaitInMillis().intValue());
    assertTrue(cf.getTcpNoDelay());
  }

  @Test
  public void testProperties() throws JMSException {
    final String KEY1 = "ClientID";
    final String VALUE1 = "client-id";
    final String KEY2 = "SSLCipherSuites";
    final String VALUE2 = "cipher1,cipher2";

    AdvancedSolaceImplementation sol = new AdvancedSolaceImplementation();
    sol.getProperties().add(new KeyValuePair(KEY1, VALUE1));
    sol.getProperties().add(new KeyValuePair(KEY2, VALUE2));
    SolConnectionFactory cf = sol.createConnectionFactory();

    assertEquals(VALUE1, cf.getProperty(KEY1));
    assertEquals(VALUE2, cf.getProperty(KEY2));
  }
}
