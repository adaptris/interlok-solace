package com.adaptris.core.jcsmp.solace;

import com.adaptris.core.ConnectionErrorHandlerImp;

public class SolaceJcsmpConnectionErrorHandler extends ConnectionErrorHandlerImp {

  @Override
  public void handleConnectionException() {
    super.restartAffectedComponents();
  }

}
