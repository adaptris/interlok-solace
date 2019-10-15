package com.adaptris.core.jcsmp.solace;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.util.LifecycleHelper;
import com.adaptris.util.TimeInterval;
import com.solacesystems.jcsmp.InvalidPropertiesException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;

public class SolaceJcsmpConnectionTest {
  
  private SolaceJcsmpConnection connection;
  
  @Mock private JCSMPFactory mockJcsmpFactory;

  @Mock private JCSMPSession mockSession;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    connection = new SolaceJcsmpConnection();
    connection.setJcsmpFactory(mockJcsmpFactory);
    connection.setConnectionAttempts(2);
    connection.setConnectionRetryInterval(new TimeInterval(100l, TimeUnit.MILLISECONDS));
    
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
