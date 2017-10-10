package com.adaptris.core.jms.solace;

import java.lang.reflect.Field;
import java.util.Set;

import javax.jms.JMSException;

import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConnectionErrorHandler;
import com.adaptris.core.jms.JmsConsumerImpl;
import com.solacesystems.jms.SolConsumerEventListener;
import com.solacesystems.jms.SolMessageConsumer;
import com.solacesystems.jms.events.ActiveFlowIndicationEvent;
import com.solacesystems.jms.events.SolConsumerEvent;
import com.thoughtworks.xstream.annotations.XStreamAlias;

//@XStreamAlias("solace-consumer-connection-error-handler")
class SolaceConsumerConnectionErrorHandler extends JmsConnectionErrorHandler implements SolConsumerEventListener {

  private static final String MESSAGE_CONSUMER_FIELD_NAME = "consumer";
  
  @Override
  public void onEvent(SolConsumerEvent event) {
    if(event instanceof ActiveFlowIndicationEvent) {
      int flowState = ((ActiveFlowIndicationEvent) event).getFlowState();
      
      if(flowState == ActiveFlowIndicationEvent.FLOW_INACTIVE)
        super.onException(new JMSException("Solace flow has been shutdown or de-provisioned"));
    }
  }
  
  @Override
  public void start() throws CoreException {
    super.start();
    try {
      Set<AdaptrisMessageConsumer> messageConsumers = retrieveConnection(JmsConnection.class).retrieveMessageConsumers();
      
      Field field = JmsConsumerImpl.class.getDeclaredField(MESSAGE_CONSUMER_FIELD_NAME);
      field.setAccessible(true);
      
      for(AdaptrisMessageConsumer messageConsumer : messageConsumers) {
        SolMessageConsumer jmsMessageConsume = (SolMessageConsumer) field.get(messageConsumer);
        jmsMessageConsume.setSolConsumerEventListener(this);
      }
      
    } catch (Exception e) {
      throw new CoreException(e);
    }
  }

}
