package com.adaptris.core.jms.solace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import javax.jms.JMSException;

import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.jms.solace.parameters.Client;
import com.adaptris.core.jms.solace.parameters.ConnectionTuning;
import com.adaptris.core.jms.solace.parameters.Keepalive;
import com.adaptris.core.jms.solace.parameters.Kerberos;
import com.adaptris.core.jms.solace.parameters.KeyStore;
import com.adaptris.core.jms.solace.parameters.ReceiveFlags;
import com.adaptris.core.jms.solace.parameters.ReceiveTuning;
import com.adaptris.core.jms.solace.parameters.SSL;
import com.adaptris.core.jms.solace.parameters.SendFlags;
import com.adaptris.core.jms.solace.parameters.SendTuning;
import com.solacesystems.jms.SolConnectionFactory;

public class ExtraParametersTest {
  
  private SolConnectionFactory cf;
  
  @Before
  public void setup() throws JMSException {
    BasicSolaceImplementation sol = new BasicSolaceImplementation();
    sol.setHostname("tcp://hostname");
    sol.setMessageVpn("vpn1");
    sol.setPort(12345);
    cf = sol.createConnectionFactory();
  }
  
  @Test
  public void testClient() {
    final String CLIENT_ID = "clientId";
    final String CLIENT_DESCRIPTION = "description";
    
    Client client = new Client();
    client.setClientId(CLIENT_ID);
    client.setClientDescription(CLIENT_DESCRIPTION);
    
    client.apply(cf);
    
    assertEquals(CLIENT_ID, cf.getClientID());
    assertEquals(CLIENT_DESCRIPTION, cf.getClientDescription());
  }

  @Test
  public void testConnectionTuning() {
    ConnectionTuning ct = new ConnectionTuning();
    ct.setConnectRetries(42);
    ct.setConnectRetriesPerHost(34);
    ct.setConnectTimeoutInMillis(34567);
    ct.setReadTimeoutInMillis(2345);
    ct.setReconnectRetries(78);
    ct.setReconnectRetryWaitInMillis(3345);
    ct.setTcpNoDelay(true);
    
    ct.apply(cf);
    
    assertEquals(42, cf.getConnectRetries().intValue());
    assertEquals(34, cf.getConnectRetriesPerHost().intValue());
    assertEquals(34567, cf.getConnectTimeoutInMillis().intValue());
    assertEquals(2345, cf.getReadTimeoutInMillis().intValue());
    assertEquals(78, cf.getReconnectRetries().intValue());
    assertEquals(3345, cf.getReconnectRetryWaitInMillis().intValue());
    assertTrue(cf.getTcpNoDelay());
  }
  
  @Test
  public void testKeepalive() {
    Keepalive ka = new Keepalive();
    ka.setKeepAliveCountMax(42);
    ka.setKeepAliveIntervalInMillis(5678);
    ka.setKeepAlives(true);
    
    ka.apply(cf);
    
    assertEquals(42, cf.getKeepAliveCountMax().intValue());
    assertEquals(5678, cf.getKeepAliveIntervalInMillis().intValue());
    assertTrue(cf.getKeepAlives());
  }
  
  @Test
  public void testKerberos() {
    final String KRB_SERVICE_NAME = "service name";
    
    Kerberos krb = new Kerberos();
    krb.setKrbMutualAuthentication(true);
    krb.setKrbServiceName(KRB_SERVICE_NAME);
    
    krb.apply(cf);
    
    assertTrue(cf.getKRBMutualAuthentication());
    assertEquals(KRB_SERVICE_NAME, cf.getKRBServiceName());
  }
  
  @Test
  public void testReceiveFlags() {
    ReceiveFlags rf = new ReceiveFlags();
    rf.setDeliverToOne(true);
    rf.setDeliverToOneOverride(true);
    
    rf.apply(cf);
    
    assertTrue(cf.getDeliverToOne());
    assertTrue(cf.getDeliverToOneOverride());
  }
  
  @Test
  public void testReceiveTuning() {
    ReceiveTuning rt = new ReceiveTuning();
    rt.setReceiveAdAckThreshold(1234);
    rt.setReceiveAdAckTimerInMillis(2345);
    rt.setReceiveAdWindowSize(3456);
    rt.setReceiveBufferSize(4567);
    rt.setSubscriberLocalPriority(3);
    rt.setSubscriberNetworkPriority(4);
    
    rt.apply(cf);
    
    assertEquals(1234, cf.getReceiveAdAckThreshold().intValue());
    assertEquals(2345, cf.getReceiveADAckTimerInMillis().intValue());
    assertEquals(3456, cf.getReceiveADWindowSize().intValue());
    assertEquals(4567, cf.getReceiveBufferSize().intValue());
    assertEquals(3, cf.getSubscriberLocalPriority().intValue());
    assertEquals(4, cf.getSubscriberNetworkPriority().intValue());
  }
  
