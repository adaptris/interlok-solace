package com.adaptris.core.jcsmp.solace;

import static org.apache.commons.lang3.ObjectUtils.defaultIfNull;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.cache.CacheEventListener;
import com.adaptris.core.cache.ExpiringMapCache;
import com.adaptris.core.jms.JmsConsumer;
import com.adaptris.core.jms.JmsPollingConsumer;
import com.adaptris.util.TimeInterval;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPStreamingPublishCorrelatingEventHandler;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

/**
 * <p>
 * A Solace JCSMP asynchronous producer event handler that is registered to the Solace producer.
 * </p>
 * <p>
 * After a message has been produced to the Solace VPN this event handler will be called at some point in the future
 * with either a success (the message was received without error) or failure (an error occurred on the Solace VPN). 
 * </p>
 * <p>
 * Once either a success or failure event for any given produced message is received this event handler will 
 * execute the registered AdaptrisMessage's success or failure code.  The code executed on success or fail is determined by the 
 * consumer that originally created the message.  For example; if your consumer is JMS, then on success of the producer producing the message
 * the success code will likely be an acknowledgement/commit of the originally consumed message.
 * </p>
 * <p>
 * <b>NOTE:</b> The success and failure code executed will not run in the workflow thread, but a callback thread maintained by the Solace client API.
 * Therefore if you're bridging a JMS consumer to this Solace producer, then you must make sure your JMS consumer is run in asynchronous mode too.
 * Standard rules of JMS will not allow you to act on (acknowledge/commit) a consumed message in a thread that is not the delivery thread.
 * Simply make sure your not using a JMS message listener such as {@link JmsConsumer}, but instead an asynchronous consumer like {@link JmsPollingConsumer}.
 * </p>
 * @author Aaron
 *
 */
public class SolaceJcsmpProduceEventHandler implements JCSMPStreamingPublishCorrelatingEventHandler, ComponentLifecycle, CacheEventListener {
  
  private static final long EXPIRE_FAILED_MESSAGES_SECONDS = 120;
  
  private static final int EVICT_MESSAGES_MAX = 5000;
  
  protected transient Logger log = LoggerFactory.getLogger(this.getClass().getName());
  
  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private transient ExpiringMapCache unAckedMessages;
  
  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private transient SolaceJcsmpAbstractProducer producer;
  
  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private volatile boolean acceptSuccessCallbacks;
  
  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private ReentrantLock lock;

  public SolaceJcsmpProduceEventHandler(SolaceJcsmpAbstractProducer producer) {
    this.setProducer(producer);
    this.setLock(new ReentrantLock(true));
  }
  
  @Override
  public void handleErrorEx(Object obj, JCSMPException ex, long arg2) {
    getLock().lock();
    
    try {
      String messageId = (String) obj;
    
      setAcceptSuccessCallbacks(false);
      log.error("Received failure callback from Solace for message id {}", messageId);
    
      logRemainingUnAckedMessages();
      
      CallbackConsumers callbackConsumers = (CallbackConsumers) this.getUnAckedMessages().get(messageId);
      if(callbackConsumers == null) {
        log.warn("Received failure callback for an unknown message {}", messageId, ex);
      } else {
        if(callbackConsumers.getOnFailure() != null)
          callbackConsumers.getOnFailure().accept(callbackConsumers.getMessage());
        getUnAckedMessages().remove(messageId);
      }
    } catch (CoreException cex) {
      log.warn("Attempting to handle failure callback.", cex);
    } finally {
      getProducer().retrieveConnection(SolaceJcsmpConnection.class).getConnectionErrorHandler().handleConnectionException();
      getLock().unlock();
    }
  }

  @Override
  public void responseReceivedEx(Object obj) {
    String messageId = (String) obj;
    
    getLock().lock();
    try {
      log.debug("Success callback received from Solace for message id {}", messageId);
      if(this.getAcceptSuccessCallbacks()) {
        try {
          CallbackConsumers callbackConsumers = (CallbackConsumers) this.getUnAckedMessages().get(messageId);
          if(callbackConsumers == null) {
            log.warn("Received success callback for an unknown message {}", messageId);
          } else {
            
            if(callbackConsumers.getOnSuccess() != null)
              callbackConsumers.getOnSuccess().accept(callbackConsumers.getMessage());
  
            getUnAckedMessages().remove(messageId);
          }
          logRemainingUnAckedMessages();
        } catch (CoreException cex) {
          log.warn("Error trying to handle success callback.", cex);
          getLock().unlock();
          handleErrorEx(obj, new JCSMPException("", cex), 0l);
        }
      } else {
        log.warn("Executing producer restart, not accepting further success callbacks until complete.");
        getLock().unlock();
        handleErrorEx(obj, new JCSMPException("Success callbacks not available at this moment."), 0l);
      }
    } finally {
      if(getLock().isLocked())
        getLock().unlock();
    
    }
  }

  @SuppressWarnings("unchecked")
  public void addUnAckedMessage(AdaptrisMessage message) throws CoreException {
    log.trace("Adding message to un'acked list with id {}", message.getUniqueId());
    CallbackConsumers callbackConsumers = new CallbackConsumers(message,
        (Consumer<AdaptrisMessage>) message.getObjectHeaders().get(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK),
        (Consumer<AdaptrisMessage>) message.getObjectHeaders().get(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK));
    this.getUnAckedMessages().put(message.getUniqueId(), callbackConsumers);
  }

  private void logRemainingUnAckedMessages() throws CoreException {
    log.trace("{} messages waiting for async callback.", this.getUnAckedMessages().size());
  }
  
  @Override
  public void init() throws CoreException {
    this.setUnAckedMessages(new ExpiringMapCache());
    this.getUnAckedMessages().setExpiration(new TimeInterval(EXPIRE_FAILED_MESSAGES_SECONDS, TimeUnit.SECONDS));
    this.getUnAckedMessages().setMaxEntries(EVICT_MESSAGES_MAX);
    this.getUnAckedMessages().getEventListener().addEventListener(this);
    this.getUnAckedMessages().init();
    this.setAcceptSuccessCallbacks(true);
  }
  
  class CallbackConsumers {
    
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private AdaptrisMessage message;
    
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private Consumer<AdaptrisMessage> onSuccess;
    @Getter(AccessLevel.PACKAGE)
    @Setter(AccessLevel.PACKAGE)
    private Consumer<AdaptrisMessage> onFailure;
    
    CallbackConsumers(AdaptrisMessage message, Consumer<AdaptrisMessage> onSuccess, Consumer<AdaptrisMessage> onFailure) {
      setMessage(message);
      setOnSuccess(onSuccess);
      setOnFailure(onFailure);
    }
  }

  @Override
  public void handleError(String messageId, JCSMPException ex, long arg2) {
    log.warn("handleErrorEx called", ex);
  }

  @Override
  public void responseReceived(String arg0) {
    log.debug("responseReceivedEx called:: {}", arg0);
  }

  @Override
  public void itemEvicted(String key, Object value) {
    log.warn("Evicting message with ID {} from un-ack'd cache.", key);
  }

  @Override
  public void itemExpired(String key, Object value) {
    log.warn("Expired message with ID {} from un-ack'd cache.", key);
  }

  @Override
  public void itemPut(String key, Object value) { }

  @Override
  public void itemRemoved(String key, Object value) { }

  @Override
  public void itemUpdated(String key, Object value) { }

}
