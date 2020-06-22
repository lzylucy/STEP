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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import java.util.ArrayList;

/** An item containing visitor information and comment. */
final class Message {
    
  public Message(long id, String name, String job, String email, String comment, long timestamp) {
    this.id = id;
    this.name = name;
    this.job = job;
    this.email = email;
    this.comment = comment;
    this.timestamp = timestamp;
  }

  private final long id;
  private final String name;
  private final String job;
  private final String email;
  private final String comment;
  private final long timestamp;
}

/** Servlet that returns comments. */
@WebServlet("/data")
public class DataServlet extends HttpServlet {

  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
  private static final UserService userService = UserServiceFactory.getUserService();
  private static final Gson GSON = new Gson();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Define a query rule that prioritizes latest messages
    final Query query = new Query("Message").addSort("timestamp", SortDirection.DESCENDING);
    final PreparedQuery results = datastore.prepare(query);

    // Get comment limit and check validity.
    int commentLimit = 0;
    try {
      commentLimit = Integer.parseInt(request.getParameter("limit"));
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int");
    }
    if (commentLimit < 0) {
      response.setContentType("text/html");
      response.getWriter().println("Please enter a non-negative integer");
      return;
    }

    // Retrieve all history comments from datastore
    // If limit > number of comments, return all comments;
    // otherwise, return number of comments according to the limit
    ArrayList<Message> messages = new ArrayList<>();
    for (final Entity entity : results.asIterable(FetchOptions.Builder.withLimit(commentLimit))) {
      long id = entity.getKey().getId();
      String name = (String) entity.getProperty("name");
      String job = (String) entity.getProperty("job");
      String email = userService.getCurrentUser().getEmail();
      String comment = (String) entity.getProperty("comment");
      long timestamp = (long) entity.getProperty("timestamp");

 
      Message msg = new Message(id, name, job, email, comment, timestamp);
      messages.add(msg);
    }

    response.setContentType("application/json;");
    response.getWriter().println(convertToJsonUsingGson(messages));
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Retrieve information from the form and add timestamp
    final String name = getParameterWithDefault(request, "user-name", "Anonymous");
    final String job = getParameterWithDefault(request, "jobs", "Other");
    final String comment = getParameterWithDefault(request, "visitor-comment", "");
    final long timestamp = System.currentTimeMillis();

    // Store the information if comment is non-empty
    if (!comment.isEmpty()) {
      Entity messageEntity = new Entity("Message");
      messageEntity.setProperty("name", name);
      messageEntity.setProperty("job", job);
      messageEntity.setProperty("comment", comment);
      messageEntity.setProperty("timestamp", timestamp);
      datastore.put(messageEntity);
    }

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }

  /**
   * @return the request parameter, or the default value if the parameter
   *         was not specified by the client
   */
  private String getParameterWithDefault(HttpServletRequest request, String name, String defaultValue) {
    String value = request.getParameter(name);
    if (value == null || value.isEmpty()) {
      return defaultValue;
    }
    return value;
  }

  /**
   * Converts an ArrayList instance into a JSON string using the Gson library.
   */
  private static final <T> String convertToJsonUsingGson(ArrayList<T> messages) {
    return GSON.toJson(messages);
  }
}