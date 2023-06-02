package com.adaptris.core.jcsmp.solace.translator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.AdaptrisMessageFactory;
import com.adaptris.core.metadata.RegexMetadataFilter;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.SDTMap;
import com.solacesystems.jcsmp.XMLMessage;

public class SolaceJcsmpUserDataTranslatorTest {

  private SolaceJcsmpUserDataTranslator translator;

  private AdaptrisMessage adpMessage;

  private RegexMetadataFilter filter;

  private XMLMessage solMessage;

  private List<SolaceJcsmpUserDataTypeMapping> mappings;

  @BeforeEach
  public void setUp() throws Exception {
    translator = new SolaceJcsmpUserDataTranslator();

    adpMessage = AdaptrisMessageFactory.getDefaultInstance().newMessage();
    adpMessage.addMessageHeader("integer", "1");
    adpMessage.addMessageHeader("boolean", "true");
    adpMessage.addMessageHeader("double", "1.11");
    adpMessage.addMessageHeader("float", "1.1111");

    filter = new RegexMetadataFilter();
    filter.addIncludePattern(".*");

    solMessage = JCSMPFactory.onlyInstance().createBytesXMLMessage();

    mappings = new ArrayList<>();
    mappings.add(new SolaceJcsmpUserDataTypeMapping("integer", SolaceJcsmpUserDataTypeEnum.Integer_Number));
    mappings.add(new SolaceJcsmpUserDataTypeMapping("boolean", SolaceJcsmpUserDataTypeEnum.True_False));
    mappings.add(new SolaceJcsmpUserDataTypeMapping("double", SolaceJcsmpUserDataTypeEnum.Double_Number));
    mappings.add(new SolaceJcsmpUserDataTypeMapping("float", SolaceJcsmpUserDataTypeEnum.Float_Number));
  }

  @AfterEach
  public void tearDown() throws Exception {

  }

  @Test
  public void testUserDataTypeMappings() throws Exception {
    translator.translate(adpMessage, solMessage, filter, mappings);

    assertEquals(Integer.class, solMessage.getProperties().get("integer").getClass());
    assertEquals(Boolean.class, solMessage.getProperties().get("boolean").getClass());
    assertEquals(Double.class, solMessage.getProperties().get("double").getClass());
    assertEquals(Float.class, solMessage.getProperties().get("float").getClass());
  }

  @Test
  public void testUserDataTypeMappingsOnlyOneMapping() throws Exception {
    mappings = new ArrayList<>();
    mappings.add(new SolaceJcsmpUserDataTypeMapping("integer", SolaceJcsmpUserDataTypeEnum.Integer_Number));

    translator.translate(adpMessage, solMessage, filter, mappings);

    assertEquals(Integer.class, solMessage.getProperties().get("integer").getClass());
    assertEquals(String.class, solMessage.getProperties().get("boolean").getClass());
    assertEquals(String.class, solMessage.getProperties().get("double").getClass());
    assertEquals(String.class, solMessage.getProperties().get("float").getClass());
  }

  @Test
  public void testUserDataToMetadata() throws Exception {
    SDTMap map = JCSMPFactory.onlyInstance().createMap();
    solMessage.setProperties(map);

    solMessage.getProperties().putString("string", "value");
    solMessage.getProperties().putInteger("integer", 1);
    solMessage.getProperties().putDouble("double", 1.11d);
    solMessage.getProperties().putFloat("float", 1.1111f);
    solMessage.getProperties().putLong("long", Long.parseLong("11111"));
    solMessage.getProperties().putShort("short", Short.parseShort("1"));
    solMessage.getProperties().putBoolean("boolean", true);

    translator.translate(solMessage, adpMessage);

    assertEquals("value", adpMessage.getMessageHeaders().get("string"));
    assertEquals("1", adpMessage.getMessageHeaders().get("integer"));
    assertEquals("11111", adpMessage.getMessageHeaders().get("long"));
    assertEquals("1.11", adpMessage.getMessageHeaders().get("double"));
    assertEquals("1.1111", adpMessage.getMessageHeaders().get("float"));
    assertEquals("true", adpMessage.getMessageHeaders().get("boolean"));
    assertEquals("1", adpMessage.getMessageHeaders().get("short"));
  }

}
