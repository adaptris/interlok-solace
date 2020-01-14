package com.adaptris.core.jms.solace;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConnectionErrorHandler;
import com.adaptris.core.jms.JmsProducer;
import com.adaptris.core.jms.JmsProducerExample;

public class BasicSolaceProducerTest extends JmsProducerExample {
  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  @Override
  protected String createBaseFileName(Object object) {
    return ((StandaloneProducer) object).getProducer().getClass().getName() + "-SolaceJMS";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {

    JmsProducer producer = new JmsProducer(new ConfiguredProduceDestination("jms:queue:MyQueueName"));
    StandaloneProducer result = new StandaloneProducer(configure(new JmsConnection()), producer);

    return result;
  }

  protected JmsConnection configure(JmsConnection c) {
    c.setUserName("BrokerUsername");
    c.setPassword("BrokerPassword");
    c.setVendorImplementation(createVendorImpl());
    c.setConnectionErrorHandler(new JmsConnectionErrorHandler());
    return c;
  }

  protected BasicSolaceImplementation createVendorImpl() {
    BasicSolaceImplementation mq = new BasicSolaceImplementation();
    mq.setBrokerUrl("smf://localhost:55555");
    mq.setMessageVpn("default");
    return mq;
  }

}
