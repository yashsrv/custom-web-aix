package com.yashsrv.customweb.helpers;

import com.google.appinventor.components.common.OptionList;

import java.util.HashMap;
import java.util.Map;

public enum HttpMethod implements OptionList<String> {
  Get("GET"),
  Post("POST"),
  Put("PUT"),
  Patch("PATCH"),
  Delete("DELETE"),
  Head("HEAD"),
  Options("OPTIONS");

  private String httpMethod;

  HttpMethod(String method) {
    this.httpMethod = method;
  }

  public String toUnderlyingValue() {
    return httpMethod;
  }

  private static final Map<String, HttpMethod> lookup = new HashMap<>();

  static {
    for(HttpMethod method : HttpMethod.values()) {
      lookup.put(method.toUnderlyingValue(), method);
    }
  }

  public static HttpMethod fromUnderlyingValue(String method) {
    return lookup.get(method);
  }
}
