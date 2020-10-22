package com.adaptris.core.jcsmp.solace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.JCSMPException;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.JCSMPProperties;
import com.solacesystems.jcsmp.JCSMPSession;
import com.solacesystems.jcsmp.JCSMPStreamingPublishEventHandler;
import com.solacesystems.jcsmp.XMLMessageProducer;
import com.solacesystems.jcsmp.transaction.TransactedSession;

public class SolaceJcsmpSessionHelperTest {

  private SolaceJcsmpSessionHelper sessionHelper;
  
  private SolaceJcsmpConnection connection;
  
  @Mock private JCSMPFactory mockJcsmpFactory;
  
  @Mock private JCSMPSession mockSession;
  
  @Mock private TransactedSession mockTransactedSession;
  
  @Mock private BytesXMLMessage mockMessage;
  
  @Mock private JCSMPStreamingPublishEventHandler eventHandler;
  
  @Mock private XMLMessageProducer mockMessageProducer;
  
  @Before
  public void setup() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    when(mockJcsmpFactory.createSession(any(JCSMPProperties.class)))
        .thenReturn(mockSession);
    
    when(mockSession.createTransactedSession())
        .thenReturn(mockTransactedSession);
    when(mockSession.getMessageProducer(eventHandler))
        .thenReturn(mockMessageProducer);
    
    connection = new SolaceJcsmpConnection();
    connection.setJcsmpFactory(mockJcsmpFactory);
    
    sessionHelper = new SolaceJcsmpSessionHelper();
    sessionHelper.setConnection(connection);
    sessionHelper.setTransacted(false);
  }
  
  @After
  public void tearDown() throws Exception {
    
  }
  
  @Test
  public void testCreateNormalSession() throws Exception {
    assertFalse(sessionHelper.isSessionActive());
    
    sessionHelper.createSession();
    
    verify(mockJcsmpFactory).createSession(any(JCSMPProperties.class));
  }
  
  @Test
  public void testCreateTransactedSession() throws Exception {
    sessionHelper.setTransacted(true);
    
    assertFalse(sessionHelper.isSessionActive());
    
    sessionHelper.createSession();
    
    verify(mockJcsmpFactory).createSession(any(JCSMPProperties.class));
    verify(mockSession).createTransactedSession();
  }
  
  @Test
  public void testCommitTransactedSession() throws Exception {
    sessionHelper.setTransacted(true);
    
    sessionHelper.createSession();
    sessionHelper.commit(mockMessage);
    
    verify(mockTransactedSession).commit();
    verify(mockTransactedSession, times(0)).rollback();
  }
  
  @Test
  public void testRollbackTransactedSession() throws Exception {
    sessionHelper.setTransacted(true);
    
    sessionHelper.createSession();
    sessionHelper.rollback();
    
    verify(mockTransactedSession).rollback();
  }
  
  @Test
  public void testRollbackNormalSession() throws Exception {    
    sessionHelper.createSession();
    sessionHelper.rollback();
    
    verify(mockTransactedSession, times(0)).rollback();
  }
  
  @Test
  public void testRollbackFailsNoExceptionThrown() throws Exception {
    doThrow(new JCSMPException("expected"))
        .when(mockTransactedSession).rollback();
    
    sessionHelper.setTransacted(true);
    
    sessionHelper.createSession();
    sessionHelper.rollback(); // fails if exception thrown.
  }
  
  @Test
  public void testCommitFailsTransactedSession() throws Exception {
    doThrow(new JCSMPException("expected"))
        .when(mockTransactedSession).commit();
    
    sessionHelper.setTransacted(true);
    
    sessionHelper.createSession();
    sessionHelper.commit(mockMessage);
    
    verify(mockTransactedSession).commit();
    verify(mockTransactedSession).rollback();
  }
  
  @Test
  public void testCloseTransactedSession() throws Exception {
    sessionHelper.setTransacted(true);
    
    sessionHelper.createSession();
    sessionHelper.close();
    
    verify(mockTransactedSession).close();
  }
  
  @Test
  public void testCloseNormalSession() throws Exception {
    sessionHelper.createSession();
    sessionHelper.close();
    
    verify(mockSession).closeSession();
  }
  
  @Test
  public void testCreateMessageProducer() throws Exception {
    sessionHelper.createSession();
    
    XMLMessageProducer messageProducer = sessionHelper.createMessageProducer(eventHandler);
    
    verify(mockSession).getMessageProducer(eventHandler);
    assertEquals(mockMessageProducer, messageProducer);
  }
}
