package com.adaptris.core.jms.solace;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
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

public class SolaceConnectionStateHandlerTest extends MockBaseTest {

  private SolaceConnectionStateHandler stateHandler;

  @Mock
  private JmsConnection jmsConnection;
  @Mock
  private Connection genericJmsConnection;
  @Mock
  private SolConnection solConnection;
  @Mock
  private SolConnectionEvent event;

  @BeforeEach
  public void setUp() throws Exception {
    stateHandler = new SolaceConnectionStateHandler();
    stateHandler.registerConnection(jmsConnection);
    when(jmsConnection.currentConnection()).thenReturn(genericJmsConnection);
  }

  @Test
  public void testInitStopCloseNoOp() {
    assertDoesNotThrow(() -> stateHandler.init());
    assertDoesNotThrow(() -> stateHandler.stop());
    assertDoesNotThrow(() -> stateHandler.close());
  }

  @Test
  public void testStartRegistersListenerWhenConnectionIsSolaceConnection() {
    when(jmsConnection.currentConnection()).thenReturn(solConnection);

    assertDoesNotThrow(() -> stateHandler.start());

    verify(solConnection).setConnectionEventListener(stateHandler);
  }

  @Test
  public void testStartIgnoresNonSolaceConnection() {
    assertDoesNotThrow(() -> stateHandler.start());

    verifyNoInteractions(solConnection);
  }

  @Test
  public void testStartSwallowsFailuresDuringListenerRegistration() {
    when(jmsConnection.currentConnection()).thenThrow(new RuntimeException("boom"));

    assertDoesNotThrow(() -> stateHandler.start());
  }

  @Test
  public void testOnEventReconnectingSetsStoppedState() {
    when(event.getType()).thenReturn(EventType.RECONNECTING);

    stateHandler.onEvent(event);

    verify(jmsConnection).changeState(StoppedState.getInstance());
  }

  @Test
  public void testOnEventReconnectedSetsStartedState() {
    when(event.getType()).thenReturn(EventType.RECONNECTED);

    stateHandler.onEvent(event);

    verify(jmsConnection).changeState(StartedState.getInstance());
  }

  @Test
  public void testOnEventUnhandledTypeDoesNotChangeState() {
    when(event.getType()).thenReturn(null);

    stateHandler.onEvent(event);

    verify(jmsConnection, never()).changeState(any());
  }

  @Test
  public void testOnEventNullEventThrowsNullPointerException() {
    assertThrows(NullPointerException.class, () -> stateHandler.onEvent(null));
  }
}

