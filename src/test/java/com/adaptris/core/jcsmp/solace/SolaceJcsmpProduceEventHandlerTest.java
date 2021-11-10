package com.adaptris.core.jcsmp.solace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.function.Consumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MockBaseTest;
import com.adaptris.core.util.LifecycleHelper;
import com.solacesystems.jcsmp.JCSMPException;

public class SolaceJcsmpProduceEventHandlerTest extends MockBaseTest {

  private SolaceJcsmpProduceEventHandler eventHandler;

  private AdaptrisMessage message, message2, message3;

  @Mock private SolaceJcsmpAbstractProducer mockProducer;
  @Mock private SolaceJcsmpConnection mockConnection;
  @Mock private SolaceJcsmpConnectionErrorHandler mockErrorHandler;

  @Before
  public void setUp() throws Exception {
    message = DefaultMessageFactory.getDefaultInstance().newMessage();
    message2 = DefaultMessageFactory.getDefaultInstance().newMessage();
    message3 = DefaultMessageFactory.getDefaultInstance().newMessage();

    eventHandler = new SolaceJcsmpProduceEventHandler(mockProducer);
    LifecycleHelper.init(eventHandler);

    when(mockProducer.retrieveConnection(SolaceJcsmpConnection.class))
    .thenReturn(mockConnection);

    when(mockConnection.getConnectionErrorHandler())
    .thenReturn(mockErrorHandler);
  }

  @Test
  public void testHandleError() throws Exception {
    Consumer<AdaptrisMessage> successCallback = message -> { fail("Success callback called."); };
    Consumer<AdaptrisMessage> failureCallback = message -> { };

    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK, successCallback);
    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK, failureCallback);

    eventHandler.addUnAckedMessage(message);

    eventHandler.handleErrorEx(message.getUniqueId(), new JCSMPException("Expected"), 1l);

    assertEquals(0, eventHandler.getUnAckedMessages().size());

    verify(mockErrorHandler).handleConnectionException();
  }

  @Test
  public void testHandleErrorWithUnknownMessage() throws Exception {
    eventHandler.handleErrorEx(message.getUniqueId(), new JCSMPException("Expected"), 1l);

    assertEquals(0, eventHandler.getUnAckedMessages().size());
  }

  @Test
  public void testHandleSuccess() throws Exception {
    Consumer<AdaptrisMessage> successCallback = message -> { };
    Consumer<AdaptrisMessage> failureCallback = message -> { fail("Failure callback called."); };

    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK, successCallback);
    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK, failureCallback);

    eventHandler.addUnAckedMessage(message);

    eventHandler.responseReceivedEx(message.getUniqueId());

    assertEquals(0, eventHandler.getUnAckedMessages().size());
  }

  @Test
  public void testDoNotHandleSuccessAfterFailure() throws Exception {
    Consumer<AdaptrisMessage> successCallback = message -> { fail("Failure callback called."); };
    Consumer<AdaptrisMessage> failureCallback = message -> { };

    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK, successCallback);
    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK, failureCallback);
    
    message2.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK, successCallback);
    message2.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK, failureCallback);

    eventHandler.addUnAckedMessage(message);
    eventHandler.addUnAckedMessage(message2);

    eventHandler.handleErrorEx(message.getUniqueId(), new JCSMPException("Expected"), 1l);
    eventHandler.responseReceivedEx(message2.getUniqueId()); //  should not fire success callback
  }

  @Test
  public void testHandleSuccessWithUnknownMessage() throws Exception {
    eventHandler.responseReceivedEx(message.getUniqueId());

    assertEquals(0, eventHandler.getUnAckedMessages().size());
  }

  @Test
  public void testHandleMultipleSuccess() throws Exception {
    Consumer<AdaptrisMessage> successCallback = message -> { };
    Consumer<AdaptrisMessage> failureCallback = message -> { fail("Failure callback called."); };

    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK, successCallback);
    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK, failureCallback);
    
    message2.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK, successCallback);
    message2.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK, failureCallback);

    message2.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK, successCallback);
    message3.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK, failureCallback);
    
    eventHandler.addUnAckedMessage(message);
    eventHandler.addUnAckedMessage(message2);
    eventHandler.addUnAckedMessage(message3);

    eventHandler.responseReceivedEx(message.getUniqueId());
    assertEquals(2, eventHandler.getUnAckedMessages().size());

    eventHandler.responseReceivedEx(message2.getUniqueId());
    assertEquals(1, eventHandler.getUnAckedMessages().size());

    eventHandler.responseReceivedEx(message3.getUniqueId());
    assertEquals(0, eventHandler.getUnAckedMessages().size());
  }

}
