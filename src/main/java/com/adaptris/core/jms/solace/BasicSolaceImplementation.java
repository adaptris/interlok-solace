package com.adaptris.core.jms.solace;

import static org.apache.commons.lang.StringUtils.isBlank;
import static org.apache.commons.lang.StringUtils.isNotBlank;
import static org.apache.commons.lang3.StringUtils.isEmpty;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.annotation.Removal;
import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.JmsActorConfig;
import com.adaptris.core.jms.JmsDestination;
import com.adaptris.core.jms.JmsDestination.DestinationType;
import com.adaptris.core.jms.UrlVendorImplementation;
import com.adaptris.core.jms.VendorImplementation;
import com.adaptris.core.jms.VendorImplementationBase;
import com.adaptris.core.licensing.License;
import com.adaptris.core.licensing.License.LicenseType;
import com.adaptris.core.licensing.LicenseChecker;
import com.adaptris.core.licensing.LicensedComponent;
import com.adaptris.core.util.LoggingHelper;
import com.adaptris.util.NumberUtils;
import com.solacesystems.jms.SolConnectionFactory;
import com.solacesystems.jms.SolJmsUtility;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * Solace implementation of <code>VendorImplementation</code>.
 * </p>
 * <p>
 * This vendor implementation is the minimal adapter interface to Solace.
 * </p>
 * <p>
 * <b>This was built against Solace 7.1.0.207</b>
 * </p>
 * <p>
 * 
 * @config basic-solace-implementation
 * @license BASIC
 */
@XStreamAlias("basic-solace-implementation")
public class BasicSolaceImplementation extends UrlVendorImplementation implements LicensedComponent {
  
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
  
  private static final int DEFAULT_SMF_PORT = 55555;
  private static boolean warningLogged = false;
  @Deprecated
  @Removal(message = "Use broker-url instead", version = "3.11.0")
  private String hostname;
  
  @Deprecated
  @Removal(message = "Use broker-url instead", version = "3.11.0")
  private Integer port;
  
  @NotNull
  @AutoPopulated
  @InputFieldDefault(value = "default")
  private String messageVpn;
  
  @Override
  public SolConnectionFactory createConnectionFactory() throws JMSException {
    if (isBlank(getBrokerUrl()) && isNotBlank(getHostname())) {
      LoggingHelper.logWarning(warningLogged, () -> {
        warningLogged = true;
      }, "hostname and port are deprecated; please use broker-url instead");
      setBrokerUrl(getHostname() + (configuredLegacyPort() != DEFAULT_SMF_PORT ? ":" + configuredLegacyPort() : ""));
    }
    
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
  public MessageConsumer createQueueReceiver(ConsumeDestination cd, JmsActorConfig c)
      throws JMSException {
    Session s = c.currentSession();
    Queue q = createQueue(cd.getDestination(), c);
    return createConsumerWithRetry(s, q, cd.getFilterExpression(), false);
  }

  @Override
  public MessageConsumer createConsumer(JmsDestination d, String selector, JmsActorConfig c) throws JMSException {
    MessageConsumer consumer = null;
    if (d.destinationType().equals(DestinationType.TOPIC) && !isEmpty(d.subscriptionId())) {
      consumer = c.currentSession().createDurableSubscriber((Topic) d.getDestination(), d.subscriptionId(), selector, d.noLocal());
    } else {
      consumer = createConsumerWithRetry(c.currentSession(), d.getDestination(), selector, d.noLocal());
    }
    return consumer;
  }

  @Override
  public MessageConsumer createTopicSubscriber(ConsumeDestination cd, String subscriptionId,
      JmsActorConfig c) throws JMSException {
    Session s = c.currentSession();
    Topic t = createTopic(cd.getDestination(), c);
    MessageConsumer result = null;
    if (!isEmpty(subscriptionId)) {
      result = s.createDurableSubscriber(t, subscriptionId, cd.getFilterExpression(), false);
    } else {
      result = s.createConsumer(t, cd.getFilterExpression());
    }
    return result;
  }
  
  /**
   * With Solace you can shut a queue down, which would mean it fails to startup.
   * So here we will retry to create the consumer as many times as configured.
   * 
   * @param session
   * @param destination
   * @param selector
   * @param noLocal
   * @return
   * @throws JMSException
   */
  protected MessageConsumer createConsumerWithRetry(Session session, Destination destination, String selector, boolean noLocal) throws JMSException {
    MessageConsumer returnedConsumer = null;
    JMSException lastException = null;
    
    int retries = 0;
    while((returnedConsumer == null) && ((retries <= createConsumerMaxRetries()) || (createConsumerMaxRetries() == 0) )) {
      try {
        returnedConsumer = session.createConsumer(destination, selector, noLocal);
      } catch(JMSException ex) {
        lastException = ex;
        retries ++;
        log.error("Failed to create consumer, will retry ({}) and after {} seconds.", retries, createConsumerRetryWaitSeconds(), ex);
        try {
          Thread.sleep(createConsumerRetryWaitSeconds() * 1000);
        } catch (InterruptedException e) {
          log.warn("Interrupted while trying to create a message consumer.  Exiting.");
          break;
        }
        
      }
    }
    
    if(returnedConsumer == null)
      throw lastException;
    
    return returnedConsumer;
  }

  @Deprecated
  @Removal(message = "Use broker-url instead", version = "3.11.0")
  public String getHostname() {
    return hostname;
  }

  /**
   * Appliance or VMR host name, must be prefixed with smf://
   * @param hostname
   */
  @Deprecated
  @Removal(message = "Use broker-url instead", version = "3.11.0")
  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  @Deprecated
  @Removal(message = "Use broker-url instead", version = "3.11.0")
  public Integer getPort() {
    return port;
  }

  /**
   * Port number.
   * 
   * @param port
   */
  @Deprecated
  @Removal(message = "Use broker-url instead", version = "3.11.0")
  public void setPort(Integer port) {
    this.port = port;
  }

  private int configuredLegacyPort() {
    return NumberUtils.toIntDefaultIfNull(getPort(), DEFAULT_SMF_PORT);
  }

  public String getMessageVpn() {
    return messageVpn;
  }

  /**
   * Message VPN name. Default: default
   */
  public void setMessageVpn(String messageVpn) {
    this.messageVpn = messageVpn;
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
        .append(getPort(), rhs.getPort())
        .append(getMessageVpn(), rhs.getMessageVpn())
        .isEquals();
    }
    return false;
  }


  @Override
  public void prepare() throws CoreException {
    LicenseChecker.newChecker().checkLicense(this);
    super.prepare();
  }

  @Override
  public boolean isEnabled(License license) {
    return license.isEnabled(LicenseType.Standard);
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
