package com.adaptris.core.jcsmp.solace;

import com.adaptris.core.CoreException;
import com.adaptris.core.StandardWorkflow;
import com.adaptris.core.util.LifecycleHelper;

public class SolaceJcsmpWorkflow extends StandardWorkflow {

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
