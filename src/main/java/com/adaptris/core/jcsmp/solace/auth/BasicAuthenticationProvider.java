package com.adaptris.core.jcsmp.solace.auth;

import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldHint;
import com.adaptris.core.CoreException;
import com.adaptris.interlok.resolver.ExternalResolver;
import com.adaptris.security.exc.PasswordException;
import com.adaptris.security.password.Password;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.thoughtworks.xstream.annotations.XStreamAlias;

/**
 * <p>
 * A basic {@link AuthenticationProvider} that requires a simple user name and password.
 * </p>
 * @author aaron
 * @config solace-jcsmp-basic-authentication
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP basic authentication provider.", tag="authentication,basic,solace,jcsmp", since="3.11.1")
@XStreamAlias("solace-jcsmp-basic-authentication")
@DisplayOrder(order = {"username", "password"})
public class BasicAuthenticationProvider implements AuthenticationProvider {

  private String username;
  
  @InputFieldHint(style = "PASSWORD", external=true)
  private String password;
  
  @Override
  public JCSMPProperties setConnectionProperties() throws CoreException {
    JCSMPProperties properties = new JCSMPProperties();
    try {
      properties.setProperty(JCSMPProperties.AUTHENTICATION_SCHEME, JCSMPProperties.AUTHENTICATION_SCHEME_BASIC);
      if(getUsername() != null)
        properties.setProperty(JCSMPProperties.USERNAME, getUsername());
      if(getPassword() != null)
        properties.setProperty(JCSMPProperties.PASSWORD, Password.decode(ExternalResolver.resolve(getPassword())));
    } catch (PasswordException pe) {
      throw new CoreException(pe);
    }
    return properties;
  }

  public String getUsername() {
    return username;
  }

  public String getPassword() {
    return password;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public void setPassword(String password) {
    this.password = password;
  }

}
