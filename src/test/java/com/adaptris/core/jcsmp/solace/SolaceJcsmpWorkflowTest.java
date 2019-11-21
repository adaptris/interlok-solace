package com.adaptris.core.jcsmp.solace;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.AdaptrisMessageProducer;
import com.adaptris.core.Channel;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.ProcessingExceptionHandler;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceCollection;
import com.adaptris.core.ServiceException;
import com.adaptris.core.util.LifecycleHelper;

public class SolaceJcsmpWorkflowTest {

  private Channel mockChannel;
  
  private SolaceJcsmpWorkflow workflow;
  
  private AdaptrisMessage message; 
  
  @Mock private AdaptrisMessageConsumer mockConsumer;
  
  @Mock private SolaceJcsmpQueueConsumer mockJcsmpConsumer;
  
  @Mock private AdaptrisMessageProducer mockProducer;
  
  @Mock private SolaceJcsmpMessageAcker mockMessageAcker;
  
  @Mock private ProcessingExceptionHandler mockErrorHandler;
  
  @Mock private ServiceCollection mockServiceCollection;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    mockChannel = new Channel();
    
    workflow = new SolaceJcsmpWorkflow();
    workflow.setConsumer(mockConsumer);
    workflow.setProducer(mockProducer);
    workflow.setMessageAcker(mockMessageAcker);
    workflow.setMessageErrorHandler(mockErrorHandler);
    workflow.setServiceCollection(mockServiceCollection);
    
    workflow.registerActiveMsgErrorHandler(mockErrorHandler);
    
    mockChannel.getWorkflowList().add(workflow);
    
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
    
    when(mockConsumer.isTrackingEndpoint())
        .thenReturn(false);
    when(mockConsumer.createName())
        .thenReturn("name");
    when(mockProducer.isTrackingEndpoint())
        .thenReturn(false);
    when(mockProducer.createName())
        .thenReturn("name");
  }
  
  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(mockChannel);
  }
  
  @Test
  public void testPrepareWithJcsmpConsumer() throws Exception {
    workflow.setConsumer(mockJcsmpConsumer);
    LifecycleHelper.prepare(mockChannel);
    
    verify(mockJcsmpConsumer).prepare();
    verify(mockProducer).prepare();
    verify(mockMessageAcker).prepare();
    verify(mockJcsmpConsumer).setMessageAcker(mockMessageAcker);
  }
  
  @Test
  public void testInit() throws Exception {
    LifecycleHelper.init(mockChannel);
    
    verify(mockConsumer).init();
    verify(mockProducer).init();
    verify(mockMessageAcker).init();
  }
  
  @Test
  public void testStart() throws Exception {
    LifecycleHelper.start(mockChannel);
    
    verify(mockConsumer).start();
    verify(mockProducer).start();
    verify(mockMessageAcker).start();
  }
  
  @Test
  public void testStop() throws Exception {
    LifecycleHelper.initAndStart(mockChannel);
    LifecycleHelper.stop(mockChannel);
    
    verify(mockConsumer, atLeast(1)).stop();
    verify(mockProducer, atLeast(1)).stop();
    verify(mockMessageAcker, atLeast(1)).stop();
  }
  
  @Test
  public void testClose() throws Exception {
    LifecycleHelper.initAndStart(mockChannel);
    LifecycleHelper.stop(mockChannel);
    LifecycleHelper.close(mockChannel);
    
    verify(mockConsumer, atLeast(1)).close();
    verify(mockProducer, atLeast(1)).close();
    verify(mockMessageAcker, atLeast(1)).close();
  }
  
  @Test
  public void testHandleMessage() throws Exception {
    LifecycleHelper.initAndStart(mockChannel);
    
    workflow.handleMessage(message, false);
    
    verify(mockProducer).produce(any());
  }
  
  @Test
  public void testHandleMessageServiceFails() throws Exception {
    doThrow(new ServiceException("Expected"))
        .when(mockServiceCollection).doService(message);
    
    LifecycleHelper.initAndStart(mockChannel);

    workflow.handleMessage(message, false);

    verify(mockErrorHandler).handleProcessingException(message);
  }
  
  @Test
  public void testHandleMessageProducerFails() throws Exception {
    doThrow(new ProduceException("Expected"))
        .when(mockProducer).produce(any());
    
    LifecycleHelper.initAndStart(mockChannel);

    workflow.handleMessage(message, false);

    verify(mockErrorHandler).handleProcessingException(message);
  }
  
  @Test
  public void testHandleMessageUnexpectedException() throws Exception {
    doThrow(new RuntimeException("Expected"))
        .when(mockProducer).produce(any());
    
    LifecycleHelper.initAndStart(mockChannel);

    workflow.handleMessage(message, false);

    verify(mockErrorHandler).handleProcessingException(message);
  }
  
}
