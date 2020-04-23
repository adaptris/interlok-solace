package com.adaptris.core.jcsmp.solace;

import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
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
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ConfiguredConsumeDestination;
import com.adaptris.core.ConnectionErrorHandler;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Topic;
import com.solacesystems.jcsmp.XMLMessageConsumer;
import com.solacesystems.jcsmp.XMLMessageListener;

public class SolaceJcsmpTopicConsumerTest {

private SolaceJcsmpTopicConsumer consumer;
  
  @Mock private SolaceJcsmpConnection mockConnection;
  
  @Mock private JCSMPSession mockSession;
  
  @Mock private JCSMPFactory mockJcsmpFactory;
  
  @Mock private Topic mockTopic;

  @Mock private XMLMessageConsumer mockMessageConsumer;
  
  @Mock private BytesXMLMessage mockBytesMessage;
  
  @Mock private SolaceJcsmpMessageTranslator mockTranslator;
  
  @Mock private AdaptrisMessageListener mockMessageListener;
  
  @Mock private AdaptrisMessage mockAdpMessage;
  
  @Mock private ConnectionErrorHandler mockConnectionErrorHandler;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    consumer = new SolaceJcsmpTopicConsumer();
    consumer.registerConnection(mockConnection);
    consumer.setJcsmpFactory(mockJcsmpFactory);
    consumer.setDestination(new ConfiguredConsumeDestination("myQueue"));
    consumer.setMessageTranslator(mockTranslator);
    consumer.registerAdaptrisMessageListener(mockMessageListener);
    
    when(mockConnection.createSession())
        .thenReturn(mockSession);
    when(mockConnection.retrieveConnection(SolaceJcsmpConnection.class))
        .thenReturn(mockConnection);
    when(mockSession.getMessageConsumer(any(XMLMessageListener.class)))
        .thenReturn(mockMessageConsumer);
    when(mockJcsmpFactory.createTopic(any(String.class)))
        .thenReturn(mockTopic);
    when(mockTranslator.translate(mockBytesMessage))
        .thenReturn(mockAdpMessage);
    when(mockConnection.getConnectionErrorHandler())
        .thenReturn(mockConnectionErrorHandler);
  }
  
  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(consumer);
  }
  
  @Test
  public void testReceiveStart() throws Exception {
    LifecycleHelper.initAndStart(consumer);
    
    verify(mockMessageConsumer).start();
  }
  
  @Test
  public void testReceiveStartFails() throws Exception {
    doThrow(new JCSMPException("Expected"))
        .when(mockConnection).createSession();
    
    try {
      LifecycleHelper.initAndStart(consumer);
      fail("Should throw an error on start.");
    } catch (CoreException ex) {
      //expected
    }
  }
  
  @Test
  public void testOnReceiveSuccess() throws Exception {
    LifecycleHelper.initAndStart(consumer);
    
    consumer.onReceive(mockBytesMessage);
    
    verify(mockTranslator).translate(mockBytesMessage);
    verify(mockMessageListener).onAdaptrisMessage(any(AdaptrisMessage.class));
  }
  
  @Test
  public void testOnReceiveTranslatorFails() throws Exception {
    doThrow(new Exception("Expected"))
        .when(mockTranslator).translate(mockBytesMessage);
    
    LifecycleHelper.initAndStart(consumer);
    
    consumer.onReceive(mockBytesMessage);
    
    verify(mockTranslator).translate(mockBytesMessage);
    verify(mockMessageListener, times(0)).onAdaptrisMessage(any(AdaptrisMessage.class));
  }
  
  @Test
  public void testOnExceptionTriggersErrorHandler() throws Exception {
    LifecycleHelper.initAndStart(consumer);
    
    consumer.onException(new JCSMPException("Expected"));
    
    verify(mockConnectionErrorHandler).handleConnectionException();
  }
}
