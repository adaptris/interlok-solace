package com.adaptris.core.jcsmp.solace;

import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import com.adaptris.core.ConnectionErrorHandler;
import com.adaptris.core.MockBaseTest;
import com.adaptris.core.fs.FsConsumer;
import com.adaptris.core.jcsmp.solace.auth.BasicAuthenticationProvider;
import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;
import com.solacesystems.jcsmp.InvalidPropertiesException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;

public class SolaceJcsmpConnectionTest extends MockBaseTest {

  private SolaceJcsmpConnection connection;

  @Mock private JCSMPFactory mockJcsmpFactory;

  @Mock private JCSMPSession mockSession;

  @Mock private SolaceJcsmpQueueConsumer mockConsumer;

  @Mock private FsConsumer mockWrongTypeConsumer;

  @Mock private ConnectionErrorHandler mockConnectionErrorHandler;

  @Before
  public void setUp() throws Exception {
    connection = new SolaceJcsmpConnection();
    connection.setJcsmpFactory(mockJcsmpFactory);
    connection.setConnectionAttempts(2);
    connection.setConnectionRetryInterval(new TimeInterval(100l, TimeUnit.MILLISECONDS));
    connection.setHost("myHost");
    BasicAuthenticationProvider provider = new BasicAuthenticationProvider();
    provider.setUsername("myUsername");
    provider.setPassword("myPassword");
    connection.setAuthenticationProvider(provider);
    connection.setVpn("myVpn");
    connection.setAdditionalDebug(true);
    connection.setConnectionErrorHandler(mockConnectionErrorHandler);

    connection.getAdvancedProperties().setAckEventMode("SUPPORTED_ACK_EVENT_MODE_PER_MSG");
    connection.getAdvancedProperties().setApplicationDescription("Interlok");
    connection.getAdvancedProperties().setCalculateMessageExpiration(false);
    connection.getAdvancedProperties().setClientAckMode(true);
    connection.getAdvancedProperties().setGdReconnectFailAction("GD_RECONNECT_FAIL_ACTION_AUTO_RETRY");
    connection.getAdvancedProperties().setGenerateRcvTimestamps(false);
    connection.getAdvancedProperties().setGenerateSenderId(false);
    connection.getAdvancedProperties().setGenerateSendTimestamps(false);
    connection.getAdvancedProperties().setGenerateSequenceNumbers(false);
    connection.getAdvancedProperties().setIgnoreDuplicateSubscriptionError(false);
    connection.getAdvancedProperties().setIgnoreSubscriptionNotFoundError(false);
    connection.getAdvancedProperties().setKrbServiceName(null);
    connection.getAdvancedProperties().setMessageCallbackOnReactor(false);
    connection.getAdvancedProperties().setNoLocal(false);
    connection.getAdvancedProperties().setPubAckTime(1000);
    connection.getAdvancedProperties().setPubAckWindowSize(1);
    connection.getAdvancedProperties().setPubUseIntermediateDirectBuf(true);
    connection.getAdvancedProperties().setReapplySubscriptions(false);
    connection.getAdvancedProperties().setReapplySubscriptions(false);
    connection.getAdvancedProperties().setSubAckTime(1000);
    connection.getAdvancedProperties().setSubAckWindowSize(1);
    connection.getAdvancedProperties().setSubAckWindowThreshold(60);
    
    connection.prepareConnection();

    when(mockJcsmpFactory.createSession(any(JCSMPProperties.class)))
    .thenReturn(mockSession);
  }

  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(connection);
  }

  @Test
  public void testConnectionSuccess() throws Exception {
    LifecycleHelper.initAndStart(connection);

    assertNotNull(connection.createSession());
  }

  @Test
  public void testConnectionSuccessWithAuthProvider() throws Exception {
    BasicAuthenticationProvider authenticationProvider = new BasicAuthenticationProvider();
    authenticationProvider.setUsername("username");
    authenticationProvider.setPassword("password");

    connection.setAuthenticationProvider(authenticationProvider);

    LifecycleHelper.initAndStart(connection);

    assertNotNull(connection.createSession());
  }

  @Test
  public void testConnectionInitCreatesErrorHandler() throws Exception {
    connection.setConnectionErrorHandler(null);

    LifecycleHelper.initAndStart(connection);

    assertNotNull(connection.getConnectionErrorHandler());
  }

  @Test
  public void testConnectionRetrySuccess() throws Exception {
    when(mockJcsmpFactory.createSession(any(JCSMPProperties.class)))
    .thenThrow(new InvalidPropertiesException("expected"))
    .thenReturn(mockSession);

    LifecycleHelper.initAndStart(connection);

    verify(mockJcsmpFactory, times(2)).createSession(any(JCSMPProperties.class));
  }

  @Test
  public void testConnectionRetryFails() throws Exception {
    when(mockJcsmpFactory.createSession(any(JCSMPProperties.class)))
    .thenThrow(new InvalidPropertiesException("expected"));

    try {
      LifecycleHelper.initAndStart(connection);
    } catch (Exception ex) {
      // expected
      verify(mockJcsmpFactory, times(2)).createSession(any(JCSMPProperties.class));
    }
  }

}
