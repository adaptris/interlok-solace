package com.adaptris.core.jcsmp.solace;

import java.text.DecimalFormat;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.ProduceException;
import com.adaptris.core.ServiceException;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.jcsmp.solace.util.Timer;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP workflow, to be used with JCSMP consumers and producers.", tag="workflow,solace,jcsmp")
@XStreamAlias("solace-jcsmp-workflow")
public class SolaceJcsmpWorkflow extends StandardWorkflow {
  
  protected static final DecimalFormat format = new DecimalFormat("###,###,###.00");

  private SolaceJcsmpMessageAcker messageAcker;
  
  public SolaceJcsmpWorkflow() {
    super();
  }
  
  protected void handleMessage(final AdaptrisMessage msg, boolean clone) {
    Timer.start("OnReceive", "OnMessagePrep", 1000);
    AdaptrisMessage wip = addConsumeLocation(msg);
    workflowStart(msg);
    processingStart(msg);
    Timer.stop("OnReceive", "OnMessagePrep");
    try {
      Timer.start("OnReceive", "handleMessage", 1000);
//      log.debug("start processing msg [{}]", messageLogger().toString(msg));
      
      Timer.start("OnReceive", "OnMessageEvent", 1000);
      wip.getMessageLifecycleEvent().setChannelId(obtainChannel().getUniqueId());
      wip.getMessageLifecycleEvent().setWorkflowId(obtainWorkflowId());
      wip.addEvent(getConsumer(), true); // initial receive event
      Timer.stop("OnReceive", "OnMessageEvent");
      Timer.start("OnReceive", "Services", 1000);
      getServiceCollection().doService(wip);
      Timer.stop("OnReceive", "Services");
      Timer.start("OnReceive", "doProduce", 1000);
      doProduce(wip);
      Timer.stop("OnReceive", "doProduce");
      Timer.stop("OnReceive", "handleMessage");
//      logSuccess(wip, 0l);
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
    Timer.start("OnReceive", "WorkflowEnd", 1000);
    workflowEnd(msg, wip);
    Timer.stop("OnReceive", "WorkflowEnd");
  }
  
  protected void logSuccess(AdaptrisMessage msg, long start) {
    log.info("message [{}] processed in [{}] ms, avg [{}] ms", msg.getUniqueId(), 
        format.format(Timer.getLastTimingMs("OnReceive", "handleMessage")), 
        format.format(Timer.getAvgTimingMs("OnReceive", "handleMessage")));
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
