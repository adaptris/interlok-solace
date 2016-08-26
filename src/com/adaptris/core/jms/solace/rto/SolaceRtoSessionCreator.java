package com.adaptris.core.jms.solace.rto;

import com.solacesystems.solclientj.core.SolclientException;
import com.solacesystems.solclientj.core.event.MessageCallback;
import com.solacesystems.solclientj.core.handle.SessionHandle;

public interface SolaceRtoSessionCreator {
  
  SessionHandle createSession(MessageCallback consumer) throws SolclientException;

}
