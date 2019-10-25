package com.adaptris.core.jcsmp.solace;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.ConnectionErrorHandlerImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP connection error handler, used to restart affected components on error.", tag="ceh,connection,solace,jcsmp")
@XStreamAlias("solace-jcsmp-connection-error-handler")
public class SolaceJcsmpConnectionErrorHandler extends ConnectionErrorHandlerImp {

  @Override
  public void handleConnectionException() {
    super.restartAffectedComponents();
  }

}
