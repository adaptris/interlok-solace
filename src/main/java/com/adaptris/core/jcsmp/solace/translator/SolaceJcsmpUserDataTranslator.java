package com.adaptris.core.jcsmp.solace.translator;

import java.util.List;

import com.adaptris.core.AdaptrisMessage;
import com.adaptris.core.MetadataCollection;
import com.adaptris.core.MetadataElement;
import com.adaptris.core.metadata.MetadataFilter;
import com.solacesystems.jcsmp.JCSMPFactory;
import com.solacesystems.jcsmp.SDTException;
import com.solacesystems.jcsmp.SDTMap;
import com.solacesystems.jcsmp.XMLMessage;

public class SolaceJcsmpUserDataTranslator {

  public void translate(XMLMessage source, AdaptrisMessage destination) throws SDTException {
    if(source.getProperties() != null) {
      for(String key : source.getProperties().keySet()) {
        Object object = source.getProperties().get(key);
        
        if(object instanceof String)
          destination.addMetadata(key, (String) object);
        else if(object instanceof Integer || object.getClass().equals(int.class))
          destination.addMetadata(key, Integer.toString((int) object));
        else if(object instanceof Boolean || object.getClass().equals(boolean.class))
          destination.addMetadata(key, Boolean.toString((boolean) object));
        else if(object instanceof Long || object.getClass().equals(long.class))
          destination.addMetadata(key, Long.toString((long) object));
        else if(object instanceof Double || object.getClass().equals(double.class))
          destination.addMetadata(key, Double.toString((double) object));
        else if(object instanceof Float || object.getClass().equals(float.class))
          destination.addMetadata(key, Float.toString((float) object));
        else if(object instanceof Short || object.getClass().equals(short.class))
          destination.addMetadata(key, Short.toString((short) object));
      }
    }
  }
  
  public void translate(AdaptrisMessage source, XMLMessage destination, MetadataFilter metadataFilter, List<SolaceJcsmpUserDataTypeMapping> dataTypeMappings) throws SDTException {
    MetadataCollection metadataCollection = metadataFilter.filter(source);
    
    SDTMap map = JCSMPFactory.onlyInstance().createMap();
    for(MetadataElement element : metadataCollection) {
      map.putString(element.getKey(), source.resolve(element.getValue()));
      
      if(dataTypeMappings != null) {
        for(SolaceJcsmpUserDataTypeMapping mapping : dataTypeMappings) {
          if(mapping.getMetadataKey().equals(element.getKey())) {
            mapping.getDataType().addToMap(mapping.getMetadataKey(), source.resolve(element.getValue()), map);
          }
        }
      }
    }
    destination.setProperties(map);
  }
}
