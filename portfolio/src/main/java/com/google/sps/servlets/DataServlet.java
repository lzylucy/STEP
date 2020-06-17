// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.sps.servlets;

import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.ArrayList;

/** Servlet that returns comments. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {
  private static final Gson GSON = new Gson();
  private ArrayList<ArrayList<String>> messages = new ArrayList<>();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    response.setContentType("application/json;");
    response.getWriter().println(convertToJsonUsingGson(messages));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Retrieve information from the form
    String name = getParameter(request, "user-name", "Anonymous");
    String job = getParameter(request, "jobs", "Other");
    String comment = getParameter(request, "visitor-comment", "");

    // Store the information if comment is non-empty
    if (comment.length() > 0) {
       ArrayList<String> info = new ArrayList<>();
       info.add(name);
       info.add(job);
       info.add(comment);
       messages.add(info);
    }

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  /**
   * Converts an ArrayList instance into a JSON string using the Gson library.
   */
  private String convertToJsonUsingGson(ArrayList<ArrayList<String>> messages) {
    return GSON.toJson(messages);
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameter(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null) {
      return defaultValue;
    }
    return value;
  }
}