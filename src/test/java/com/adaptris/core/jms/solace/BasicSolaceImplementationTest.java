package com.adaptris.core.jms.solace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.jms.JmsActorConfig;
import com.adaptris.core.jms.JmsDestination;
import com.adaptris.core.jms.JmsDestination.DestinationType;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.solacesystems.jms.SolConnectionFactory;

public class BasicSolaceImplementationTest {
  
  @Mock private Session mockSession;
  @Mock private JmsActorConfig mockActorConfig;
  @Mock private JmsDestination mockJmsDestination;
  @Mock private Destination mockDestination;
  @Mock private Queue mockQueue;
  @Mock private Topic mockTopic;
  @Mock private MessageConsumer mockMessageConsumer;
  
  private BasicSolaceImplementation sol;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    sol = new BasicSolaceImplementation();
    sol.setCreateConsumerMaxRetries(5);
    sol.setCreateConsumerRetryWaitSeconds(0);
    
    when(mockActorConfig.currentSession())
        .thenReturn(mockSession);
    
    when(mockSession.createQueue(any(String.class)))
        .thenReturn(mockQueue);
    
    when(mockSession.createTopic(any(String.class)))
        .thenReturn(mockTopic);
    
    when(mockSession.createConsumer(any(Destination.class), any(String.class), any(boolean.class)))
      .thenReturn(mockMessageConsumer);
  }
  
  @Test
  public void testCreateQueueConsumer() throws Exception {
    sol.createQueueReceiver(new ConfiguredConsumeDestination("MyQueue"), mockActorConfig);
    
    verify(mockSession).createConsumer(mockQueue, null, false);
  }
  
  @Test
  public void testCreateTopicSubscriber() throws Exception {
    sol.createTopicSubscriber(new ConfiguredConsumeDestination("MyTopic"), null, mockActorConfig);
    
    verify(mockSession).createConsumer(mockTopic, null);
  }
  
  @Test
  public void testCreateTopicDurableSubscriber() throws Exception {
    sol.createTopicSubscriber(new ConfiguredConsumeDestination("MyTopic"), "MySubId", mockActorConfig);
    
    verify(mockSession).createDurableSubscriber(mockTopic, "MySubId", null, false);
  }
  
  @Test
  public void testCreateDurableSubscriber() throws Exception {
    when(mockJmsDestination.destinationType())
        .thenReturn(DestinationType.TOPIC);
    when(mockJmsDestination.subscriptionId())
        .thenReturn("MySubId");
    when(mockJmsDestination.getDestination())
        .thenReturn(mockTopic);
    
    sol.createConsumer(mockJmsDestination, null, mockActorConfig);
    
    verify(mockSession).createDurableSubscriber(mockTopic, "MySubId", null, false);
  }
  
  @Test
  public void testCreateMessageConsumer() throws Exception {
    when(mockJmsDestination.destinationType())
        .thenReturn(DestinationType.QUEUE);
    when(mockJmsDestination.getDestination())
        .thenReturn(mockQueue);
    
    sol.createConsumer(mockJmsDestination, null, mockActorConfig);
    
    verify(mockSession).createConsumer(mockQueue, null, false);
  }
  
  @Test
  public void testCreateMessageConsumerWithRetry() throws Exception {
    when(mockJmsDestination.destinationType())
        .thenReturn(DestinationType.QUEUE);
    when(mockJmsDestination.getDestination())
        .thenReturn(mockQueue);
    
    when(mockSession.createConsumer(mockQueue, null, false))
        .thenThrow(new JMSException("Expected1"))
        .thenThrow(new JMSException("Expected2"))
        .thenThrow(new JMSException("Expected3"))
        .thenReturn(mockMessageConsumer);
    
    sol.createConsumer(mockJmsDestination, null, mockActorConfig);
    
    verify(mockSession, times(4)).createConsumer(mockQueue, null, false);
  }
  
  @Test
  public void testCreateMessageConsumerWithInfiniteRetry() throws Exception {
    sol.setCreateConsumerMaxRetries(0); // infinite
    
    when(mockJmsDestination.destinationType())
        .thenReturn(DestinationType.QUEUE);
    when(mockJmsDestination.getDestination())
        .thenReturn(mockQueue);
    
    when(mockSession.createConsumer(mockQueue, null, false))
        .thenThrow(new JMSException("Expected1"))
        .thenThrow(new JMSException("Expected2"))
        .thenThrow(new JMSException("Expected3"))
        .thenReturn(mockMessageConsumer);
    
    sol.createConsumer(mockJmsDestination, null, mockActorConfig);
    
    verify(mockSession, times(4)).createConsumer(mockQueue, null, false);
  }
  
  @Test
  public void testCreateMessageConsumerWithMaxRetry() throws Exception {
    when(mockJmsDestination.destinationType())
        .thenReturn(DestinationType.QUEUE);
    when(mockJmsDestination.getDestination())
        .thenReturn(mockQueue);
    
    when(mockSession.createConsumer(mockQueue, null, false))
        .thenThrow(new JMSException("Expected"));
    
    try {
      sol.createConsumer(mockJmsDestination, null, mockActorConfig);
      fail("Should fail after 5 attempts to create the consumer.");
    } catch (Exception ex) {
      // expected.
    }
    
    verify(mockSession, atLeast(5)).createConsumer(mockQueue, null, false);
  }
  
  @Test
  public void testDefaultValues() {
    assertEquals(new Integer(0), sol.createConsumerRetryWaitSeconds());
    assertEquals(new Integer(5), sol.createConsumerMaxRetries());
    
    sol.setCreateConsumerMaxRetries(null);
    sol.setCreateConsumerRetryWaitSeconds(null);
    
    assertEquals(new Integer(30), sol.createConsumerRetryWaitSeconds());
    assertEquals(new Integer(0), sol.createConsumerMaxRetries());
  }
  
  @Test
  public void testConnectionEquals() {
    assertTrue(sol.connectionEquals(sol));
    
    assertFalse(sol.connectionEquals(new StandardJndiImplementation()));
  }
  
  @Test
  public void testBasicProperties() throws JMSException {
    final String BROKER_URL = "tcp://hostname:12345";
    final String MESSAGE_VPN = "vpn1";
    
    sol.setBrokerUrl(BROKER_URL);
    sol.setMessageVpn(MESSAGE_VPN);
    
    SolConnectionFactory cf = sol.createConnectionFactory();
    
    assertEquals(BROKER_URL, cf.getHost());
    assertNull(cf.getPort());
    assertEquals(MESSAGE_VPN, cf.getVPN());
  }

  @Test
  @SuppressWarnings("deprecation")
  public void testHostnameMigratesToBrokerUrl() throws JMSException {
    final String BROKER_URL = "tcp://hostname:12345";
    final String MESSAGE_VPN = "vpn1";
    
    sol.setHostname(BROKER_URL);
    sol.setMessageVpn(MESSAGE_VPN);
    
    SolConnectionFactory cf = sol.createConnectionFactory();
    
    assertEquals(BROKER_URL, sol.getBrokerUrl());
    assertEquals(BROKER_URL, cf.getHost());
    assertNull(cf.getPort());
    assertEquals(MESSAGE_VPN, cf.getVPN());
  }
  
  @Test
  @SuppressWarnings("deprecation")
  public void testHostnameAndPortMigratesToBrokerUrl() throws JMSException {
    final String BROKER_URL = "tcp://hostname";
    final String MESSAGE_VPN = "vpn1";
    final Integer PORT = 12345;
    
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
    sol.setBrokerUrl("tcp://somewhere");
    sol.setMessageVpn("somevpn");
    
    assertEquals("Solace host: tcp://somewhere; Message vpn: somevpn", sol.retrieveBrokerDetailsForLogging());
  }

}
