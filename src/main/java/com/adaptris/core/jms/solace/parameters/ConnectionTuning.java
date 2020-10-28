package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

@XStreamAlias("solace-connection-tuning")
public class ConnectionTuning implements Parameter {
  
  /**
   * This property is used to specify the number of times to retry data connection attempts.
   */
  @Getter
  @Setter
  private Integer connectRetries;
  /**
   * This property is used to specify the number of times to retry data connection attempts to a single host before moving on to the next host in the list.
   */
  @Getter
  @Setter
  private Integer connectRetriesPerHost;
  /**
   * This property is used to specify the timeout in milliseconds on a data connection attempt.
   */
  @Getter
  @Setter
  private Integer connectTimeoutInMillis;
  /**
   * This property is used to specify the timeout in milliseconds for reading a reply from the appliance.
   */
  @Getter
  @Setter
  private Integer readTimeoutInMillis;
  /**
   * This property is used to specify the number of times to attempt a reconnect once the data connection to the appliance has been lost.
   */
  @Getter
  @Setter
  private Integer reconnectRetries;
  /**
   * This property is used to specify the amount of time to wait in milliseconds between reconnect attempts.
   */
  @Getter
  @Setter
  private Integer reconnectRetryWaitInMillis;
  /**
   * This property is used to specify whether to set the TCP_NODELAY option.
   */
  @Getter
  @Setter
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

}
