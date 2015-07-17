package com.adaptris.core.jms.solace;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.ConfiguredProduceDestination;
import com.adaptris.core.StandaloneProducer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConnectionErrorHandler;
import com.adaptris.core.jms.JmsProducer;
import com.adaptris.core.jms.JmsProducerExample;

public class BasicSolaceProducerTest extends JmsProducerExample {

  private static Logger log = LoggerFactory.getLogger(BasicSolaceProducerTest.class);

  public BasicSolaceProducerTest(String name) {
    super(name);
  }

  @Override
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  public void tearDown() throws Exception {
    super.tearDown();
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
    mq.setHostname("smf://localhost");
    mq.setMessageVpn("default");
    return mq;
  }

}
