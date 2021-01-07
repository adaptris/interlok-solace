package com.adaptris.core.jms.solace;

import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreException;
import com.adaptris.core.MockBaseTest;
import com.adaptris.core.StateManagedComponent;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConsumerImpl;
import com.solacesystems.jms.SolMessageConsumer;
import com.solacesystems.jms.events.ActiveFlowIndicationEvent;
import com.solacesystems.jms.events.SolConsumerEvent;

public class SolaceConsumerConnectionErrorHandlerTest extends MockBaseTest {

  private SolaceConsumerConnectionErrorHandler errorHandler;

  @Mock private Channel mockChannel;
  @Mock private MessageConsumer mockMessageConsumer;
  @Mock private SolMessageConsumer mockSolMessageConsumer;
  @Mock private JmsConnection mockConnection;
  @Mock private Connection mockJmsConnection;
  @Mock private SolConsumerEvent mockEvent;

  private MockAdaptrisMessageConsumer mockAdpConsumer;
  private MessageConsumer actualMessageConsumer;

  private ActiveFlowIndicationEvent activeQueueEvent;
  private ActiveFlowIndicationEvent inactiveQueueEvent;

  @Before
  public void setUp() throws Exception {
    errorHandler = new SolaceConsumerConnectionErrorHandler();
    errorHandler.registerConnection(mockConnection);

    actualMessageConsumer = mockSolMessageConsumer;

    mockAdpConsumer = new MockAdaptrisMessageConsumer();
    mockAdpConsumer.start();

    Set<StateManagedComponent> exListeners = new HashSet<>();
    exListeners.add(mockChannel);

    Set<AdaptrisMessageConsumer> consumers = new HashSet<>();
    consumers.add(mockAdpConsumer);

    when(mockConnection.retrieveMessageConsumers())
    .thenReturn(consumers);
    when(mockConnection.currentConnection())
    .thenReturn(mockJmsConnection);
    when(mockConnection.retrieveExceptionListeners())
    .thenReturn(exListeners);


    activeQueueEvent = new ActiveFlowIndicationEvent(ActiveFlowIndicationEvent.FLOW_ACTIVE);
    inactiveQueueEvent = new ActiveFlowIndicationEvent(ActiveFlowIndicationEvent.FLOW_INACTIVE);
  }

  @Test
  public void testStartup() throws Exception {
    errorHandler.start();

    verify(mockSolMessageConsumer).setSolConsumerEventListener(any());
  }

  @Test
  public void testStartupWrongConsumerType() throws Exception {
    actualMessageConsumer = mockMessageConsumer;
    mockAdpConsumer.start();

    try {
      errorHandler.start();
      fail("Should fail becauyse the consumer is not a solace one.");
    } catch (Exception ex) {
      //expected
    }
  }

  @Test
  public void testOnEventActiveQueue() throws Exception {
    errorHandler.onEvent(activeQueueEvent);

    verify(mockConnection, times(0)).requestStop();
  }

  @Test
  public void testOnEventIncorrectEventType() throws Exception {
    errorHandler.onEvent(mockEvent);

    verify(mockConnection, times(0)).requestStop();
  }

  @Test
  public void testOnEventInactiveQueue() throws Exception {
    errorHandler.onEvent(inactiveQueueEvent);

    verify(mockConnection, times(1)).requestStop();
  }

  /*
   *
   */
  class MockAdaptrisMessageConsumer extends JmsConsumerImpl {
    @Override
    public void prepare() throws CoreException {}
    @Override
    protected MessageConsumer createConsumer() throws JMSException, CoreException {
      return actualMessageConsumer;
    }

    @Override
    protected String configuredEndpoint() {
      return "";
    }
  }
}
