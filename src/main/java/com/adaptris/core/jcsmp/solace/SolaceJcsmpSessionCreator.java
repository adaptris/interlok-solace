package com.adaptris.core.jcsmp.solace;

import com.solacesystems.jcsmp.Session;

public interface SolaceJcsmpSessionCreator {

  public Session createSession() throws Exception;
  
}
