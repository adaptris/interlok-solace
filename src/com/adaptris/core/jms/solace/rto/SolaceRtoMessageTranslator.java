package com.adaptris.core.jms.solace.rto;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.CoreException;
import com.adaptris.core.StateManagedComponent;
import com.solacesystems.solclientj.core.handle.MessageHandle;

public interface SolaceRtoMessageTranslator extends StateManagedComponent {

  AdaptrisMessage translate(MessageHandle messageHandle) throws CoreException;
  
  MessageHandle translate(AdaptrisMessage adaptrisMessage) throws CoreException;
  
}
