package com.adaptris.core.jms.solace.parameters;


public abstract class ParameterImp<T> implements Parameter {
  
  private T value;
  
  public T getValue() {
    return value;
  }
  
  public void setValue(final T value) { 
    this.value = value; 
  }
  
}
