package com.adaptris.core.jcsmp.solace;

import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.Mockito.verify;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.Channel;
import com.adaptris.core.StateManagedComponent;
import com.adaptris.core.util.LifecycleHelper;

public class SolaceJcsmpConnectionErrorHandlerTest {

  private SolaceJcsmpConnectionErrorHandler connectionErrorhandler;
  
  private Channel mockChannel;
  
  @Mock private SolaceJcsmpConnection mockConnection;
    
  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    mockChannel = new Channel();
    mockChannel.setConsumeConnection(mockConnection);
    
    connectionErrorhandler = new SolaceJcsmpConnectionErrorHandler();
    connectionErrorhandler.registerConnection(mockConnection);
    
    Set<StateManagedComponent> comps = new HashSet<StateManagedComponent>();
    comps.add(mockChannel);
    
    when(mockConnection.retrieveExceptionListeners())
        .thenReturn(comps);
    when(mockConnection.getConnectionErrorHandler())
        .thenReturn(connectionErrorhandler);
    
    LifecycleHelper.prepare(mockChannel);
    LifecycleHelper.initAndStart(mockChannel);
  }
  
  @After
  public void tearDown() throws Exception {
    LifecycleHelper.stopAndClose(connectionErrorhandler);
  }
  
  @Test
  public void testHandleException() {
    connectionErrorhandler.handleConnectionException();
    
    verify(mockConnection, atLeast(1)).requestStop();
  }
  
}
