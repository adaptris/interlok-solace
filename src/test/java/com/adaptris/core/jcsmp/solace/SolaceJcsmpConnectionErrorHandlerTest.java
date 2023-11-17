package com.adaptris.core.jcsmp.solace;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.adaptris.core.Channel;
import com.adaptris.core.MockBaseTest;
import com.adaptris.core.StateManagedComponent;
import com.adaptris.core.util.LifecycleHelper;

public class SolaceJcsmpConnectionErrorHandlerTest extends MockBaseTest {

  private SolaceJcsmpConnectionErrorHandler connectionErrorhandler;

  private Channel mockChannel;

  @Mock
  private SolaceJcsmpConnection mockConnection;

  @BeforeEach
  public void setUp() throws Exception {
    mockChannel = new Channel();
    mockChannel.setConsumeConnection(mockConnection);

    connectionErrorhandler = new SolaceJcsmpConnectionErrorHandler();
    connectionErrorhandler.registerConnection(mockConnection);

    Set<StateManagedComponent> comps = new HashSet<>();
    comps.add(mockChannel);

    when(mockConnection.retrieveExceptionListeners()).thenReturn(comps);
    when(mockConnection.getConnectionErrorHandler()).thenReturn(connectionErrorhandler);

    LifecycleHelper.prepare(mockChannel);
    LifecycleHelper.initAndStart(mockChannel);
  }

  @AfterEach
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(connectionErrorhandler);
  }

  @Test
  public void testHandleException() {
    connectionErrorhandler.handleConnectionException();

    verify(mockConnection, atLeast(1)).requestStop();
  }

}
