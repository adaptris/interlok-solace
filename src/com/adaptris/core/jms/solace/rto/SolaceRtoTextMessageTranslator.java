package com.adaptris.core.jms.solace.rto;

import java.nio.ByteBuffer;

import org.apache.commons.lang.StringUtils;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.ClosedState;
import com.adaptris.core.ComponentState;
import com.adaptris.core.CoreException;
import com.adaptris.core.DefaultMessageFactory;
import com.solacesystems.solclientj.core.SolEnum;
import com.solacesystems.solclientj.core.Solclient;
import com.solacesystems.solclientj.core.handle.MessageHandle;

public class SolaceRtoTextMessageTranslator implements SolaceRtoMessageTranslator {
  
  private enum ContentMode {
    BRIEF (SolEnum.MessageDumpMode.BRIEF),
    
    FULL(SolEnum.MessageDumpMode.FULL);
    
    ContentMode(int mode) {
      this.setSolaceMode(mode);
    }
    
    private int solaceMode;

    public int getSolaceMode() {
      return solaceMode;
    }

    public void setSolaceMode(int solaceMode) {
      this.solaceMode = solaceMode;
    }
  }
  
  private String uniqueId;
  
  private transient ComponentState state;
  
  private static final String DEFAULT_CONTENT_MODE = "BRIEF";
  
  private String contentMode;
  
  private transient MessageHandle txMessageHandle;
  
  public SolaceRtoTextMessageTranslator() {
    this.setContentMode(DEFAULT_CONTENT_MODE);
    this.changeState(ClosedState.getInstance());
  }

  @Override
  public AdaptrisMessage translate(MessageHandle messageHandle) throws CoreException {
    AdaptrisMessage newMessage = DefaultMessageFactory.getDefaultInstance().newMessage();
    
    newMessage.setContent(messageHandle.dump(ContentMode.valueOf(getContentMode()).getSolaceMode()), newMessage.getContentEncoding());
    
    return newMessage;
  }

  public String getContentMode() {
    return contentMode;
  }

  public void setContentMode(String contentMode) {
    this.contentMode = contentMode;
  }

  @Override
  public MessageHandle translate(AdaptrisMessage adaptrisMessage) throws CoreException {
    ByteBuffer content = ByteBuffer.allocateDirect(adaptrisMessage.getContent().length());
    if(!StringUtils.isEmpty(adaptrisMessage.getContent()))
      content.put(adaptrisMessage.getContent().getBytes());

    content.flip();
    txMessageHandle.setBinaryAttachment(content);
    return txMessageHandle;
  }

  @Override
  public String getUniqueId() {
    return uniqueId;
  }
  
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }

  @Override
  public ComponentState retrieveComponentState() {
    return state;
  }

  @Override
  public void changeState(ComponentState newState) {
    this.state = newState;
  }

  /** @see com.adaptris.core.StateManagedComponent#requestInit() */
  public void requestInit() throws CoreException {
    state.requestInit(this);
  }

  /** @see com.adaptris.core.StateManagedComponent#requestStart() */
  public void requestStart() throws CoreException {
    state.requestStart(this);
  }

  /** @see com.adaptris.core.StateManagedComponent#requestStop() */
  public void requestStop() {
    state.requestStop(this);
  }

  /** @see com.adaptris.core.StateManagedComponent#requestClose() */
  public void requestClose() {
    state.requestClose(this);
  }

  @Override
  public void init() throws CoreException {
    txMessageHandle = Solclient.Allocator.newMessageHandle();
    Solclient.createMessageForHandle(txMessageHandle);
  }

  @Override
  public void start() throws CoreException {
  }

  @Override
  public void stop() {
  }

  @Override
  public void close() {
    if((txMessageHandle != null) && (txMessageHandle.isBound()))
      this.txMessageHandle.destroy();
  }

  @Override
  public void prepare() throws CoreException {
  }

}
