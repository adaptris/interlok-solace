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

  private AdaptrisMessage message;

  @Mock private SolaceJcsmpAbstractProducer mockProducer;
  @Mock private SolaceJcsmpConnection mockConnection;
  @Mock private SolaceJcsmpConnectionErrorHandler mockErrorHandler;

  @Before
  public void setUp() throws Exception {
    message = DefaultMessageFactory.getDefaultInstance().newMessage();

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

    eventHandler.addUnAckedMessage("messageId", message);

    eventHandler.handleError("messageId", new JCSMPException("Expected"), 1l);

    assertEquals(0, eventHandler.getUnAckedMessages().size());

    verify(mockErrorHandler).handleConnectionException();
  }

  @Test
  public void testHandleErrorWithUnknownMessage() throws Exception {
    eventHandler.handleError("messageId", new JCSMPException("Expected"), 1l);

    assertEquals(0, eventHandler.getUnAckedMessages().size());
  }

  @Test
  public void testHandleSuccess() throws Exception {
    Consumer<AdaptrisMessage> successCallback = message -> { };
    Consumer<AdaptrisMessage> failureCallback = message -> { fail("Failure callback called."); };

    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK, successCallback);
    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK, failureCallback);

    eventHandler.addUnAckedMessage("messageId", message);

    eventHandler.responseReceived("messageId");

    assertEquals(0, eventHandler.getUnAckedMessages().size());
  }

  @Test
  public void testDoNotHandleSuccessAfterFailure() throws Exception {
    Consumer<AdaptrisMessage> successCallback = message -> { fail("Failure callback called."); };
    Consumer<AdaptrisMessage> failureCallback = message -> { };

    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK, successCallback);
    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK, failureCallback);

    eventHandler.addUnAckedMessage("messageId", message);
    eventHandler.addUnAckedMessage("messageId2", message);

    eventHandler.handleError("messageId", new JCSMPException("Expected"), 1l);
    eventHandler.responseReceived("messageId"); //  should not fire success callback
  }

  @Test
  public void testHandleSuccessWithUnknownMessage() throws Exception {
    eventHandler.responseReceived("messageId");

    assertEquals(0, eventHandler.getUnAckedMessages().size());
  }

  @Test
  public void testHandleMultipleSuccess() throws Exception {
    Consumer<AdaptrisMessage> successCallback = message -> { };
    Consumer<AdaptrisMessage> failureCallback = message -> { fail("Failure callback called."); };

    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK, successCallback);
    message.getObjectHeaders().put(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK, failureCallback);

    eventHandler.addUnAckedMessage("messageId", message);
    eventHandler.addUnAckedMessage("messageId2", message);
    eventHandler.addUnAckedMessage("messageId3", message);

    eventHandler.responseReceived("messageId");
    assertEquals(2, eventHandler.getUnAckedMessages().size());

    eventHandler.responseReceived("messageId2");
    assertEquals(1, eventHandler.getUnAckedMessages().size());

    eventHandler.responseReceived("messageId3");
    assertEquals(0, eventHandler.getUnAckedMessages().size());
  }

}
