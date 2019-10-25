package com.adaptris.core.jcsmp.solace;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StandardWorkflowImpl;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP workflow, to be used with JCSMP consumers and producers.", tag="workflow,solace,jcsmp")
@XStreamAlias("solace-jcsmp-workflow")
public class SolaceJcsmpWorkflow extends StandardWorkflowImpl {

  private SolaceJcsmpMessageAcker messageAcker;
  
  public SolaceJcsmpWorkflow() {
    super();
  }
  
  @Override
  public void onAdaptrisMessage(AdaptrisMessage msg) {
    if (!obtainChannel().isAvailable()) {
      handleChannelUnavailable(msg);
    }
    else {
      handleMessage(msg, true);
    }
  }
  
  @Override
  protected void resubmitMessage(AdaptrisMessage msg) {
    handleMessage(msg, true);
  }

  protected void handleMessage(final AdaptrisMessage msg, boolean clone) {
    AdaptrisMessage wip = addConsumeLocation(msg);
    workflowStart(msg);
    try {
      long start = System.currentTimeMillis();
      log.debug("start processing msg [{}]", messageLogger().toString(msg));
      if (clone) {
        wip = (AdaptrisMessage) msg.clone(); // retain orig. for error handling
      }
      wip.getMessageLifecycleEvent().setChannelId(obtainChannel().getUniqueId());
      wip.getMessageLifecycleEvent().setWorkflowId(obtainWorkflowId());
      wip.addEvent(getConsumer(), true); // initial receive event
      getServiceCollection().doService(wip);
      doProduce(wip);
      logSuccess(wip, start);
    }
    catch (ServiceException e) {
      handleBadMessage("Exception from ServiceCollection", e, copyExceptionHeaders(wip, msg));
    }
    catch (ProduceException e) {
      wip.addEvent(getProducer(), false); // generate event
      handleBadMessage("Exception producing msg", e, copyExceptionHeaders(wip, msg));
      handleProduceException();
    }
    catch (Exception e) { // all other Exc. inc. runtime
      handleBadMessage("Exception processing message", e, copyExceptionHeaders(wip, msg));
    }
    finally {
      sendMessageLifecycleEvent(wip);
    }
    workflowEnd(msg, wip);
  }
  
  @Override
  protected void prepareWorkflow() throws CoreException {
    if(this.getConsumer() instanceof SolaceJcsmpQueueConsumer)
      ((SolaceJcsmpQueueConsumer) this.getConsumer()).setMessageAcker(this.getMessageAcker());
    
    this.getMessageAcker().setParentWorkflow(this);
    LifecycleHelper.prepare(this.getMessageAcker());
    super.prepareWorkflow();
  }
  
  @Override
  protected void initialiseWorkflow() throws CoreException {
    super.initialiseWorkflow();
    LifecycleHelper.init(this.getMessageAcker());
  }

  @Override
  protected void startWorkflow() throws CoreException {
    super.startWorkflow();
    LifecycleHelper.start(this.getMessageAcker());
  }

  @Override
  protected void stopWorkflow() {
    super.stopWorkflow();
    LifecycleHelper.stop(this.getMessageAcker());
  }

  @Override
  protected void closeWorkflow() {
    super.closeWorkflow();
    LifecycleHelper.close(this.getMessageAcker());
  }

  public SolaceJcsmpMessageAcker getMessageAcker() {
    return messageAcker;
  }

  public void setMessageAcker(SolaceJcsmpMessageAcker messageAcker) {
    this.messageAcker = messageAcker;
  }
  
}
