package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("connection-tuning")
public class ConnectionTuning implements Parameter {
  private Integer connectRetries;
  private Integer connectRetriesPerHost;
  private Integer connectTimeoutInMillis;
  private Integer readTimeoutInMillis;
  private Integer reconnectRetries;
  private Integer reconnectRetryWaitInMillis;
  private Boolean tcpNoDelay;
  
  @Override
  public void apply(SolConnectionFactory cf) {
    cf.setConnectRetries(getConnectRetries());
    cf.setConnectRetriesPerHost(getConnectRetriesPerHost());
    cf.setConnectTimeoutInMillis(getConnectTimeoutInMillis());
    cf.setReadTimeoutInMillis(getReadTimeoutInMillis());
    cf.setReconnectRetries(getReconnectRetries());
    cf.setReconnectRetryWaitInMillis(getReconnectRetryWaitInMillis());
    cf.setTcpNoDelay(getTcpNoDelay());
  }
  
  public Integer getConnectRetries() {
    return connectRetries;
  }

  /**
   * This property is used to specify the number of times to retry data connection attempts.
   */
  public void setConnectRetries(Integer connectRetries) {
    this.connectRetries = connectRetries;
  }

  public Integer getConnectRetriesPerHost() {
    return connectRetriesPerHost;
  }

  /**
   * This property is used to specify the number of times to retry data connection attempts to a single host before moving on to the next host in the list.
   */
  public void setConnectRetriesPerHost(Integer connectRetriesPerHost) {
    this.connectRetriesPerHost = connectRetriesPerHost;
  }

  public Integer getConnectTimeoutInMillis() {
    return connectTimeoutInMillis;
  }

  /**
   * This property is used to specify the timeout in milliseconds on a data connection attempt.
   */
  public void setConnectTimeoutInMillis(Integer connectTimeoutInMillis) {
    this.connectTimeoutInMillis = connectTimeoutInMillis;
  }

  public Integer getReadTimeoutInMillis() {
    return readTimeoutInMillis;
  }

  /**
   * This property is used to specify the timeout in milliseconds for reading a reply from the appliance.
   */
  public void setReadTimeoutInMillis(Integer readTimeoutInMillis) {
    this.readTimeoutInMillis = readTimeoutInMillis;
  }

  public Integer getReconnectRetries() {
    return reconnectRetries;
  }

  /**
   * This property is used to specify the number of times to attempt a reconnect once the data connection to the appliance has been lost.
   */
  public void setReconnectRetries(Integer reconnectRetries) {
    this.reconnectRetries = reconnectRetries;
  }

  public Integer getReconnectRetryWaitInMillis() {
    return reconnectRetryWaitInMillis;
  }

  /**
   * This property is used to specify the amount of time to wait in milliseconds between reconnect attempts.
   */
  public void setReconnectRetryWaitInMillis(Integer reconnectRetryWaitInMillis) {
    this.reconnectRetryWaitInMillis = reconnectRetryWaitInMillis;
  }
  
  public Boolean getTcpNoDelay() {
    return tcpNoDelay;
  }

  /**
   * This property is used to specify whether to set the TCP_NODELAY option.
   */
  public void setTcpNoDelay(Boolean tcpNoDelay) {
    this.tcpNoDelay = tcpNoDelay;
  }

}
