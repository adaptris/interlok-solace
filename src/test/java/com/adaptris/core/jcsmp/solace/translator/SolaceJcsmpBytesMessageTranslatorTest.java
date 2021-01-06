package com.adaptris.core.jcsmp.solace.translator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MockBaseTest;
import com.solacesystems.jcsmp.BytesMessage;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPFactory;

public class SolaceJcsmpBytesMessageTranslatorTest extends MockBaseTest {

  private static final String MESSAGE_CONTENT = "My message content.";

  private SolaceJcsmpBytesMessageTranslator translator;

  @Mock private JCSMPFactory mockJcsmpFactory;

  @Mock private BytesMessage mockBytesMessage;

  @Before
  public void setUp() throws Exception {
    translator = new SolaceJcsmpBytesMessageTranslator();
    translator.setJcsmpFactory(mockJcsmpFactory);

    when(mockJcsmpFactory.createMessage(BytesMessage.class))
    .thenReturn(mockBytesMessage);
    when(mockBytesMessage.getData())
    .thenReturn(MESSAGE_CONTENT.getBytes());
  }

  @Test
  public void testTranslateAdaptrisBytesMessage() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);

    BytesMessage translatedMessage = (BytesMessage) translator.translate(adaptrisMessage);

    assertEquals(MESSAGE_CONTENT, new String(translatedMessage.getData()));
  }

  @Test
  public void testTranslateAdaptrisTextMessageWithDeliveryMode() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);

    translator.setDeliveryMode("NON_PERSISTENT");
    BytesMessage translatedMessage = (BytesMessage) translator.translate(adaptrisMessage);

    assertEquals(MESSAGE_CONTENT, new String(translatedMessage.getData()));
    assertEquals(DeliveryMode.NON_PERSISTENT, translatedMessage.getDeliveryMode());
  }

  @Test
  public void testTranslateSolaceTextMessage() throws Exception {
    AdaptrisMessage adaptrisMessage = translator.translate(mockBytesMessage);

    assertEquals(MESSAGE_CONTENT, adaptrisMessage.getContent());
  }

  @Test
  public void testTranslateSolaceTextMessageWithDeliveryMode() throws Exception {
    AdaptrisMessage adaptrisMessage = translator.translate(mockBytesMessage);

    assertEquals(MESSAGE_CONTENT, adaptrisMessage.getContent());
  }

}
