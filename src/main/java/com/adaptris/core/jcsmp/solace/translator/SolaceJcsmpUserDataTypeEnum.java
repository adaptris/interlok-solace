package com.adaptris.core.jcsmp.solace.translator;

import com.solacesystems.jcsmp.SDTException;
import com.solacesystems.jcsmp.SDTMap;

public enum SolaceJcsmpUserDataTypeEnum {

  Integer_Number {
    @Override
    public void addToMap(String key, String value, SDTMap map) throws SDTException {
      map.putInteger(key, Integer.parseInt(value));
    }
  },
  
  Long_Number {
    @Override
    public void addToMap(String key, String value, SDTMap map) throws SDTException {
      map.putLong(key, Long.parseLong(value));
    }
  },
  
  Float_Number {
    @Override
    public void addToMap(String key, String value, SDTMap map) throws SDTException {
      map.putFloat(key, Float.parseFloat(value));
    }
  },
  
  Short_Number {
    @Override
    public void addToMap(String key, String value, SDTMap map) throws SDTException {
      map.putShort(key, Short.parseShort(value));
    }
  },
  
  Double_Number {
    @Override
    public void addToMap(String key, String value, SDTMap map) throws SDTException {
      map.putDouble(key, Double.parseDouble(value));
    }
  },
  
  True_False {
    @Override
    public void addToMap(String key, String value, SDTMap map) throws SDTException {
      map.putBoolean(key, Boolean.parseBoolean(value));
    }
  };
  
  public abstract void addToMap(String key, String value, SDTMap map) throws SDTException;
  
}
