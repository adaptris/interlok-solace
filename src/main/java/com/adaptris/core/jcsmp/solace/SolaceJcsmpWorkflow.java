package com.adaptris.core.jcsmp.solace;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.core.CoreException;
import com.adaptris.core.PoolingWorkflow;
import com.adaptris.core.util.LifecycleHelper;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP workflow, to be used with JCSMP consumers and producers.", tag="workflow,solace,jcsmp")
@XStreamAlias("solace-jcsmp-workflow")
public class SolaceJcsmpWorkflow extends PoolingWorkflow {

  private SolaceJcsmpMessageAcker messageAcker;
  
  public SolaceJcsmpWorkflow() {
    super();
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
