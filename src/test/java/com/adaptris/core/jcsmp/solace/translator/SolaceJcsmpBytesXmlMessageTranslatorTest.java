package com.adaptris.core.jcsmp.solace.translator;

import static org.junit.Assert.assertEquals;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.solacesystems.jcsmp.BytesXMLMessage;
import com.solacesystems.jcsmp.DeliveryMode;

public class SolaceJcsmpBytesXmlMessageTranslatorTest {

  private static final String MESSAGE_CONTENT = "My message content.";
  
  private SolaceJcsmpBytesXmlMessageTranslator translator;
      
  @Before
  public void setUp() throws Exception {    
    translator = new SolaceJcsmpBytesXmlMessageTranslator();
  }
  
  @After
  public void tearDown() throws Exception {
    
  }
  
  @Test
  public void testTranslateAdaptrisBytesMessage() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);
    
    BytesXMLMessage translatedMessage = (BytesXMLMessage) translator.translate(adaptrisMessage);
    
    byte[] contents = new byte[translatedMessage.getContentLength()];
    translatedMessage.readContentBytes(0, contents, 0, translatedMessage.getContentLength());
    
    assertEquals(MESSAGE_CONTENT, new String(contents));
  }
  
  @Test
  public void testTranslateAdaptrisTextMessageWithDeliveryMode() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);
    
    translator.setDeliveryMode("NON_PERSISTENT");
    BytesXMLMessage translatedMessage = (BytesXMLMessage) translator.translate(adaptrisMessage);
    
    byte[] contents = new byte[translatedMessage.getContentLength()];
    translatedMessage.readContentBytes(0, contents, 0, translatedMessage.getContentLength());
    
    assertEquals(MESSAGE_CONTENT, new String(contents));
    assertEquals(DeliveryMode.NON_PERSISTENT, translatedMessage.getDeliveryMode());
  }
  
  @Test
  public void testTranslateSolaceTextMessage() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);
    BytesXMLMessage translatedMessage = (BytesXMLMessage) translator.translate(adaptrisMessage);
    
    AdaptrisMessage translatedAdaptrisMessage = translator.translate(translatedMessage);
    
    assertEquals(MESSAGE_CONTENT, translatedAdaptrisMessage.getContent());
  }
  
  @Test
  public void testTranslateSolaceTextMessageWithDeliveryMode() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);
    BytesXMLMessage translatedMessage = (BytesXMLMessage) translator.translate(adaptrisMessage);
    
    AdaptrisMessage translatedAdaptrisMessage = translator.translate(translatedMessage);
    
    assertEquals(MESSAGE_CONTENT, translatedAdaptrisMessage.getContent());
  }
}
