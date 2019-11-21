package com.adaptris.core.jcsmp.solace;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.Workflow;
import com.adaptris.core.util.LifecycleHelper;
import com.solacesystems.jcsmp.BytesXMLMessage;

public class SolaceJcsmpInterceptorMessageAckerTest {
  
  private SolaceJcsmpInterceptorMessageAcker messageAcker;
  
  private Channel mockChannel;
  
  private SolaceJcsmpWorkflow workflow;
  
  private AdaptrisMessage adaptrisMessage;
  
  @Mock BytesXMLMessage mockMessage;
    
  @Mock Workflow mockWorkflow;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    workflow = new SolaceJcsmpWorkflow();
    
    messageAcker = new SolaceJcsmpInterceptorMessageAcker();
    messageAcker.setUniqueId("message-acker-id");
    messageAcker.registerParentChannel(mockChannel);
    messageAcker.registerParentWorkflow(mockWorkflow);
    messageAcker.setParentWorkflow(workflow);
    
    workflow.setMessageAcker(messageAcker);
    
    mockChannel = new Channel();
    mockChannel.getWorkflowList().add(workflow);
    
    adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage();
    
    LifecycleHelper.initAndStart(messageAcker);
  }
  
  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(messageAcker);
  }

  @Test
  public void testPrepareInterceptorSetUp() throws Exception {
    LifecycleHelper.prepare(mockChannel);
    
    assertEquals("message-acker-id", workflow.getInterceptors().get(0).getUniqueId());
  }
  
  @Test
  public void testWorkflowEndSuccessMessage() throws Exception {
    messageAcker.addUnacknowledgedMessage(mockMessage, adaptrisMessage.getUniqueId());
    
    assertEquals(1, messageAcker.getUnacknowledgedMessageCount());
    
    messageAcker.workflowStart(adaptrisMessage);
    messageAcker.workflowEnd(adaptrisMessage, adaptrisMessage);
    
    assertEquals(0, messageAcker.getUnacknowledgedMessageCount());
    
    verify(mockMessage).ackMessage();
  }
  
  @Test
  public void testWorkflowEndFailedMessage() throws Exception {
    adaptrisMessage.getObjectHeaders().put(CoreConstants.OBJ_METADATA_EXCEPTION, new Exception("expected"));
    
    messageAcker.addUnacknowledgedMessage(mockMessage, adaptrisMessage.getUniqueId());
    
    assertEquals(1, messageAcker.getUnacknowledgedMessageCount());
    
    messageAcker.workflowStart(adaptrisMessage);
    messageAcker.workflowEnd(adaptrisMessage, adaptrisMessage);
    
    assertEquals(1, messageAcker.getUnacknowledgedMessageCount());
    
    verify(mockMessage, times(0)).ackMessage();
  }
  
  @Test
  public void testWorkflowEndNoMessageExists() throws Exception {
    assertEquals(0, messageAcker.getUnacknowledgedMessageCount());
    
    messageAcker.workflowStart(adaptrisMessage);
    messageAcker.workflowEnd(adaptrisMessage, adaptrisMessage);
    
    assertEquals(0, messageAcker.getUnacknowledgedMessageCount());
    
    verify(mockMessage, times(0)).ackMessage();
  }
}
