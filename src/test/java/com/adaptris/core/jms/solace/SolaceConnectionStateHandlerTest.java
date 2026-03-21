package com.adaptris.core.jms.solace;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import javax.jms.Connection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.adaptris.core.MockBaseTest;
import com.adaptris.core.StartedState;
import com.adaptris.core.StoppedState;
import com.adaptris.core.jms.JmsConnection;
import com.solacesystems.jms.SolConnection;
import com.solacesystems.jms.events.SolConnectionEvent;
import com.solacesystems.jms.events.SolConnectionEvent.EventType;

class SolaceConnectionStateHandlerTest extends MockBaseTest {

  private SolaceConnectionStateHandler stateHandler;

  @Mock private JmsConnection jmsConnection;
  @Mock private Connection genericJmsConnection;
  @Mock private SolConnection solConnection;
  @Mock private SolConnectionEvent event;

  @BeforeEach
  void setUp() {
    stateHandler = new SolaceConnectionStateHandler();
    stateHandler.registerConnection(jmsConnection);
    when(jmsConnection.currentConnection()).thenReturn(genericJmsConnection);
  }

  @Test
  void testInitNoOp() {
    assertDoesNotThrow(() -> stateHandler.init());
  }

  @Test
  void testStopDeregistersListenerWhenConnectionIsSolaceConnection() {
    when(jmsConnection.currentConnection()).thenReturn(solConnection);

    stateHandler.stop();

    verify(solConnection).setConnectionEventListener(null);
  }

  @Test
  void testStopIgnoresNonSolaceConnection() {
    stateHandler.stop();

    verifyNoInteractions(solConnection);
  }

  @Test
  void testStopSwallowsExceptions() {
    when(jmsConnection.currentConnection()).thenThrow(new RuntimeException("boom"));

    assertDoesNotThrow(() -> stateHandler.stop());
  }

  @Test
  void testCloseDeregistersListenerWhenConnectionIsSolaceConnection() {
    when(jmsConnection.currentConnection()).thenReturn(solConnection);

    stateHandler.close();

    verify(solConnection).setConnectionEventListener(null);
  }

  @Test
  void testCloseIgnoresNonSolaceConnection() {
    stateHandler.close();

    verifyNoInteractions(solConnection);
  }

  @Test
  void testCloseSwallowsExceptions() {
    when(jmsConnection.currentConnection()).thenThrow(new RuntimeException("boom"));

    assertDoesNotThrow(() -> stateHandler.close());
  }

  @Test
  void testStartRegistersListenerWhenConnectionIsSolaceConnection() {
    when(jmsConnection.currentConnection()).thenReturn(solConnection);

    assertDoesNotThrow(() -> stateHandler.start());

    verify(solConnection).setConnectionEventListener(stateHandler);
  }

  @Test
  void testStartIgnoresNonSolaceConnection() {
    assertDoesNotThrow(() -> stateHandler.start());

    verifyNoInteractions(solConnection);
  }

  @Test
  void testStartSwallowsFailuresDuringListenerRegistration() {
    when(jmsConnection.currentConnection()).thenThrow(new RuntimeException("boom"));

    assertDoesNotThrow(() -> stateHandler.start());
  }

  @Test
  void testOnEventReconnectingSetsStoppedState() {
    when(event.getType()).thenReturn(EventType.RECONNECTING);

    stateHandler.onEvent(event);

    verify(jmsConnection).changeState(StoppedState.getInstance());
  }

  @Test
  void testOnEventReconnectedSetsStartedState() {
    when(event.getType()).thenReturn(EventType.RECONNECTED);

    stateHandler.onEvent(event);

    verify(jmsConnection).changeState(StartedState.getInstance());
  }

  @Test
  void testOnEventUnhandledTypeDoesNotChangeState() {
    when(event.getType()).thenReturn(null);

    stateHandler.onEvent(event);

    verify(jmsConnection, never()).changeState(any());
  }
}
