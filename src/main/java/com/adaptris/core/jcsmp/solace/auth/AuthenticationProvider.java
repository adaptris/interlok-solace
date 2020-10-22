package com.adaptris.core.jcsmp.solace.auth;

import com.adaptris.core.CoreException;
import com.solacesystems.jcsmp.JCSMPProperties;

public interface AuthenticationProvider {
  
  public JCSMPProperties setConnectionProperties() throws CoreException; 

}
