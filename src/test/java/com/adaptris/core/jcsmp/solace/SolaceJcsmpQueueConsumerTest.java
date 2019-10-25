package com.adaptris.core.jcsmp.solace;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageListener;
import com.adaptris.core.ConnectionErrorHandler;
import com.adaptris.core.util.LifecycleHelper;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.FlowReceiver;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Queue;
import com.solacesystems.jcsmp.XMLMessageListener;

public class SolaceJcsmpQueueConsumerTest {
  
  private SolaceJcsmpQueueConsumer consumer;
  
  @Mock private SolaceJcsmpConnection mockConnection;
  
  @Mock private JCSMPSession mockSession;
  
  @Mock private JCSMPFactory mockJcsmpFactory;
  
  @Mock private Queue mockQueue;

  @Mock private FlowReceiver mockFlowReceiver;
  
  @Mock private BytesXMLMessage mockBytesMessage;
  
  @Mock private SolaceJcsmpMessageTranslator mockTranslator;
  
  @Mock private AdaptrisMessageListener mockMessageListener;
  
  @Mock private AdaptrisMessage mockAdpMessage;
  
  @Mock private ConnectionErrorHandler mockConnectionErrorHandler;
  
  @Mock private SolaceJcsmpMessageAcker mockMessageAcker;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    consumer = new SolaceJcsmpQueueConsumer();
    consumer.registerConnection(mockConnection);
    consumer.setJcsmpFactory(mockJcsmpFactory);
    consumer.setQueueName("myQueue");
    consumer.setMaxThreads(1);
    consumer.setMessageTranslator(mockTranslator);
    consumer.registerAdaptrisMessageListener(mockMessageListener);
    consumer.setMessageAcker(mockMessageAcker);
    
    when(mockConnection.createSession())
        .thenReturn(mockSession);
    when(mockConnection.retrieveConnection(SolaceJcsmpConnection.class))
        .thenReturn(mockConnection);
    when(mockSession.createFlow(any(XMLMessageListener.class), any(), any()))
        .thenReturn(mockFlowReceiver);
    when(mockJcsmpFactory.createQueue(any(String.class)))
        .thenReturn(mockQueue);
    when(mockTranslator.translate(mockBytesMessage))
        .thenReturn(mockAdpMessage);
    when(mockConnection.getConnectionErrorHandler())
        .thenReturn(mockConnectionErrorHandler);
    
    LifecycleHelper.initAndStart(consumer);
  }
  
  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(consumer);
  }
  
  @Test
  public void testReceiveStart() throws Exception {
    consumer.startReceive();
    
    verify(mockFlowReceiver).start();
  }
  
  @Test
  public void testOnReceiveSuccess() throws Exception {
    consumer.onReceive(mockBytesMessage);
    
    LifecycleHelper.stopAndClose(consumer);
    consumer.getExecutorService().awaitTermination(5L, TimeUnit.SECONDS);
    
    verify(mockTranslator).translate(mockBytesMessage);
    verify(mockMessageListener).onAdaptrisMessage(any(AdaptrisMessage.class));
  }
  
  @Test
  public void testOnReceiveTranslatorFails() throws Exception {
    doThrow(new Exception("Expected"))
        .when(mockTranslator).translate(mockBytesMessage);
    
    consumer.onReceive(mockBytesMessage);
    
    LifecycleHelper.stopAndClose(consumer);
    consumer.getExecutorService().awaitTermination(5L, TimeUnit.SECONDS);
    
    verify(mockTranslator).translate(mockBytesMessage);
    verify(mockMessageListener, times(0)).onAdaptrisMessage(any(AdaptrisMessage.class));
  }
  
  @Test
  public void testOnExceptionTriggersErrorHandler() throws Exception {
    consumer.onException(new JCSMPException("Expected"));
    
    verify(mockConnectionErrorHandler).handleConnectionException();
  }

}
