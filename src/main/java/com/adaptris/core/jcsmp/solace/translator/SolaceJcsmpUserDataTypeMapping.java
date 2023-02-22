package com.adaptris.core.jcsmp.solace.translator;

import com.thoughtworks.xstream.annotations.XStreamAlias;

import lombok.Getter;
import lombok.Setter;

@XStreamAlias("solace-jcsmp-user-data-type-mapping")
public class SolaceJcsmpUserDataTypeMapping {
  
  @Getter
  @Setter
  private String metadataKey;
  
  @Getter
  @Setter
  private SolaceJcsmpUserDataTypeEnum dataType;

  public SolaceJcsmpUserDataTypeMapping() {
  }
  
  public SolaceJcsmpUserDataTypeMapping(String metadataKey, SolaceJcsmpUserDataTypeEnum dataType) {
    setMetadataKey(metadataKey);
    setDataType(dataType);
  }
}
