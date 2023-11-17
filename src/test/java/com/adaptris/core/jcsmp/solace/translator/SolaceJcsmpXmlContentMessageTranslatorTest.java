package com.adaptris.core.jcsmp.solace.translator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MockBaseTest;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.XMLContentMessage;

public class SolaceJcsmpXmlContentMessageTranslatorTest extends MockBaseTest {

  private static final String MESSAGE_CONTENT = "My message content.";

  private SolaceJcsmpXmlContentMessageTranslator translator;

  @Mock
  private JCSMPFactory mockJcsmpFactory;

  @Mock
  private XMLContentMessage mockXmlContentMessage;

  @BeforeEach
  public void setUp() throws Exception {
    translator = new SolaceJcsmpXmlContentMessageTranslator();
    translator.setJcsmpFactory(mockJcsmpFactory);

    when(mockJcsmpFactory.createMessage(XMLContentMessage.class)).thenReturn(mockXmlContentMessage);
    when(mockXmlContentMessage.getXMLContent()).thenReturn(MESSAGE_CONTENT);
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
