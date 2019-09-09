package com.adaptris.core.jms.solace;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.jms.JmsActorConfig;
import com.adaptris.core.jms.JmsDestination;
import com.adaptris.core.jms.VendorImplementation;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * JNDI Solace implementation of <code>VendorImplementation</code>.
 * </p>
 * <p>
 * This vendor implementation allows you to configure the JNDI properties that will fetch the connection-factory from the Solace JNDI store..
 * </p>
 * <p>
 * The main reason to use this JNDI implementation rather than the standard JNDI implementation, is that this one allows us retry creating the consumer should it fail.
 * Typically this might happen if a Solace queue has been shutdown.  In which case the consumer creation will fail.
 * </p>
 * <p>
 * <b>This was built against Solace 7.1.0.207</b>
 * </p>
 * <p>
 * 
 * @config solace-jndi-implementation
 * @license BASIC
 */
@AdapterComponent
@ComponentProfile(summary = "Custom solace JNDI implementation that has consumer creation reties should it error when starting up/restarting..", tag = "consumer,jms,jndi,vendor")
@XStreamAlias("solace-jndi-implementation")
public class SolaceJndiVendorImplementation extends StandardJndiImplementation {
  
  private static final int DEFAULT_CREATE_CONSUMER_RETRY_WAIT_SECONDS = 30;
  
  private static final int DEFAULT_CREATE_CONSUMER_RETRIES = 0;
  
  @AdvancedConfig
  @AutoPopulated
  @InputFieldDefault(value = "30")
  private Integer createConsumerRetryWaitSeconds;
  
  @AutoPopulated
  @AdvancedConfig
  @InputFieldDefault(value = "0")
  private Integer createConsumerMaxRetries;
  
  private transient ConsumerCreator consumerCreator;
  
  public SolaceJndiVendorImplementation() {
    consumerCreator = new ConsumerCreator();
  }

  /**
   * <p>
   * If a Solace queue has been shutdown, we should wait for it to come back up before
   * we continue.
   * </p>
   * @see VendorImplementation#createQueue(java.lang.String, JmsActorConfig)
   */
  @Override
  public MessageConsumer createQueueReceiver(ConsumeDestination cd, JmsActorConfig c)
      throws JMSException {
    return consumerCreator.createQueueReceiver(cd, c, createConsumerMaxRetries(), createConsumerRetryWaitSeconds());
  }

  @Override
  public MessageConsumer createConsumer(JmsDestination d, String selector, JmsActorConfig c) throws JMSException {
    return consumerCreator.createConsumer(d, selector, c, createConsumerMaxRetries(), createConsumerRetryWaitSeconds());
  }

  @Override
  public MessageConsumer createTopicSubscriber(ConsumeDestination cd, String subscriptionId,
      JmsActorConfig c) throws JMSException {
    return consumerCreator.createTopicSubscriber(cd, subscriptionId, c);
  }
  
  public Integer createConsumerRetryWaitSeconds() {
    return getCreateConsumerRetryWaitSeconds() == null? DEFAULT_CREATE_CONSUMER_RETRY_WAIT_SECONDS : getCreateConsumerRetryWaitSeconds();
  }
  
  public Integer getCreateConsumerRetryWaitSeconds() {
    return createConsumerRetryWaitSeconds;
  }

  public void setCreateConsumerRetryWaitSeconds(Integer createConsumerRetryWaitSeconds) {
    this.createConsumerRetryWaitSeconds = createConsumerRetryWaitSeconds;
  }

  public Integer createConsumerMaxRetries() {
    return getCreateConsumerMaxRetries() == null? DEFAULT_CREATE_CONSUMER_RETRIES : getCreateConsumerMaxRetries();
  }
  
  public Integer getCreateConsumerMaxRetries() {
    return createConsumerMaxRetries;
  }

  public void setCreateConsumerMaxRetries(Integer createConsumerMaxRetries) {
    this.createConsumerMaxRetries = createConsumerMaxRetries;
  }
}
