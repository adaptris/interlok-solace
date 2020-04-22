package com.adaptris.core.jcsmp.solace;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.util.LifecycleHelper;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.XMLMessageProducer;

public class SolaceJcsmpQueueProducerTest {
  
  private SolaceJcsmpQueueProducer producer;
  
  private AdaptrisMessage adaptrisMessage;
  
  private ProduceDestination produceDestination;
  
  @Mock private SolaceJcsmpConnection mockConnection;
  
  @Mock private SolaceJcsmpConnectionErrorHandler mockConnectionErrorHandler;
  
  @Mock private JCSMPFactory mockJcsmpFactory;
  
  @Mock private Queue mockQueue;

  @Mock private JCSMPSession mockSession;
  
  @Mock private JCSMPSession mockSession2;

  @Mock private XMLMessageProducer mockProducer;
  
  @Mock private XMLMessageProducer mockProducer2;
  
  @Mock private SolaceJcsmpMessageTranslator mockTranslator;

  @Mock private BytesXMLMessage mockMessage;
  
  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage();
    
    produceDestination = new ConfiguredProduceDestination("myDestination");
    
    producer = new SolaceJcsmpQueueProducer();
    producer.setJcsmpFactory(mockJcsmpFactory);
    producer.registerConnection(mockConnection);
//    producer.setProducerEventHandler(callbackHandler);
    producer.setMessageTranslator(mockTranslator);
    
    when(mockConnection.createSession())
        .thenReturn(mockSession);
    when(mockConnection.getConnectionErrorHandler())
        .thenReturn(mockConnectionErrorHandler);
    when(mockJcsmpFactory.createQueue(any(String.class)))
        .thenReturn(mockQueue);
    when(mockConnection.retrieveConnection(SolaceJcsmpConnection.class))
        .thenReturn(mockConnection);
    when(mockSession.getMessageProducer(any(JCSMPStreamingPublishCorrelatingEventHandler.class)))
        .thenReturn(mockProducer);
    when(mockTranslator.translate(adaptrisMessage))
        .thenReturn(mockMessage);
    
    LifecycleHelper.initAndStart(producer);
  }
  
  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(producer);
  }

  @Test
  public void testSessionCache() throws Exception {
    when(mockConnection.createSession())
        .thenReturn(mockSession)
        .thenReturn(mockSession2);
    
    assertNull(producer.getCurrentSession());
    
    JCSMPSession session1 = producer.session();
    JCSMPSession session2 = producer.session();
    
    assertTrue(session1 == session2);
  }
  
  @Test
  public void testSessionCacheFirstSessiongetsClosed() throws Exception {
    when(mockConnection.createSession())
        .thenReturn(mockSession)
        .thenReturn(mockSession2);
    when(mockSession.isClosed())
        .thenReturn(true);
    
    assertNull(producer.getCurrentSession());
    
    JCSMPSession session1 = producer.session();
    JCSMPSession session2 = producer.session();
    
    assertTrue(session1 != session2);
  }
  
  @Test
  public void testMessageProducerCache() throws Exception {
    when(mockSession.getMessageProducer(any(JCSMPStreamingPublishEventHandler.class)))
        .thenReturn(mockProducer)
        .thenReturn(mockProducer2);
    
    assertNull(producer.getMessageProducer());
    
//    XMLMessageProducer messageProducer1 = producer.messageProducer();
//    XMLMessageProducer messageProducer2 = producer.messageProducer();
    
//    assertTrue(messageProducer1 == messageProducer2);
  }
  
  @Test
  public void testQueueCache() throws Exception {
    producer.generateDestination(adaptrisMessage, produceDestination);
    producer.generateDestination(adaptrisMessage, produceDestination);
    producer.generateDestination(adaptrisMessage, produceDestination);
    
    verify(mockJcsmpFactory, times(1)).createQueue(any(String.class));
  }
  
  @Test
  public void testPublishSuccess() throws Exception {
    doAnswer(invocation -> {
      producer.getProducerLatch().countDown();
      return null;
    }).when(mockProducer).send(mockMessage, mockQueue);
    
    producer.produce(adaptrisMessage, produceDestination);
    
    
    verify(mockTranslator).translate(adaptrisMessage);
    verify(mockProducer).send(mockMessage, mockQueue);
  }
  
  @Test
  public void testPublishNoAnswerFromAsyncProducer() throws Exception {
    try {
      producer.produce(adaptrisMessage, produceDestination);
      fail("no answer from async producer should cause a produce exception");
    } catch (ProduceException ex) {
      // expected
    }
  }
  
  @Test
  public void testPublishFailure() throws Exception {
    doThrow(new JCSMPException("Expected"))
        .when(mockProducer).send(mockMessage, mockQueue);
    
    try {
      producer.produce(adaptrisMessage, produceDestination);
      fail("Produce should fail.");
    } catch (ProduceException ex) {
   // expected
    }
  }
  
}
