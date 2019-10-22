package com.adaptris.core.jcsmp.solace;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.TextMessage;

public class SolaceJcsmpBytesMessageTranslatorTest {
  
  private static final String MESSAGE_CONTENT = "My message content.";
  
  private SolaceJcsmpBytesMessageTranslator translator;
  
  @Mock private JCSMPFactory mockJcsmpFactory;

  @Mock private TextMessage mockTextMessage;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    translator = new SolaceJcsmpBytesMessageTranslator();
    translator.setJcsmpFactory(mockJcsmpFactory);
    
    when(mockJcsmpFactory.createMessage(TextMessage.class))
        .thenReturn(mockTextMessage);
    when(mockTextMessage.getBytes())
        .thenReturn(MESSAGE_CONTENT.getBytes());
  }
  
  @After
  public void tearDown() throws Exception {
    
  }
  
  @Test
  public void testTranslateAdaptrisTextMessage() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);
    
    TextMessage translatedMessage = (TextMessage) translator.translate(adaptrisMessage);
    
    verify(translatedMessage).setText(MESSAGE_CONTENT);
  }
  
  @Test
  public void testTranslateSolaceTextMessage() throws Exception {
    AdaptrisMessage adaptrisMessage = translator.translate(mockTextMessage);
    
    assertEquals(MESSAGE_CONTENT, adaptrisMessage.getContent());
  }

}
