package com.adaptris.core.jcsmp.solace;

import javax.validation.constraints.NotNull;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.ObjectUtils;
import com.adaptris.annotation.AdapterComponent;
import com.adaptris.annotation.AdvancedConfig;
import com.adaptris.annotation.ComponentProfile;
import com.adaptris.annotation.DisplayOrder;
import com.adaptris.annotation.InputFieldDefault;
import com.adaptris.core.AdaptrisConnection;
import com.adaptris.core.AllowsRetriesConnection;
import com.adaptris.core.CoreException;
import com.adaptris.core.jcsmp.solace.auth.AuthenticationProvider;
import com.adaptris.core.util.ExceptionHelper;
import com.adaptris.security.exc.PasswordException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.Session;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;


/**
 * <p>
 * The Interlok {@link AdaptrisConnection} that will allow us to connect to your Solace router via the JCSMP api.
 * </p>
 * <p>
 * Simply supply the host, vpn name, user and password.
 * </p>
 * @author aaron
 * @config solace-jcsmp-connection
 */
@AdapterComponent
@ComponentProfile(summary="A Solace native JCSMP connection used to create JCSMP sessions to your Solace appliance.", tag="connection,solace,jcsmp", since="3.9.3")
@XStreamAlias("solace-jcsmp-connection")
@DisplayOrder(order = {"host", "vpn", "username", "password"})
public class SolaceJcsmpConnection extends AllowsRetriesConnection implements SolaceJcsmpSessionCreator {

  /**
   * The tcp address to your Solace router, such as "tcp://localhost:55555".
   * @param host
   */
  @NotNull
  @Getter
  @Setter
  private String host;

  /**
   * The Solace VPN name, such as "default".
   * @param vpn
   */
  @NotNull
  @Getter
  @Setter
  private String vpn;

  /**
   * How would you like to be authenticated when connecting to Solace.
   */
  @NotNull
  @Getter
  @Setter
  private AuthenticationProvider authenticationProvider;

  @Getter(AccessLevel.PACKAGE)
  @Setter(AccessLevel.PACKAGE)
  private transient JCSMPFactory jcsmpFactory;

  @AdvancedConfig
  @InputFieldDefault(value = "false")
  @Getter
  @Setter
  private Boolean additionalDebug;

  public SolaceJcsmpConnection() {
    super();
  }

  @Override
  public JCSMPSession createSession() throws Exception {
    return jcsmpFactory().createSession(generateJcsmpProperties());
  }

  @Override
  protected void prepareConnection() throws CoreException {
  }

  @Override
  protected void initConnection() throws CoreException {
    try {
      if(getConnectionErrorHandler() == null) {
        setConnectionErrorHandler(new SolaceJcsmpConnectionErrorHandler());
        getConnectionErrorHandler().registerConnection(this);
      }
      connect().closeSession();
    } catch (Exception e) {
      throw ExceptionHelper.wrapCoreException(e);
    }
  }

  private Session connect() throws Exception {
    Session session = null;
    int attemptCount = 0;

    while (session == null) {
      try {
        attemptCount++;
        if (additionalDebug()) {
          log.trace("Attempting connection to Solace VPN [{}] at [{}]", getVpn(), getHost());
        }
        session = createSession();
      }
      catch (Exception e) {
        if (attemptCount == 1) {
          if (logWarning(attemptCount)) {
            log.warn("Connection attempt [{}] failed for Solace VPN [{}] at [{}]", attemptCount, getVpn(), getHost(), e);
          }
        }

        if (connectionAttempts() != -1 && attemptCount >= connectionAttempts()) {
          log.error("Failed to connect to Solace VPN [{}] at [{}]", getVpn(), getHost(), e);
          throw e;
        }
        else {
          log.warn("Attempt [{}] failed for Solace VPN [{}] at [{}], retrying", attemptCount, getVpn(), getHost());
          log.info(createLoggingStatement(attemptCount));
          Thread.sleep(connectionRetryInterval());
          continue;
        }
      }
    }
    return session;
  }

  @Override
  protected void startConnection() throws CoreException {
  }

  @Override
  protected void stopConnection() {
  }

  @Override
  protected void closeConnection() {
  }

  private JCSMPProperties generateJcsmpProperties() throws CoreException, PasswordException {
    JCSMPProperties properties = new JCSMPProperties();
    if(getAuthenticationProvider() != null) {
      properties = getAuthenticationProvider().initConnectionProperties();
    }
    properties.setProperty(JCSMPProperties.HOST, getHost());
    properties.setProperty(JCSMPProperties.VPN_NAME, getVpn());

    return properties;
  }

  JCSMPFactory jcsmpFactory() {
    return ObjectUtils.defaultIfNull(getJcsmpFactory(), JCSMPFactory.onlyInstance());
  }

  protected boolean additionalDebug() {
    return BooleanUtils.toBooleanDefaultIfNull(getAdditionalDebug(), false);
  }

}
