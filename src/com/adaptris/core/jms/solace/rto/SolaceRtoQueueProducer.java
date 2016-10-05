package com.adaptris.core.jms.solace.rto;

import javax.validation.constraints.NotNull;

import com.adaptris.annotation.AutoPopulated;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceDestination;
import com.adaptris.core.ProduceException;
import com.adaptris.core.RequestReplyProducerImp;
import com.adaptris.core.util.LifecycleHelper;
import com.solacesystems.solclientj.core.SolEnum;
import com.solacesystems.solclientj.core.Solclient;
import com.solacesystems.solclientj.core.SolclientErrorInfo;
import com.solacesystems.solclientj.core.event.MessageCallback;
import com.solacesystems.solclientj.core.event.SessionEvent;
import com.solacesystems.solclientj.core.event.SessionEventCallback;
import com.solacesystems.solclientj.core.handle.Handle;
import com.solacesystems.solclientj.core.handle.MessageHandle;
import com.solacesystems.solclientj.core.handle.SessionHandle;
import com.solacesystems.solclientj.core.resource.Queue;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("solace-rto-queue-producer")
public class SolaceRtoQueueProducer extends RequestReplyProducerImp implements MessageCallback, SessionEventCallback {
  
private transient SessionHandle session;
  
  private transient Queue solaceDestination;
    
  @NotNull
  @AutoPopulated
  private SolaceRtoMessageTranslator messageTranslator;
  
  public SolaceRtoQueueProducer() {
    this.setMessageTranslator(new SolaceRtoTextMessageTranslator());
  }
  
  @Override
  public void init() throws CoreException {
    session = this.retrieveConnection(SolaceRtoConnection.class).createSession(this);
    LifecycleHelper.init(this.getMessageTranslator());
  }

  @Override
  public void start() throws CoreException {
    LifecycleHelper.start(this.getMessageTranslator());
    session.connect();
  }

  @Override
  public void stop() {
    LifecycleHelper.stop(this.getMessageTranslator());
    if((session != null) && (session.isBound())) {
      session.disconnect();
    }
  }

  @Override
  public void close() {
    LifecycleHelper.close(this.getMessageTranslator());
  }

  @Override
  public void prepare() throws CoreException {
  }

  @Override
  public void produce(AdaptrisMessage msg, ProduceDestination destination) throws ProduceException {
    MessageHandle txMessageHandle = null;
    try {
      solaceDestination = Solclient.Allocator.newQueue(destination.getDestination(msg));
      
      txMessageHandle = this.getMessageTranslator().translate(msg);
      // Set the delivery mode for the message.
      txMessageHandle.setMessageDeliveryMode(SolEnum.MessageDeliveryMode.PERSISTENT);
      // Set the destination/topic
      txMessageHandle.setDestination(solaceDestination);
        
      session.send(txMessageHandle);
    } catch (CoreException e) {
      throw new ProduceException(e);
    }
  }

  @Override
  protected long defaultTimeout() {
    return 0;
  }

  @Override
  protected AdaptrisMessage doRequest(AdaptrisMessage msg, ProduceDestination dest, long timeout) throws ProduceException {
    return null;
  }

  public SolaceRtoMessageTranslator getMessageTranslator() {
    return messageTranslator;
  }

  public void setMessageTranslator(SolaceRtoMessageTranslator messageTranslator) {
    this.messageTranslator = messageTranslator;
  }

  @Override
  public void onEvent(SessionHandle sessionHandle) {
    SessionEvent se = sessionHandle.getSessionEvent();

    int sessionEventCode = se.getSessionEventCode();
    long correlationKey = se.getCorrelationKey();

    SolclientErrorInfo solclientErrorInfo = Solclient.getLastErrorInfo();

    switch (sessionEventCode) {
    case SolEnum.SessionEventCode.ACKNOWLEDGEMENT:

        log.info("AdPubAckEventAdapter - Received ACKNOWLEDGEMENT for correlationKey ["+ correlationKey + "]");

        break;
    case SolEnum.SessionEventCode.REJECTED_MSG_ERROR:
        log.info("AdPubAckEventAdapter - Received REJECTED_MSG_ERROR for correlationKey ["+ correlationKey + "] " + solclientErrorInfo);
        break;

    case SolEnum.SessionEventCode.UP_NOTICE:
    case SolEnum.SessionEventCode.TE_UNSUBSCRIBE_OK:
    case SolEnum.SessionEventCode.CAN_SEND:
    case SolEnum.SessionEventCode.RECONNECTING_NOTICE:
    case SolEnum.SessionEventCode.RECONNECTED_NOTICE:
    case SolEnum.SessionEventCode.PROVISION_OK:
    case SolEnum.SessionEventCode.SUBSCRIPTION_OK:
        log.info("AdPubAckEventAdapter - Received SessionEvent [" + se + "]");
        break;

    case SolEnum.SessionEventCode.DOWN_ERROR:
    case SolEnum.SessionEventCode.CONNECT_FAILED_ERROR:
    case SolEnum.SessionEventCode.SUBSCRIPTION_ERROR:
    case SolEnum.SessionEventCode.TE_UNSUBSCRIBE_ERROR:
    case SolEnum.SessionEventCode.PROVISION_ERROR:
        log.info("AdPubAckEventAdapter - Error Received SessionEvent [" + se + "] " + solclientErrorInfo);
        break;

    default:
        log.info("AdPubAckEventAdapter - Received Unrecognized or deprecated event, SessionEvent [" + se + "]");
        break;
    }
  }

  @Override
  public void onMessage(Handle handle) {

  }

}
