package com.adaptris.core.jms.solace;

import java.lang.reflect.Field;
import java.util.Set;

import javax.jms.JMSException;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessageConsumer;
import com.adaptris.core.CoreException;
import com.adaptris.core.jms.JmsConnection;
import com.adaptris.core.jms.JmsConnectionErrorHandler;
import com.adaptris.core.jms.JmsConsumer;
import com.adaptris.core.jms.JmsConsumerImpl;
import com.adaptris.core.jms.PasConsumer;
import com.adaptris.core.jms.PtpConsumer;
import com.solacesystems.jms.SolConsumerEventListener;
import com.solacesystems.jms.SolMessageConsumer;
import com.solacesystems.jms.events.ActiveFlowIndicationEvent;
import com.solacesystems.jms.events.SolConsumerEvent;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * A custom connection error handler that can be configured on your Solace consumers.
 * </p>
 * <p>
 * This connection error handler uses the same standard JMSException listener, but additionally will listen for queue shutdown events, therefore triggering the exception handler.
 * </p>
 * <p>
 * If you use this error handler and a {@link JmsConsumer} (rather than {@link PtpConsumer} or {@link PasConsumer}) then you will also need to set the advanced property of
 * deferConsumerCreationToVendor to true on your {@link JmsConsumer}.  This will allow us to continue to try to create the consumer on restart until the queue has been bought back up again.
 * </p>
 */
@AdapterComponent
@XStreamAlias("solace-consumer-connection-error-handler")
@ComponentProfile(summary = "A connection error handler that listens for Solace queue shutdown events.", tag = "consumer,jms,error-handler")
public class SolaceConsumerConnectionErrorHandler extends JmsConnectionErrorHandler implements SolConsumerEventListener {

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
