package com.adaptris.core.jcsmp.solace.translator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.DefaultMessageFactory;
import com.adaptris.core.MockBaseTest;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.solacesystems.jcsmp.DeliveryMode;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.TextMessage;

public class SolaceJcsmpTextMessageTranslatorTest extends MockBaseTest {

  private static final String MESSAGE_CONTENT = "My message content.";

  private SolaceJcsmpTextMessageTranslator translator;

  @Mock
  private JCSMPFactory mockJcsmpFactory;

  @Mock
  private TextMessage mockTextMessage;

  private Long timestamp;

  @BeforeEach
  public void setUp() throws Exception {
    translator = new SolaceJcsmpTextMessageTranslator();
    translator.setJcsmpFactory(mockJcsmpFactory);

    timestamp = Long.valueOf(System.currentTimeMillis());

    when(mockJcsmpFactory.createMessage(TextMessage.class)).thenReturn(mockTextMessage);
    when(mockTextMessage.getText()).thenReturn(MESSAGE_CONTENT);
    when(mockTextMessage.getPriority()).thenReturn(3);
    when(mockTextMessage.getSenderTimestamp()).thenReturn(timestamp);
    when(mockTextMessage.getApplicationMessageId()).thenReturn("xyz");
    when(mockTextMessage.getDiscardIndication()).thenReturn(true);
  }

  @Test
  public void testTranslateAdaptrisTextMessage() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);

    translator.setApplyPerMessagePropertiesOnProduce(true);
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
  public void testTranslateAdaptrisTextMessageWithPerMessageProperties() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);

    SolaceJcsmpPerMessageProperties pmp = new SolaceJcsmpPerMessageProperties();
    pmp.setAckImmediately("true");
    pmp.setAsReplyMessage("false");
    pmp.setCorrelationId("correlation-1");
    pmp.setCorrelationKey("correlation-key");
    pmp.setClassOfService("2");
    pmp.setDeliveryMode("PERSISTENT");
    pmp.setElidingEligible("false");
    pmp.setExpiration(Long.toString(System.currentTimeMillis()));
    pmp.setHttpContentEncoding("http-encoding");
    pmp.setHttpContentType("http-type");
    pmp.setReplyToQueue("reply-queue");
    pmp.setReplyToSuffix("reply-suffix");
    pmp.setSenderId("sender-id");
    pmp.setSequenceNumber("56");
    pmp.setTimeToLive("60000");

    pmp.setApplicationMessageId("%message{app-message-id}");
    pmp.setSenderTimestamp("%message{sender-timestamp}");
    pmp.setPriority("%message{priority}");
    pmp.setDmqEligible("%message{dmq-eligible}");

    adaptrisMessage.addMessageHeader("app-message-id", "123");
    adaptrisMessage.addMessageHeader("sender-timestamp", Long.toString(timestamp));
    adaptrisMessage.addMessageHeader("priority", "1");
    adaptrisMessage.addMessageHeader("dmq-eligible", "true");

    translator.setPerMessageProperties(pmp);
    translator.setApplyPerMessagePropertiesOnProduce(true);
    TextMessage translatedMessage = (TextMessage) translator.translate(adaptrisMessage);

    assertEquals(MESSAGE_CONTENT, translatedMessage.getText());
    assertEquals("123", translatedMessage.getApplicationMessageId());
    assertEquals(1, translatedMessage.getPriority());
    assertEquals(timestamp, translatedMessage.getSenderTimestamp());
    assertEquals("correlation-1", translatedMessage.getCorrelationId());
    assertEquals("correlation-key", translatedMessage.getCorrelationKey());
    assertEquals("reply-queue", translatedMessage.getReplyTo().getName());
  }

  @Test
  public void testTranslateAdaptrisTextMessageWithPerMessagePropertiesAndSDT() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);
    adaptrisMessage.addMetadata("key1", "value1");
    adaptrisMessage.addMetadata("key2", "value2");
    adaptrisMessage.addMetadata("key3", "value3");

    RegexMetadataFilter filter = new RegexMetadataFilter();
    filter.addIncludePattern("key.*");

    TextMessage translatedMessage = (TextMessage) translator.translate(adaptrisMessage);
    AdaptrisMessage adaptrisMessage2 = translator.translate(translatedMessage);

    assertEquals("value1", adaptrisMessage2.getMetadataValue("key1"));
    assertEquals("value2", adaptrisMessage2.getMetadataValue("key2"));
    assertEquals("value3", adaptrisMessage2.getMetadataValue("key3"));
  }

  @Test
  public void testTranslateSolaceTextMessage() throws Exception {
    AdaptrisMessage adaptrisMessage = translator.translate(mockTextMessage);

    assertEquals(MESSAGE_CONTENT, adaptrisMessage.getContent());
  }

  @Test
  public void testTranslateSolaceTextMessageWithDeliveryMode() throws Exception {
    translator.setApplyPerMessagePropertiesOnConsume(true);
    AdaptrisMessage adaptrisMessage = translator.translate(mockTextMessage);

    assertEquals(MESSAGE_CONTENT, adaptrisMessage.getContent());
  }

  @Test
  public void testTranslateSolaceTextMessageWithApplyProperties() throws Exception {
    AdaptrisMessage adaptrisMessage = DefaultMessageFactory.getDefaultInstance().newMessage(MESSAGE_CONTENT);

    SolaceJcsmpPerMessageProperties pmp = new SolaceJcsmpPerMessageProperties();
    pmp.setAckImmediately("true");
    pmp.setAsReplyMessage("false");
    pmp.setCorrelationId("correlation-1");
    pmp.setCorrelationKey("correlation-key");
    pmp.setClassOfService("2");
    pmp.setDeliveryMode("PERSISTENT");
    pmp.setElidingEligible("false");
    pmp.setExpiration(Long.toString(System.currentTimeMillis()));
    pmp.setHttpContentEncoding("http-encoding");
    pmp.setHttpContentType("http-type");
    pmp.setReplyToQueue("reply-queue");
    pmp.setReplyToSuffix("reply-suffix");
    pmp.setSenderId("sender-id");
    pmp.setSequenceNumber("56");
    pmp.setTimeToLive("60000");

    pmp.setApplicationMessageId("%message{app-message-id}");
    pmp.setSenderTimestamp("%message{sender-timestamp}");
    pmp.setPriority("%message{priority}");
    pmp.setDmqEligible("%message{dmq-eligible}");

    adaptrisMessage.addMessageHeader("app-message-id", "123");
    adaptrisMessage.addMessageHeader("sender-timestamp", Long.toString(timestamp));
    adaptrisMessage.addMessageHeader("priority", "1");
    adaptrisMessage.addMessageHeader("dmq-eligible", "true");

    translator.setPerMessageProperties(pmp);
    translator.setApplyPerMessagePropertiesOnProduce(true);
    translator.setApplyPerMessagePropertiesOnConsume(true);
    TextMessage translatedMessage = (TextMessage) translator.translate(adaptrisMessage);

    AdaptrisMessage secondTranslatedMessage = translator.translate(translatedMessage);

    assertEquals(MESSAGE_CONTENT, adaptrisMessage.getContent());
    assertEquals(true, Boolean.parseBoolean(secondTranslatedMessage.getMetadataValue("%message{dmq-eligible}")));
    assertEquals("56", secondTranslatedMessage.getMetadataValue("56"));
    assertEquals("reply-queue", secondTranslatedMessage.getMetadataValue("replyTo"));
  }

}
