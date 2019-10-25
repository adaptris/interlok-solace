package com.adaptris.core.jcsmp.solace;

import com.solacesystems.jcsmp.XMLMessageListener;

public interface SolaceJcsmpReceiverStarter extends XMLMessageListener {
  
  public void startReceive() throws Exception;

}
