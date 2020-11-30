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

import lombok.Getter;
import lombok.Setter;

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

  /**
   * The username to authenticate your Solace connection.
   */
  @Getter
  @Setter
  private String username;
  
  /**
   * The password used to authenticate your Solace connection.
   */
  @InputFieldHint(style = "PASSWORD", external=true)
  @Getter
  @Setter
  private String password;
  
  @Override
  public JCSMPProperties initConnectionProperties() throws CoreException {
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

}
