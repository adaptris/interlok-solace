package com.adaptris.core.jcsmp.solace.translator;

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
import com.solacesystems.jcsmp.XMLContentMessage;

public class SolaceJcsmpXmlContentMessageTranslatorTest {

  private static final String MESSAGE_CONTENT = "My message content.";
  
  private SolaceJcsmpXmlContentMessageTranslator translator;
  
  @Mock private JCSMPFactory mockJcsmpFactory;

  @Mock private XMLContentMessage mockXmlContentMessage;
    
  @Before
  public void setUp() throws Exception {
    MockitoAnnotations.initMocks(this);
    
    translator = new SolaceJcsmpXmlContentMessageTranslator();
    translator.setJcsmpFactory(mockJcsmpFactory);
        
    when(mockJcsmpFactory.createMessage(XMLContentMessage.class))
        .thenReturn(mockXmlContentMessage);
    when(mockXmlContentMessage.getXMLContent())
        .thenReturn(MESSAGE_CONTENT);
  }
  
  @After
  public void tearDown() throws Exception {
    
  }
  
  @Test
  public void testTranslateAdaptrisBytesMessage() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);
    
    XMLContentMessage translatedMessage = (XMLContentMessage) translator.translate(adaptrisMessage);
    
    assertEquals(MESSAGE_CONTENT, translatedMessage.getXMLContent());
  }
  
  @Test
  public void testTranslateAdaptrisTextMessageWithDeliveryMode() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);
    
    translator.setDeliveryMode("NON_PERSISTENT");
    XMLContentMessage translatedMessage = (XMLContentMessage) translator.translate(adaptrisMessage);
    
    assertEquals(MESSAGE_CONTENT, translatedMessage.getXMLContent());
    assertEquals(DeliveryMode.NON_PERSISTENT, translatedMessage.getDeliveryMode());
  }
  
  @Test
  public void testTranslateSolaceTextMessage() throws Exception {
    AdaptrisMessage adaptrisMessage = translator.translate(mockXmlContentMessage);
    
    assertEquals(MESSAGE_CONTENT, adaptrisMessage.getContent());
  }
  
  @Test
  public void testTranslateSolaceTextMessageWithDeliveryMode() throws Exception {
    AdaptrisMessage adaptrisMessage = translator.translate(mockXmlContentMessage);
    
    assertEquals(MESSAGE_CONTENT, adaptrisMessage.getContent());
  }
}
