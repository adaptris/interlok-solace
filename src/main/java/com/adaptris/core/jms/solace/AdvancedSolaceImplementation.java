package com.adaptris.core.jms.solace;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.jms.JMSException;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.jms.JmsUtils;
import com.adaptris.core.jms.solace.parameters.Parameter;
import com.adaptris.util.KeyValuePair;
import com.adaptris.util.KeyValuePairSet;
import com.solacesystems.jms.SolConnectionFactory;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

import lombok.Getter;
import lombok.Setter;

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
  
  /**
   * This property specifies the authentication scheme.
   */
  @Getter
  @Setter
  private AuthenticationSchemeEnum authenticationScheme;
  /**
   * This property is used to enable and specify the ZLIB compression level.
   */
  @Getter
  @Setter
  private Integer compressionLevel;
  /**
   * This property specifies the delivery mode for sent messages.
   */
  @Getter
  @Setter
  private DeliveryModeEnum deliveryMode;
  /**
   * This property specifies whether to optimize the API for direct transport.
   */
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean directOptimized;
  /**
   * This property specifies whether to use direct transport for non-persistent messages.
   */
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean directTransport;
  /**
   * This property is used to indicate whether durable topic endpoints or queues are to be created on the appliance when the corresponding Session.createDurableSubscriber() or Session.createQueue() is called.
   */
  @AdvancedConfig
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean dynamicDurables;
  /**
   * This property is used to indicate whether dynamically created durable topic endpoints or queues are set to respect time to live (see Dynamic Durables).
   */
  @AdvancedConfig
  @InputFieldDefault(value = "true")
  @Getter
  @Setter
  private Boolean respectTTL;
  
  @NotNull
  @AutoPopulated
  @Valid
  @XStreamImplicit
  @Getter
  @Setter
  private List<Parameter> extraParameters = new ArrayList<Parameter>();
  
  @NotNull
  @AutoPopulated
  @Getter
  @Setter
  private KeyValuePairSet properties = new KeyValuePairSet();

  @Override
  public SolConnectionFactory createConnectionFactory() throws JMSException {
    SolConnectionFactory connectionFactory = super.createConnectionFactory();
    try {
      if(getAuthenticationScheme() != null) {
        connectionFactory.setAuthenticationScheme(getAuthenticationScheme().getValue());
      }
      connectionFactory.setCompressionLevel(getCompressionLevel());
      
      if(getDeliveryMode() != null) {
        connectionFactory.setDeliveryMode(getDeliveryMode().getDeliveryMode());
      }
      
      connectionFactory.setDirectOptimized(directOptimized());
      connectionFactory.setDirectTransport(directTransport());
      connectionFactory.setDynamicDurables(dynamicDurables());
      connectionFactory.setRespectTTL(getRespectTTL());

      for(Parameter p: getExtraParameters()) {
        p.apply(connectionFactory);
      }
      
      for(Iterator<KeyValuePair> it = getProperties().iterator(); it.hasNext();) {
        KeyValuePair kv = it.next();
        connectionFactory.setProperty(kv.getKey(), kv.getValue());
      }
      // Username and password will be set in .connect(username, password)
    } catch (Exception e) {
      JmsUtils.rethrowJMSException(e);
    }
    return connectionFactory;

  }
  
  public boolean directOptimized() {
    return this.getDirectOptimized() != null ? this.getDirectOptimized() : false;
  }
  
  public boolean directTransport() {
    return this.getDirectTransport() != null ? this.getDirectTransport() : false;
  }
  
  public boolean dynamicDurables() {
    return this.getDynamicDurables() != null ? this.getDynamicDurables() : false;
  }
  
  public boolean respectTTL() {
    return this.getRespectTTL() != null ? this.getRespectTTL() : true;
  }
  
}
