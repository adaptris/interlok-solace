package com.adaptris.core.jcsmp.solace;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

public class SolaceJcsmpLoggingProducerEventHandlerTest {

  private SolaceJcsmpLoggingProducerEventHandler eventHandler;
  
  @Mock private Logger mockLogger; 
  
  @Mock private SolaceJcsmpQueueProducer mockProducer;
  
  @Mock private SolaceJcsmpConnection mockConnection;
  
  @Mock private SolaceJcsmpConnectionErrorHandler mockCeh;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    eventHandler = new SolaceJcsmpLoggingProducerEventHandler();
    eventHandler.setLog(mockLogger);
  }
  
  @After
  public void tearDown() throws Exception {
    
  }
  
  @Test
  public void testCallbackSuccess() throws Exception {
    eventHandler.responseReceivedEx("x");
    
    verify(mockLogger).debug(any(String.class), any(Object.class));
  }
  
  @Test
  public void testCallbackFailure() throws Exception {
    eventHandler.handleErrorEx("x", null, 1L);
    
    verify(mockLogger).error(any(String.class), any(Object.class), any(Exception.class));
  }
  
  @Test
  public void testCallbackFailureErrorHandler() throws Exception {
    when(mockProducer.retrieveConnection(SolaceJcsmpConnection.class))
        .thenReturn(mockConnection);
    when(mockConnection.getConnectionErrorHandler())
        .thenReturn(mockCeh);
    eventHandler.setProducer(mockProducer);
    eventHandler.setTriggerErrorHandlerOnFailure(true);
    eventHandler.handleErrorEx("x", null, 1L);
    
    verify(mockLogger).error(any(String.class), any(Object.class), any(Exception.class));
    verify(mockCeh).handleConnectionException();
  }
  
  @Test
  public void neverCalledButNecessaryOverride() throws Exception {
    eventHandler.responseReceived("x");
    eventHandler.handleError("x", null, 1L);
  }
  
}
