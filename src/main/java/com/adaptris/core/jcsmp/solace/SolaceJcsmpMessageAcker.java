package com.adaptris.core.jcsmp.solace;

import com.adaptris.core.ComponentLifecycle;
import com.adaptris.core.ComponentLifecycleExtension;
import com.solacesystems.jcsmp.BytesXMLMessage;

public interface SolaceJcsmpMessageAcker extends ComponentLifecycle, ComponentLifecycleExtension {
  
  public void setParentWorkflow(SolaceJcsmpWorkflow workflow);

  public void acknowledge(String messageIdentifier);
  
  public void addUnacknowledgedMessage(BytesXMLMessage message, String messageIdentifier);
  
  public int getUnacknowledgedMessageCount();
  
}
