package com.adaptris.core.jcsmp.solace;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.Channel;
import com.adaptris.core.CoreConstants;
import com.adaptris.core.Workflow;
import com.adaptris.core.WorkflowInterceptor;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * This implementation of the {@link SolaceJcsmpMessageAcker} should be used when you're not bridging messages
 * between Solace end points using Jcsmp.  Essentially once the Interlok workflow completes, which includes all services and the producer,
 * this class will fire, acknowledging the originally consumed Solace Jcsmp message.
 * </p>
 * @author aaron
 * @version=”3.9.3”
 * @config solace-jcsmp-workflow-message-acker
 * 
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP component used to acknowledge consumed messages once the workflow completes processing.", tag="ack,solace,jcsmp")
@XStreamAlias("solace-jcsmp-workflow-message-acker")
public class SolaceJcsmpInterceptorMessageAcker extends SolaceJcsmpBaseMessageAcker implements WorkflowInterceptor {
  
  private String uniqueId;

  @Override
  public void workflowStart(AdaptrisMessage inputMsg) {
  }

  @Override
  public void workflowEnd(AdaptrisMessage inputMsg, AdaptrisMessage outputMsg) {
    if(wasSuccessful(inputMsg, outputMsg)) {
      this.acknowledge(inputMsg.getUniqueId());
    }
  }
  
  @Override
  public void setParentWorkflow(SolaceJcsmpWorkflow parentWorkflow) {
    super.setParentWorkflow(parentWorkflow);
    this.getParentWorkflow().getInterceptors().add(this);
  }
  
  @Override
  public void registerParentChannel(Channel channel) {
  }

  @Override
  public void registerParentWorkflow(Workflow workflow) {
  }
  
  @Override
  public String getUniqueId() {
    return uniqueId;
  }
  
  public void setUniqueId(String uniqueId) {
    this.uniqueId = uniqueId;
  }
  
  protected boolean wasSuccessful(AdaptrisMessage... msgs) {
    boolean result = true;
    for (AdaptrisMessage msg : msgs) {
      if (msg.getObjectHeaders().containsKey(CoreConstants.OBJ_METADATA_EXCEPTION)) {
        result = false;
        break;
      }
    }
    return result;
  }

}