  @Test
  public void testSendFlags() {
    SendFlags sf = new SendFlags();
    sf.setDmqEligible(true);
    sf.setElidingEligible(true);
    sf.setGenerateSenderID(true);
    sf.setXmlPayload(true);
    
    sf.apply(cf);
    
    assertTrue(cf.getDmqEligible());
    assertTrue(cf.getElidingEligible());
    assertTrue(cf.getGenerateSenderID());
    assertTrue(cf.getXmlPayload());
  }
  
  @Test
  public void testSendTuning() {
    SendTuning st = new SendTuning();
    st.setSendAdAckTimerInMillis(1234);
    st.setSendAdMaxResends(2345);
    st.setSendAdWindowSize(3456);
    st.setSendBufferSize(4567);
    
    st.apply(cf);
    
    assertEquals(1234, cf.getSendADAckTimerInMillis().intValue());
    assertEquals(2345, cf.getSendADMaxResends().intValue());
    assertEquals(3456, cf.getSendADWindowSize().intValue());
    assertEquals(4567, cf.getSendBufferSize().intValue());
  }
  
  @Test
  public void testSSL() {
    final String CIPHER_SUITES = "some_cipher,some_other_cipher";
    final String EXCLUDED_PROTOCOLS = "some_protocol,some_other_protocol";
    final String KEYSTORE_FILENAME = "keystore.p12";
    final String KEYSTORE_FORMAT = "PKCS12";
    final String KEYSTORE_PASSWORD = "password1";
    final String PRIVATE_KEY_ALIAS = "alias";
    final String PRIVATE_KEY_PASSWORD = "password";
    final String PROTOCOL = "protocol";
    final String TRUSTED_COMMON_NAME_LIST = "cn1,cn2,cn3";
    final String TRUSTSTORE_FILENAME = "truststore.jks";
    final String TRUSTSTORE_FORMAT = "JKS";
    final String TRUSTSTORE_PASSWORD = "password2";
    
    SSL ssl = new SSL();
    ssl.setCipherSuites(CIPHER_SUITES);
    ssl.setExcludedProtocols(EXCLUDED_PROTOCOLS);
    ssl.setKeyStore(keystore(KEYSTORE_FILENAME, KEYSTORE_FORMAT, KEYSTORE_PASSWORD));
    ssl.setPrivateKeyAlias(PRIVATE_KEY_ALIAS);
    ssl.setPrivateKeyPassword(PRIVATE_KEY_PASSWORD);
    ssl.setProtocol(PROTOCOL);
    ssl.setTrustedCommonNameList(TRUSTED_COMMON_NAME_LIST);
    ssl.setTrustStore(keystore(TRUSTSTORE_FILENAME, TRUSTSTORE_FORMAT, TRUSTSTORE_PASSWORD));
    ssl.setValidateCertificate(false);
    ssl.setValidateCertificateDate(false);
    
    ssl.apply(cf);

    assertEquals(CIPHER_SUITES, cf.getSSLCipherSuites());
    assertEquals(EXCLUDED_PROTOCOLS, cf.getSSLExcludedProtocols());
    assertEquals(KEYSTORE_FILENAME, cf.getSSLKeyStore());
    assertEquals(KEYSTORE_FORMAT, cf.getSSLKeyStoreFormat());
    assertEquals(KEYSTORE_PASSWORD, cf.getSSLKeyStorePassword());
    assertEquals(PRIVATE_KEY_ALIAS, cf.getSSLPrivateKeyAlias());
    assertEquals(PRIVATE_KEY_PASSWORD, cf.getSSLPrivateKeyPassword());
    assertEquals(PROTOCOL, cf.getSSLProtocol());
    assertEquals(TRUSTED_COMMON_NAME_LIST, cf.getSSLTrustedCommonNameList());
    assertEquals(TRUSTSTORE_FILENAME, cf.getSSLTrustStore());
    assertEquals(TRUSTSTORE_FORMAT, cf.getSSLTrustStoreFormat());
    assertEquals(TRUSTSTORE_PASSWORD, cf.getSSLTrustStorePassword());
    assertFalse(cf.getSSLValidateCertificate());
    assertFalse(cf.getSSLValidateCertificateDate());
  }
  
  private KeyStore keystore(String filename, String format, String password) {
    KeyStore ks = new KeyStore();
    ks.setFilename(filename);
    ks.setFormat(format);
    ks.setPassword(password);
    return ks;
  }
}
