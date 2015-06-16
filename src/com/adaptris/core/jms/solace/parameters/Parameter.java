package com.adaptris.core.jms.solace.parameters;

import com.solacesystems.jms.SolConnectionFactory;

/**
 * Helper class for parameter to apply to a solace connection factory. Used
 * for grouping similar parameters to prevent the configuration dialog from
 * exploding with options users won't need most of the time.
 * 
 * @author gerco
 */
public interface Parameter {

  public void apply(SolConnectionFactory cf);
  
}
