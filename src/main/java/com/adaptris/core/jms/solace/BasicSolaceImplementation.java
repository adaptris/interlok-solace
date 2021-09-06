package com.adaptris.core.jms.solace;

import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.JmsActorConfig;
import com.adaptris.core.jms.JmsDestination;
import com.adaptris.core.jms.UrlVendorImplementation;
import com.adaptris.core.jms.VendorImplementation;
import com.adaptris.core.jms.VendorImplementationBase;
import com.adaptris.interlok.util.Args;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * Solace implementation of <code>VendorImplementation</code>.
 * </p>
 * <p>
 * This vendor implementation is the minimal adapter interface to Solace.
 * </p>
 * <p>
 * <b>This was built against Solace 10.6.0</b>
 * </p>
 * <p>
 *
 * @config basic-solace-implementation
 * @license BASIC
 */
@XStreamAlias("basic-solace-implementation")
public class BasicSolaceImplementation extends UrlVendorImplementation {

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

  /**
   * Message VPN name. Default: default
   */
  @NotNull
  @AutoPopulated
  @InputFieldDefault(value = "default")
  @Getter
  @Setter
  private String messageVpn;

  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private transient ConsumerCreator consumerCreator;

  public BasicSolaceImplementation() {
    super();
    consumerCreator = new ConsumerCreator();
  }

  @Override
  public SolConnectionFactory createConnectionFactory() throws JMSException {
    try {
      SolConnectionFactory connnectionFactory = SolJmsUtility.createConnectionFactory();
      connnectionFactory.setHost(getBrokerUrl());
      connnectionFactory.setVPN(messageVpn());
      // Username and password will be set in .connect(username, password)
      return connnectionFactory;


    } catch (JMSException e) {
      throw e;
    } catch (Exception e) {
      JMSException ex = new JMSException("Unexpected Exception creating Solace connectionfactory");
      ex.setLinkedException(e);
      throw ex;
    }
  }

  @Override
  public String retrieveBrokerDetailsForLogging() {
    return String.format("Solace host: %s; Message vpn: %s", getBrokerUrl(), getMessageVpn());
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

  private String messageVpn() {
    return ObjectUtils.defaultIfNull(getMessageVpn(), "default");
  }

  @Override
  public boolean connectionEquals(VendorImplementationBase other) {
    if (other instanceof BasicSolaceImplementation) {
      BasicSolaceImplementation rhs = (BasicSolaceImplementation) other;
      return new EqualsBuilder()
        .append(getBrokerUrl(), rhs.getBrokerUrl())
        .append(getMessageVpn(), rhs.getMessageVpn())
        .isEquals();
    }
    return false;
  }


  @Override
  public void prepare() throws CoreException {
    Args.notBlank(getBrokerUrl(), "broker-url");
    super.prepare();
  }

  public Integer createConsumerRetryWaitSeconds() {
    return getCreateConsumerRetryWaitSeconds() == null? DEFAULT_CREATE_CONSUMER_RETRY_WAIT_SECONDS : getCreateConsumerRetryWaitSeconds();
  }

  public Integer createConsumerMaxRetries() {
    return getCreateConsumerMaxRetries() == null? DEFAULT_CREATE_CONSUMER_RETRIES : getCreateConsumerMaxRetries();
  }

}
