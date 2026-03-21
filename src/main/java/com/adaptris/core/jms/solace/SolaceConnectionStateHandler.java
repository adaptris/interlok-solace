package com.adaptris.core.jms.solace;

import javax.jms.Connection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.ConnectionStateHandlerImp;
import com.adaptris.core.CoreException;
import com.adaptris.core.StartedState;
import com.adaptris.core.StoppedState;
import com.adaptris.core.jms.JmsConnection;
import com.solacesystems.jms.SolConnection;
import com.solacesystems.jms.SolConnectionEventListener;
import com.solacesystems.jms.events.SolConnectionEvent;
import com.solacesystems.jms.events.SolConnectionEvent.EventType;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/** A JMS connection state handler that listens for Solace connection-level events. */
@AdapterComponent
@XStreamAlias("solace-connection-state-handler")
@ComponentProfile(
    summary =
        "A JMS connection state handler that listens for Solace connection-level events and updates connection state.",
    tag = "consumer,producer,jms,state-handler,solace")
public class SolaceConnectionStateHandler extends ConnectionStateHandlerImp
    implements SolConnectionEventListener {

  private transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @Override
  public void init() throws CoreException {}

  @Override
  public void start() throws CoreException {
    try {
      JmsConnection jmsConn = retrieveConnection(JmsConnection.class);
      Connection conn = jmsConn.currentConnection();

      if (conn instanceof SolConnection solConn) {
        solConn.setConnectionEventListener(this);
        log.debug("Registered SolConnectionEventListener on Solace connection");
      } else {
        log.warn("Connection is not a SolConnection; connection-level events will not be detected");
      }
    } catch (Exception e) {
      log.warn(
          "Failed to register SolConnectionEventListener on connection; continuing without broker connection event handling",
          e);
    }
  }

  @Override
  public void stop() {
    deregisterListener();
  }

  @Override
  public void close() {
    deregisterListener();
  }

  private void deregisterListener() {
    try {
      JmsConnection jmsConn = retrieveConnection(JmsConnection.class);
      Connection conn = jmsConn.currentConnection();
      if (conn instanceof SolConnection solConnection) {
        solConnection.setConnectionEventListener(null);
        log.debug("De-registered SolConnectionEventListener from Solace connection");
      }
    } catch (Exception e) {
      log.trace("Failed to de-register SolConnectionEventListener (may already be closed)", e);
    }
  }

  @Override
  public void onEvent(SolConnectionEvent event) {
    EventType eventType = event.getType();
    JmsConnection jmsConn = retrieveConnection(JmsConnection.class);

    if (eventType == EventType.RECONNECTING) {
      log.info("Solace RECONNECTING - updating connection state");
      jmsConn.changeState(StoppedState.getInstance());
    } else if (eventType == EventType.RECONNECTED) {
      log.info("Solace RECONNECTED - updating connection state");
      jmsConn.changeState(StartedState.getInstance());
    } else {
      log.warn("Received unknown Solace connection event type: {}", eventType);
    }
  }
}
