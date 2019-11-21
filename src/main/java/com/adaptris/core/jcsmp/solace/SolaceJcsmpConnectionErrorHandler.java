package com.adaptris.core.jcsmp.solace;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.ConnectionErrorHandler;
import com.adaptris.core.ConnectionErrorHandlerImp;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * The Interlok {@link ConnectionErrorHandler} implementation specifically for {@link SolaceJcsmpConnection} 
 *  </p>
 *  <p>
 *  Should a connection be disconnected or report an error, this handler will fire, attempting to restart everything.
 *  </p>
 * @author aaron
 * @version=”3.9.3”
 * @config solace-jcsmp-connection-error-handler
 * 
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP connection error handler, used to restart affected components on error.", tag="ceh,connection,solace,jcsmp")
@XStreamAlias("solace-jcsmp-connection-error-handler")
public class SolaceJcsmpConnectionErrorHandler extends ConnectionErrorHandlerImp {

  @Override
  public void handleConnectionException() {
    super.restartAffectedComponents();
  }

}
