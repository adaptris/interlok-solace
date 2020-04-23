package com.adaptris.core.jcsmp.solace;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.solacesystems.jcsmp.JCSMPException;

public class SolaceJcsmpProduceEventHandlerTest {
  
  private SolaceJcsmpProduceEventHandler eventHandler;
  
  private AdaptrisMessage message;
  
  @Before
  public void setUp() throws Exception {
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
    
    eventHandler = new SolaceJcsmpProduceEventHandler(message);
  }
  
  @After
  public void tearDown() throws Exception {
    
  }
  
  @Test
  public void testHandleError() throws Exception {
    eventHandler.handleError("SomeMessageId", new JCSMPException("Expected"), 0l);
    
    assertNotNull(message.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION));
  }
  
  public void testHandleSuccess() throws Exception {
    CountDownLatch countDownLatch = new CountDownLatch(1);
    message.getObjectHeaders().put(SolaceJcsmpProduceEventHandler.SOLACE_LATCH_KEY, countDownLatch);
    
    eventHandler.responseReceived("SomeMessageId");
    
    if(!countDownLatch.await(1, TimeUnit.SECONDS))
      fail("Success handler should clear the latch.");
    
    assertNull(message.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION));
  }
  
  public void testHandleSuccessNoLatch() throws Exception {    
    eventHandler.responseReceived("SomeMessageId");
    
    assertNotNull(message.getObjectHeaders().get(CoreConstants.OBJ_METADATA_EXCEPTION));
  }

}
