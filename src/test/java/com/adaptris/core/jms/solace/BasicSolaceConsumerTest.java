package com.adaptris.core.jms.solace;

import com.adaptris.core.StandaloneConsumer;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConnectionErrorHandler;
import com.adaptris.core.jms.JmsConsumer;
import com.adaptris.core.jms.JmsConsumerCase;

public class BasicSolaceConsumerTest extends JmsConsumerCase {
  @Override
  public boolean isAnnotatedForJunit4() {
    return true;
  }
  @Override
  protected String createBaseFileName(Object object) {
    return ((StandaloneConsumer) object).getConsumer().getClass().getName() + "-SolaceJMS";
  }

  @Override
  protected Object retrieveObjectForSampleConfig() {
    JmsConsumer consumer = new JmsConsumer().withEndpoint("jms:queue:MyQueueName");
    StandaloneConsumer result = new StandaloneConsumer(configure(new JmsConnection()), consumer);
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
