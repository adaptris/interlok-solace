package com.adaptris.core.jms.solace;

import static org.apache.commons.lang3.StringUtils.isEmpty;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.Topic;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.ConsumeDestination;
import com.adaptris.core.jms.JmsActorConfig;
import com.adaptris.core.jms.JmsDestination;
import com.adaptris.core.jms.JmsDestination.DestinationType;
import com.adaptris.core.jms.VendorImplementation;

public class ConsumerCreator {
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass());

  /**
   * <p>
   * If a Solace queue has been shutdown, we should wait for it to come back up before
   * we continue.
   * </p>
   * @see VendorImplementation#createQueue(java.lang.String, JmsActorConfig)
   */
  public MessageConsumer createQueueReceiver(ConsumeDestination cd, JmsActorConfig c, int maxRetries, int waitBetweenRetries)
      throws JMSException {
    Session s = c.currentSession();
    Queue q = s.createQueue(cd.getDestination());
    return createConsumerWithRetry(s, q, cd.getFilterExpression(), false, maxRetries, waitBetweenRetries);
  }

  public MessageConsumer createConsumer(JmsDestination d, String selector, JmsActorConfig c, int maxRetries, int waitBetweenRetries) throws JMSException {
    MessageConsumer consumer = null;
    if (d.destinationType().equals(DestinationType.TOPIC) && !isEmpty(d.subscriptionId())) {
      consumer = c.currentSession().createDurableSubscriber((Topic) d.getDestination(), d.subscriptionId(), selector, d.noLocal());
    } else {
      consumer = createConsumerWithRetry(c.currentSession(), d.getDestination(), selector, d.noLocal(), maxRetries, waitBetweenRetries);
    }
    return consumer;
  }

  public MessageConsumer createTopicSubscriber(ConsumeDestination cd, String subscriptionId,
      JmsActorConfig c) throws JMSException {
    Session s = c.currentSession();
    Topic t = s.createTopic(cd.getDestination());
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
  protected MessageConsumer createConsumerWithRetry(Session session, Destination destination, String selector, boolean noLocal, int maxRetries, int waitBetweenRetries) throws JMSException {
    MessageConsumer returnedConsumer = null;
    JMSException lastException = null;
    
    int retries = 0;
    while((returnedConsumer == null) && ((retries <= maxRetries) || (maxRetries == 0) )) {
      try {
        returnedConsumer = session.createConsumer(destination, selector, noLocal);
      } catch(JMSException ex) {
        lastException = ex;
        retries ++;
        log.error("Failed to create consumer, will retry ({}) and after {} seconds.", retries, waitBetweenRetries, ex);
        try {
          Thread.sleep(waitBetweenRetries * 1000);
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
  
}
