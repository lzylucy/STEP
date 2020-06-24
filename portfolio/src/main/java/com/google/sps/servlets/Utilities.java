package com.google.sps.servlets;

import javax.servlet.http.HttpServletRequest;
import com.google.gson.Gson;
import java.util.ArrayList;

public final class Utilities {

  private static final Gson GSON = new Gson();

  private Utilities() {
    throw new AssertionError();
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  public static final String getParameterWithDefault(HttpServletRequest request, 
                                            String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    return value;
  }

  /**
   * Converts an ArrayList instance into a JSON string using the Gson library.
   */
  public static final <T> String convertToJson(ArrayList<T> messages) {
    return GSON.toJson(messages);
  }
}