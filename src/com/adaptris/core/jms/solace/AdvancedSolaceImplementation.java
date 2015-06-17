package com.adaptris.core.jms.solace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jms.JMSException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.jms.solace.parameters.Parameter;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

/**
 * <p>
 * Solace implementation of <code>VendorImplementation</code>.
 * </p>
 * <p>
 * This vendor implementation is a more complete adapter interface to Solace, designed to
 * expose as many of the configuration properties as possible. Most settings are exposed
 * as part of parameter objects added to the "extraParameters" list.
 * </p>
 * <p>
 * <b>This was built against Solace 7.1.0.207</b>
 * </p>
 * <p>
 * 
 * @config advanced-solace-implementation
 * @license BASIC
 */
@XStreamAlias("advanced-solace-implementation")
public class AdvancedSolaceImplementation extends BasicSolaceImplementation {
  private AuthenticationSchemeEnum authenticationScheme;
  private Integer compressionLevel;
  private DeliveryModeEnum deliveryMode;
  private Boolean directOptimized;
  private Boolean directTransport;
  private Boolean dynamicDurables;
  private Boolean respectTTL;
  
  @NotNull
  @AutoPopulated
  @Valid
  @XStreamImplicit
  private List<Parameter> extraParameters = new ArrayList<Parameter>();
  
  @NotNull
  @AutoPopulated
  @Valid
  @XStreamImplicit
  private KeyValuePairSet properties = new KeyValuePairSet();

  @Override
  public SolConnectionFactory createConnectionFactory() throws JMSException {
    try {
      SolConnectionFactory connectionFactory = super.createConnectionFactory();

      if(getAuthenticationScheme() != null) {
        connectionFactory.setAuthenticationScheme(getAuthenticationScheme().getValue());
      }
      connectionFactory.setCompressionLevel(getCompressionLevel());
      connectionFactory.setDeliveryMode(getDeliveryMode().getDeliveryMode());
      connectionFactory.setDirectOptimized(getDirectOptimized());
      connectionFactory.setDirectTransport(getDirectTransport());
      connectionFactory.setDynamicDurables(getDynamicDurables());
      connectionFactory.setRespectTTL(getRespectTTL());

      for(Parameter p: getExtraParameters()) {
        p.apply(connectionFactory);
      }
      
      for(Iterator<KeyValuePair> it = getProperties().iterator(); it.hasNext();) {
        KeyValuePair kv = it.next();
        connectionFactory.setProperty(kv.getKey(), kv.getValue());
      }
      
      // Username and password will be set in .connect(username, password)
      return connectionFactory;
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
    return String.format("Solace host: %s; Message vpn: %s", getHostname(), getMessageVpn());
  }

  public AuthenticationSchemeEnum getAuthenticationScheme() {
    return authenticationScheme;
  }

  /**
   * This property specifies the authentication scheme.
   */
  public void setAuthenticationScheme(AuthenticationSchemeEnum authenticationScheme) {
    this.authenticationScheme = authenticationScheme;
  }

  public KeyValuePairSet getProperties() {
    return properties;
  }

  public void setProperties(KeyValuePairSet properties) {
    this.properties = properties;
  }

  public Integer getCompressionLevel() {
    return compressionLevel;
  }

  /**
   * This property is used to enable and specify the ZLIB compression level.
   */
  public void setCompressionLevel(Integer compressionLevel) {
    this.compressionLevel = compressionLevel;
  }

  public DeliveryModeEnum getDeliveryMode() {
    return deliveryMode;
  }

  /**
   * This property specifies the delivery mode for sent messages.
   */
  public void setDeliveryMode(DeliveryModeEnum deliveryMode) {
    this.deliveryMode = deliveryMode;
  }

  public Boolean getDirectOptimized() {
    return directOptimized;
  }

  /**
   * This property specifies whether to optimize the API for direct transport.
   */
  public void setDirectOptimized(Boolean directOptimized) {
    this.directOptimized = directOptimized;
  }

  public Boolean getDirectTransport() {
    return directTransport;
  }

  /**
   * This property specifies whether to use direct transport for non-persistent messages.
   */
  public void setDirectTransport(Boolean directTransport) {
    this.directTransport = directTransport;
  }

  public Boolean getDynamicDurables() {
    return dynamicDurables;
  }

  /**
   * This property is used to indicate whether durable topic endpoints or queues are to be created on the appliance when the corresponding Session.createDurableSubscriber() or Session.createQueue() is called.
   */
  public void setDynamicDurables(Boolean dynamicDurables) {
    this.dynamicDurables = dynamicDurables;
  }

  public Boolean getRespectTTL() {
    return respectTTL;
  }

  /**
   * This property is used to indicate whether dynamically created durable topic endpoints or queues are set to respect time to live (see Dynamic Durables).
   */
  public void setRespectTTL(Boolean respectTTL) {
    this.respectTTL = respectTTL;
  }

  public List<Parameter> getExtraParameters() {
    return extraParameters;
  }

  public void setExtraParameters(List<Parameter> extraParameters) {
    this.extraParameters = extraParameters;
  }
  
}
