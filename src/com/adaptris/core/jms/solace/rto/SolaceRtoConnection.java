package com.adaptris.core.jms.solace.rto;

import java.util.logging.Level;

import javax.validation.constraints.NotNull;

import com.adaptris.core.AllowsRetriesConnection;
import com.adaptris.core.CoreException;
import com.solacesystems.solclientj.core.SolEnum;
import com.solacesystems.solclientj.core.Solclient;
import com.solacesystems.solclientj.core.SolclientException;
import com.solacesystems.solclientj.core.event.MessageCallback;
import com.solacesystems.solclientj.core.event.SessionEventCallback;
import com.solacesystems.solclientj.core.handle.ContextHandle;
import com.solacesystems.solclientj.core.handle.Handle;
import com.solacesystems.solclientj.core.handle.SessionHandle;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("solace-rto-connection")
public class SolaceRtoConnection extends AllowsRetriesConnection implements SessionEventCallback, SolaceRtoSessionCreator {

  @NotNull
  private String host;
  @NotNull
  private String username;
  @NotNull
  private String password;
  @NotNull
  private String vpnName;
  
  private transient ContextHandle contextHandle = Solclient.Allocator.newContextHandle();
    
  @Override
  protected void prepareConnection() throws CoreException {
    if(!Solclient.isInitilized()) { // only need to initialize once per JVM.
      if(Solclient.init(new String[0]) == SolEnum.ReturnCode.OK)
        Solclient.setLogLevel(Level.FINE);
      else
        throw new CoreException("Solace RTO engine failed to startup");
    }
  }

  @Override
  protected void initConnection() throws CoreException {
    if(Solclient.createContextForHandle(contextHandle, new String[0]) != SolEnum.ReturnCode.OK)
      throw new CoreException("Solace context could not be created");
  }

  @Override
  protected void startConnection() throws CoreException {
  }

  @Override
  protected void stopConnection() {
    this.destroyHandle(contextHandle, "Solace context handle");
  }

  @Override
  protected void closeConnection() {
  }
  
  @Override
  public SessionHandle createSession(MessageCallback consumer) throws SolclientException {
    String[] sessionProperties = new String[8];
    sessionProperties[0] = SessionHandle.PROPERTIES.HOST;
    sessionProperties[1] = this.getHost();
    sessionProperties[2] = SessionHandle.PROPERTIES.USERNAME;
    sessionProperties[3] = this.getUsername();
    sessionProperties[4] = SessionHandle.PROPERTIES.PASSWORD;
    sessionProperties[5] = this.getPassword();
    sessionProperties[6] = SessionHandle.PROPERTIES.VPN_NAME;
    sessionProperties[7] = this.getVpnName();
    
    SessionHandle sessionHandle = Solclient.Allocator.newSessionHandle();
    contextHandle.createSessionForHandle(sessionHandle, sessionProperties, consumer, this);
    
    return sessionHandle;
  }

  @Override
  public void onEvent(SessionHandle sessionHandle) {
  }
  
  private void destroyHandle(Handle handle, String description) {
    try {
      if (handle != null && handle.isBound()) {
        handle.destroy();
        log.info("Destroyed [" + description + "]");
      }
    } catch (Throwable t) {
      log.error("Unable to destroy [" + description + "]", t);
    }
  }

  public String getHost() {
    return host;
  }

  public void setHost(String host) {
    this.host = host;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getVpnName() {
    return vpnName;
  }

  public void setVpnName(String vpnName) {
    this.vpnName = vpnName;
  }

}
