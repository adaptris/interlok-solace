package com.adaptris.core.jcsmp.solace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;
import com.solacesystems.jcsmp.XMLMessageProducer;
import com.solacesystems.jcsmp.transaction.TransactedSession;
import com.solacesystems.jcsmp.transaction.TransactionStatus;

import lombok.Getter;
import lombok.Setter;

public class SolaceJcsmpSessionHelper {

  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());

  @Getter
  @Setter
  private boolean transacted;

  @Getter
  @Setter
  private TransactedSession transactedSession;

  @Getter
  @Setter
  private JCSMPSession session;

  @Getter
  @Setter
  private SolaceJcsmpConnection connection;

  public SolaceJcsmpSessionHelper() {
  }

  private void createStandardSession() throws Exception {
    setSession(getConnection().createSession());
  }

  private boolean isStandardSessionActive() {
    return getSession() != null && !getSession().isClosed();
  }

  private boolean isTransactedSessionActive() {
    return getTransactedSession() != null && !(getTransactedSession().getStatus() == TransactionStatus.CLOSED);
  }

  private void createTransactedSession() throws Exception {
    if (!isStandardSessionActive()) {
      createStandardSession();
    }
    setTransactedSession(getSession().createTransactedSession());
  }

  boolean isSessionActive() {
    return getTransacted() ? isTransactedSessionActive() : isStandardSessionActive();
  }

  XMLMessageProducer createMessageProducer(JCSMPStreamingPublishCorrelatingEventHandler eventHandler) throws JCSMPException {
    return getSession().getMessageProducer(eventHandler);
  }

  void createSession() throws Exception {
    if (getTransacted()) {
      createTransactedSession();
    } else {
      createStandardSession();
    }
  }

  void commit(BytesXMLMessage message) {
    if (getTransacted()) {
      try {
        getTransactedSession().commit();
        log.trace("Committing transaction.");
      } catch (Exception e) {
        log.error("Commit failed, rolling back.", e);
        rollback();
      }
    } else {
      message.ackMessage();
    }
  }

  void rollback() {
    if (getTransacted()) {
      try {
        getTransactedSession().rollback();
      } catch (JCSMPException e) {
        log.error("Rollback failed.", e);
      }

    }
  }

  void close() {
    if (getTransacted()) {
      if (isTransactedSessionActive()) {
        getTransactedSession().close();
      }
    } else if (isStandardSessionActive()) {
      getSession().closeSession();
    }
  }

}
