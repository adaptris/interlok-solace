package com.adaptris.core.jcsmp.solace;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MockBaseTest;
import com.adaptris.core.ProduceException;
import com.adaptris.core.jcsmp.solace.translator.SolaceJcsmpMessageTranslator;
import com.adaptris.core.util.LifecycleHelper;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.XMLMessageProducer;

public class SolaceJcsmpQueueProducerTest extends MockBaseTest {

  private SolaceJcsmpQueueProducer producer;

  private AdaptrisMessage adaptrisMessage;

  private String produceDestination;

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

  @BeforeEach
  public void setUp() throws Exception {
    adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage();

    produceDestination = "myDestination";

    producer = new SolaceJcsmpQueueProducer();
    producer.setJcsmpFactory(mockJcsmpFactory);
    producer.registerConnection(mockConnection);
    //    producer.setProducerEventHandler(callbackHandler);
    producer.setMessageTranslator(mockTranslator);
    producer.setQueue("myDestination");

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

  @AfterEach
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(producer);
  }

  @Test
  public void testSessionCache() throws Exception {
    when(mockConnection.createSession())
    .thenReturn(mockSession)
    .thenReturn(mockSession2);

    assertNull(producer.getSessionHelper().getSession());

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

    assertNull(producer.getSessionHelper().getSession());

    JCSMPSession session1 = producer.session();
    JCSMPSession session2 = producer.session();

    assertTrue(session1 != session2);
  }

  @Test
  public void testMessageProducerCache() throws Exception {
    when(mockSession.getMessageProducer(any(JCSMPStreamingPublishCorrelatingEventHandler.class)))
    .thenReturn(mockProducer)
    .thenReturn(mockProducer2);

    assertNull(producer.getMessageProducer());
  }

  @Test
  public void testQueueCache() throws Exception {
    producer.generateDestination(adaptrisMessage, "myDestination");
    producer.generateDestination(adaptrisMessage, "myDestination");
    producer.generateDestination(adaptrisMessage, "myDestination");

    verify(mockJcsmpFactory, times(1)).createQueue(any(String.class));
  }

  @Test
  public void testPublishSuccess() throws Exception {
    producer.doProduce(adaptrisMessage, produceDestination);

    verify(mockTranslator).translate(adaptrisMessage);
    verify(mockProducer).send(mockMessage, mockQueue);
  }

  @Test
  public void testPublishFailure() throws Exception {
    doThrow(new JCSMPException("Expected"))
    .when(mockProducer).send(mockMessage, mockQueue);

    try {
      producer.doProduce(adaptrisMessage, produceDestination);
      fail("Produce should fail.");
    } catch (ProduceException ex) {
      // expected
    }
  }

}
