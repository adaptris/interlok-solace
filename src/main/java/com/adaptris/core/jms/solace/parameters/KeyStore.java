package com.adaptris.core.jms.solace.parameters;

import com.adaptris.annotation.InputFieldHint;
import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

@XStreamAlias("solace-keystore")
public class KeyStore {
  
  /**
   * This property specifies the key store to use in URL or path format.
   */
  @Getter
  @Setter
  private String filename;
  /**
   * This property specifies the format of the given key store.
   */
  @Getter
  @Setter
  private String format;
  /**
   * This property specifies the key store password to use.
   */
  @Getter
  @Setter
  @InputFieldHint(style = "PASSWORD", external = true)
  private String password;

  
}
