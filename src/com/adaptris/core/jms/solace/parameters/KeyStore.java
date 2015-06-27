package com.adaptris.core.jms.solace.parameters;

import com.adaptris.annotation.InputFieldHint;
import com.thoughtworks.xstream.annotations.XStreamAlias;

@XStreamAlias("solace-keystore")
public class KeyStore {
  private String filename;
  private String format;
  
  @InputFieldHint(style = "PASSWORD")
  private String password;

  public String getFilename() {
    return filename;
  }
  
  /**
   * This property specifies the key store to use in URL or path format.
   */
  public void setFilename(String filename) {
    this.filename = filename;
  }
  
  public String getFormat() {
    return format;
  }
  
  /**
   * This property specifies the format of the given key store.
   */
  public void setFormat(String format) {
    this.format = format;
  }
  
  public String getPassword() {
    return password;
  }
  
  /**
   * This property specifies the key store password to use.
   */
  public void setPassword(String password) {
    this.password = password;
  }
  
}
