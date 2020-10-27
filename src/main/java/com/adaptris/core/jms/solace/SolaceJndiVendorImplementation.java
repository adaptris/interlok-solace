package com.adaptris.core.jms.solace;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.jms.JmsActorConfig;
import com.adaptris.core.jms.JmsDestination;
import com.adaptris.core.jms.VendorImplementation;
import com.adaptris.core.jms.jndi.StandardJndiImplementation;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

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
 * <b>This was built against Solace 10.6.0</b>
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

  /**
   * Sets the amount of time in seconds to wait before each attempt to create the message consumer.
   */
  @AdvancedConfig
  @AutoPopulated
  @InputFieldDefault(value = "30")
  @Getter
  @Setter
  private Integer createConsumerRetryWaitSeconds;

  /**
   * <p>Sets the maximum amount of times to retry attempting to create the message consumer.</p>
   * <p>A value of zero means continue trying forever</p>
   */
  @AutoPopulated
  @AdvancedConfig
  @InputFieldDefault(value = "0")
  @Getter
  @Setter
  private Integer createConsumerMaxRetries;

  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
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
  public MessageConsumer createQueueReceiver(String queueName, String selector, JmsActorConfig c)
      throws JMSException {
    return consumerCreator.createQueueReceiver(queueName, selector, c, createConsumerMaxRetries(),
        createConsumerRetryWaitSeconds());
  }

  @Override
  public MessageConsumer createConsumer(JmsDestination d, String selector, JmsActorConfig c) throws JMSException {
    return consumerCreator.createConsumer(d, selector, c, createConsumerMaxRetries(), createConsumerRetryWaitSeconds());
  }

  @Override
  public MessageConsumer createTopicSubscriber(String topicName, String selector,
      String subscriptionId,
      JmsActorConfig c) throws JMSException {
    return consumerCreator.createTopicSubscriber(topicName, selector, subscriptionId, c);
  }

  public Integer createConsumerRetryWaitSeconds() {
    return getCreateConsumerRetryWaitSeconds() == null? DEFAULT_CREATE_CONSUMER_RETRY_WAIT_SECONDS : getCreateConsumerRetryWaitSeconds();
  }

  public Integer createConsumerMaxRetries() {
    return getCreateConsumerMaxRetries() == null? DEFAULT_CREATE_CONSUMER_RETRIES : getCreateConsumerMaxRetries();
  }

}
