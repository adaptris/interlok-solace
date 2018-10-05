package com.adaptris.core.jms.solace;

import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.jms.solace.parameters.ConnectionTuning;
import com.adaptris.core.jms.solace.parameters.ReceiveFlags;
import com.adaptris.core.jms.solace.parameters.SendFlags;
import com.adaptris.util.KeyValuePair;

public class AdvancedSolaceConsumerTest extends BasicSolaceConsumerTest {

  public AdvancedSolaceConsumerTest(String name) {
    super(name);
  }


  @Override
  protected String createBaseFileName(Object object) {
    return ((StandaloneConsumer) object).getConsumer().getClass().getName() + "-Advanced-SolaceJMS";
  }


  protected AdvancedSolaceImplementation createVendorImpl() {
    return configureVendor(new AdvancedSolaceImplementation());
  }

  static AdvancedSolaceImplementation configureVendor(AdvancedSolaceImplementation mq) {
    mq.setHostname("smf://localhost");
    mq.setBrokerUrl("smf://localhost");
    mq.setMessageVpn("default");
    mq.setAuthenticationScheme(AuthenticationSchemeEnum.AUTHENTICATION_SCHEME_BASIC);
    mq.getExtraParameters().add(createTuning());
    mq.getExtraParameters().add(createSendFlags());
    mq.getExtraParameters().add(createReceiveFlags());
    mq.getProperties().add(new KeyValuePair("FactoryProperty_1", "Factory_Property_Value1"));
    mq.getProperties().add(new KeyValuePair("FactoryProperty_2", "Factory_Property_Value2"));
    return mq;
  }

  private static ConnectionTuning createTuning() {
    ConnectionTuning tuning = new ConnectionTuning();
    tuning.setConnectRetries(10);
    tuning.setConnectRetriesPerHost(5);
    tuning.setConnectTimeoutInMillis(60000);
    tuning.setReadTimeoutInMillis(5000);
    return tuning;
  }

  private static SendFlags createSendFlags() {
    SendFlags tuning = new SendFlags();
    tuning.setDmqEligible(true);
    tuning.setGenerateSenderID(true);
    return tuning;
  }

  private static ReceiveFlags createReceiveFlags() {
    ReceiveFlags tuning = new ReceiveFlags();
    tuning.setDeliverToOne(true);
    return tuning;
  }
}
