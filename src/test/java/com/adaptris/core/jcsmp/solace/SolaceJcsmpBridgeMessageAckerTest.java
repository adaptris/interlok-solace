package com.adaptris.core.jcsmp.solace;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.CoreException;
import com.adaptris.core.util.LifecycleHelper;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;

public class SolaceJcsmpBridgeMessageAckerTest {
  
  private SolaceJcsmpBridgeMessageAcker messageAcker;
  
  private SolaceJcsmpWorkflow workflow;
    
  @Mock private BytesXMLMessage mockMessage;
  
  @Mock private SolaceJcsmpQueueProducer mockProducer;
  
  @Mock private AdaptrisMessageProducer mockAdpProducer;
  
  @Mock private SolaceJcsmpConnection mockConnection;
  
  @Mock private SolaceJcsmpConnectionErrorHandler mockCeh;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    messageAcker = new SolaceJcsmpBridgeMessageAcker();
    
    workflow = new SolaceJcsmpWorkflow();
  }
  
  @After
  public void tearDown() throws Exception {
    
  }
  @Test
  public void testPrepareSetsUpProducerEventHandler() throws Exception {
    workflow.setMessageAcker(messageAcker);
    workflow.setProducer(mockProducer);
    
    LifecycleHelper.prepare(workflow);
    
    verify(mockProducer).setProducerEventHandler(messageAcker);
  }
  
  @Test
  public void testPrepareSetsUpProducerEventHandlerWrongProducerType() throws Exception {
    workflow.setMessageAcker(messageAcker);
    workflow.setProducer(mockAdpProducer);
    
    try {
      LifecycleHelper.prepare(workflow);
      fail("Should fail with wrong producer type");
    } catch (CoreException ex) {
      // expected
    }
    
  }
  
  @Test
  public void testResponseReceived() throws Exception {
    messageAcker.addUnacknowledgedMessage(mockMessage, "id");
    
    messageAcker.responseReceivedEx("id");
    
    verify(mockMessage).ackMessage();
  }

  @Test
  public void testResponseReceivednoMessageFound() throws Exception {
    messageAcker.addUnacknowledgedMessage(mockMessage, "id");
    
    messageAcker.responseReceivedEx("xxx");
    
    verify(mockMessage, times(0)).ackMessage();
  }
  
  @Test
  public void testErrorResponseReceived() throws Exception {
    messageAcker.addUnacknowledgedMessage(mockMessage, "id");
    
    messageAcker.handleErrorEx("id", new JCSMPException("expected"), 1L);
    
    verify(mockMessage, times(0)).ackMessage();
  }
  
  @Test
  public void testCallbackFailureErrorHandler() throws Exception {
    when(mockProducer.retrieveConnection(SolaceJcsmpConnection.class))
        .thenReturn(mockConnection);
    when(mockConnection.getConnectionErrorHandler())
        .thenReturn(mockCeh);
    messageAcker.setProducer(mockProducer);
    messageAcker.setTriggerErrorHandlerOnFailure(true);
    messageAcker.handleErrorEx("x", null, 1L);
    
    verify(mockCeh).handleConnectionException();
  }
  
  @Test
  public void neverCalledButNecessaryOverride() throws Exception {
    messageAcker.responseReceived("x");
    messageAcker.handleError("x", null, 1L);
  }
  
}
