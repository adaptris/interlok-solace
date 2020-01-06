package com.adaptris.core.jcsmp.solace;

import com.adaptris.core.AdaptrisMessage;
import com.solacesystems.jcsmp.BytesXMLMessage;

public interface SolaceJcsmpMessageTranslator {

  public AdaptrisMessage translate(BytesXMLMessage message) throws Exception;
  
  public BytesXMLMessage translate(AdaptrisMessage message) throws Exception;
  
}
