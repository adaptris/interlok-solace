package com.adaptris.core.jms.solace;

import static com.adaptris.core.jms.solace.AdvancedSolaceConsumerTest.configureVendor;
import com.adaptris.core.StandaloneProducer;

public class AdvancedSolaceProducerTest extends BasicSolaceProducerTest {

  @Override
  protected String createBaseFileName(Object object) {
    return ((StandaloneProducer) object).getProducer().getClass().getName() + "-Advanced-SolaceJMS";
  }

  @Override
  protected AdvancedSolaceImplementation createVendorImpl() {
    return configureVendor(new AdvancedSolaceImplementation());
  }

}
