package com.adaptris.core.jcsmp.solace;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.TextMessage;

public class SolaceJcsmpBytesMessageTranslatorTest {
  
  private static final String MESSAGE_CONTENT = "My message content.";
  
  private SolaceJcsmpBytesMessageTranslator translator;
  
  @Mock private JCSMPFactory mockJcsmpFactory;

  @Mock private TextMessage mockTextMessage;
  
  private Long timestamp;
  
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    translator = new SolaceJcsmpBytesMessageTranslator();
    translator.setJcsmpFactory(mockJcsmpFactory);
    
    timestamp = new Long(System.currentTimeMillis());
    
    when(mockJcsmpFactory.createMessage(TextMessage.class))
        .thenReturn(mockTextMessage);
    when(mockTextMessage.getBytes())
        .thenReturn(MESSAGE_CONTENT.getBytes());
    when(mockTextMessage.getPriority())
        .thenReturn(3);
    when(mockTextMessage.getSenderTimestamp())
        .thenReturn(timestamp);
    when(mockTextMessage.getApplicationMessageId())
        .thenReturn("xyz");
    when(mockTextMessage.getDiscardIndication())
        .thenReturn(true);
  }
  
  @After
  public void tearDown() throws Exception {
    
  }
  
  @Test
  public void testTranslateAdaptrisTextMessage() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);
    
    TextMessage translatedMessage = (TextMessage) translator.translate(adaptrisMessage);
    
    assertEquals(MESSAGE_CONTENT, translatedMessage.getText());
  }
  
  @Test
  public void testTranslateAdaptrisTextMessageWithDeliveryMode() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);
    
    translator.setDeliveryMode("NON_PERSISTENT");
    TextMessage translatedMessage = (TextMessage) translator.translate(adaptrisMessage);
    
    assertEquals(MESSAGE_CONTENT, translatedMessage.getText());
    assertEquals(DeliveryMode.NON_PERSISTENT, translatedMessage.getDeliveryMode());
  }
  
  @Test
  public void testTranslateAdaptrisTextMessageWithMappings() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);
    adaptrisMessage.addMessageHeader("app-message-id", "123");
    adaptrisMessage.addMessageHeader("sender-timestamp", Long.toString(timestamp));
    adaptrisMessage.addMessageHeader("priority", "1");
    adaptrisMessage.addMessageHeader("dmq-eligible", "true");
    
    SolaceJcsmpMetadataMapping map1 = new SolaceJcsmpMetadataMapping("app-message-id", "ApplicationMessageId");
    SolaceJcsmpMetadataMapping map2 = new SolaceJcsmpMetadataMapping("sender-timestamp", "SenderTimestamp", "Long");
    SolaceJcsmpMetadataMapping map3 = new SolaceJcsmpMetadataMapping("priority", "Priority", "Integer");
    SolaceJcsmpMetadataMapping map4 = new SolaceJcsmpMetadataMapping("dmq-eligible", "DMQEligible", "Boolean");
    
    translator.getMappings().add(map1);
    translator.getMappings().add(map2);
    translator.getMappings().add(map3);
    translator.getMappings().add(map4);
    
    TextMessage translatedMessage = (TextMessage) translator.translate(adaptrisMessage);
    
    assertEquals(MESSAGE_CONTENT, translatedMessage.getText());
    assertEquals("123", translatedMessage.getApplicationMessageId());
    assertEquals(1, translatedMessage.getPriority());
    assertEquals(timestamp, translatedMessage.getSenderTimestamp());
  }
  
  @Test
  public void testTranslateSolaceTextMessage() throws Exception {
    AdaptrisMessage adaptrisMessage = translator.translate(mockTextMessage);
    
    assertEquals(MESSAGE_CONTENT, adaptrisMessage.getContent());
  }
  
  @Test
  public void testTranslateSolaceTextMessageWithDeliveryMode() throws Exception {
    AdaptrisMessage adaptrisMessage = translator.translate(mockTextMessage);
    
    assertEquals(MESSAGE_CONTENT, adaptrisMessage.getContent());
  }
  
  @Test
  public void testTranslateSolaceTextMessageWithMappings() throws Exception {
    SolaceJcsmpMetadataMapping map1 = new SolaceJcsmpMetadataMapping("app-message-id", "ApplicationMessageId");
    SolaceJcsmpMetadataMapping map2 = new SolaceJcsmpMetadataMapping("sender-timestamp", "SenderTimestamp", "Long");
    SolaceJcsmpMetadataMapping map3 = new SolaceJcsmpMetadataMapping("priority", "Priority", "Integer");
    SolaceJcsmpMetadataMapping map4 = new SolaceJcsmpMetadataMapping("discard-indication", "DiscardIndication", "Boolean");
    
    translator.getMappings().add(map1);
    translator.getMappings().add(map2);
    translator.getMappings().add(map3);
    translator.getMappings().add(map4);
        
    AdaptrisMessage adaptrisMessage = translator.translate(mockTextMessage);
        
    assertEquals(MESSAGE_CONTENT, adaptrisMessage.getContent());
    assertEquals("xyz", adaptrisMessage.getMetadataValue("app-message-id"));
    assertEquals("3", adaptrisMessage.getMetadataValue("priority"));
    assertEquals(Long.toString(timestamp), adaptrisMessage.getMetadataValue("sender-timestamp"));
    assertEquals("true", adaptrisMessage.getMetadataValue("discard-indication"));
  }

}
