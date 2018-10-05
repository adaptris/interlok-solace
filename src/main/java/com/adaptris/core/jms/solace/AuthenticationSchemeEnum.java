package com.adaptris.core.jms.solace;

import com.solacesystems.jms.SupportedProperty;

public enum AuthenticationSchemeEnum {
  AUTHENTICATION_SCHEME_BASIC(SupportedProperty.AUTHENTICATION_SCHEME_BASIC),
  AUTHENTICATION_SCHEME_CLIENT_CERTIFICATE(SupportedProperty.AUTHENTICATION_SCHEME_CLIENT_CERTIFICATE),
  AUTHENTICATION_SCHEME_GSS_KRB(SupportedProperty.AUTHENTICATION_SCHEME_GSS_KRB);

  private final String v;
  private AuthenticationSchemeEnum(final String v) {
    this.v = v;
  }
  public String getValue() {
    return v;
  }
}
