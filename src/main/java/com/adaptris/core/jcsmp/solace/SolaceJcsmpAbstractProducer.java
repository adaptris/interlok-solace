package com.adaptris.core.jcsmp.solace;

import java.util.HashMap;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.ObjectUtils;

import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.AutoPopulated;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ProduceOnlyProducerImp;
import com.adaptris.core.jcsmp.solace.translator.SolaceJcsmpMessageTranslator;
import com.adaptris.core.jcsmp.solace.translator.SolaceJcsmpTextMessageTranslator;
import com.adaptris.core.util.ExceptionHelper;
import com.solacesystems.jcsmp.Destination;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.XMLMessage;
import com.solacesystems.jcsmp.XMLMessageProducer;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

public abstract class SolaceJcsmpAbstractProducer extends ProduceOnlyProducerImp {

  /**
   * The message translator is responsible for translating the Solace JCSMP message object
   * into an {@link AdaptrisMessage} and the reverse.  The translator will typically handle the payload and the headers/metadata.
   * @param messageTranslator
   */
  @NotNull
  @AutoPopulated
  @Getter
  @Setter
  private SolaceJcsmpMessageTranslator messageTranslator;

  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private transient JCSMPFactory jcsmpFactory;

  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private transient XMLMessageProducer messageProducer;

  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private transient Map<String, Destination> destinationCache;
  
  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private transient SolaceJcsmpProduceEventHandler asynEventHandler;
  
  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private transient SolaceJcsmpSessionHelper sessionHelper;

  @AdvancedConfig(rare=true)
  @InputFieldDefault(value = "false")
  private Boolean traceLogTimings;

  public SolaceJcsmpAbstractProducer() {
    setMessageTranslator(new SolaceJcsmpTextMessageTranslator());
    setDestinationCache(new HashMap<String, Destination>());
    setAsynEventHandler(new SolaceJcsmpProduceEventHandler(this));
    setSessionHelper(new SolaceJcsmpSessionHelper());
  }

  @Override
  public void init() throws CoreException {
    this.getAsynEventHandler().init();
    getDestinationCache().clear();
    setMessageProducer(null);
    getSessionHelper().setConnection(retrieveConnection(SolaceJcsmpConnection.class));
    getSessionHelper().setTransacted(false);
  }
  
  @Override
  public void stop() {
    getSessionHelper().close();
  }

  @Override
  public void doProduce(AdaptrisMessage msg, String queueOrTopic) throws ProduceException {
    try {
      Destination dest = generateDestination(msg, queueOrTopic);

      XMLMessageProducer jcsmpMessageProducer = messageProducer(msg);
      XMLMessage translatedMessage = getMessageTranslator().translate(msg);
      
      translatedMessage.setCorrelationKey(msg.getUniqueId());

      jcsmpMessageProducer.send(translatedMessage, dest);
      getAsynEventHandler().addUnAckedMessage(translatedMessage.getMessageId(), msg);
      // Standard workflow will attempt to execute this after the produce, 
      // let's remove them so it's handled by our async event handler.
      msg.getObjectHeaders().remove(CoreConstants.OBJ_METADATA_ON_SUCCESS_CALLBACK);
      msg.getObjectHeaders().remove(CoreConstants.OBJ_METADATA_ON_FAILURE_CALLBACK);
    } catch (Exception ex) {
      setMessageProducer(null);
      throw ExceptionHelper.wrapProduceException(ex);
    }
  }

  protected abstract Destination generateDestination(AdaptrisMessage msg, String queueOrTopic)
      throws Exception;

  JCSMPSession session() throws Exception {
    if(!getSessionHelper().isSessionActive())
      getSessionHelper().createSession();

    return getSessionHelper().getSession();
  }

  XMLMessageProducer messageProducer(AdaptrisMessage msg) throws JCSMPException, Exception {
    if((getMessageProducer() == null) || (session() == null))
      setMessageProducer(session().getMessageProducer(getAsynEventHandler()));
    return getMessageProducer();
  }

  JCSMPFactory jcsmpFactory() {
    return ObjectUtils.defaultIfNull(getJcsmpFactory(), JCSMPFactory.onlyInstance());
  }

}
