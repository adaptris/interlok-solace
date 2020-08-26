package com.adaptris.core.jcsmp.solace.translator;

import com.adaptris.core.AdaptrisMessage;
import com.solacesystems.jcsmp.XMLMessage;

public interface SolaceJcsmpMessageTranslator {

  public AdaptrisMessage translate(XMLMessage message) throws Exception;
  
  public XMLMessage translate(AdaptrisMessage message) throws Exception;
  
}
