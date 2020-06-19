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
    
  public Message(long id, String name, String job, String comment, long timestamp) {
    this.id = id;
    this.name = name;
    this.job = job;
    this.comment = comment;
    this.timestamp = timestamp;
  }

  private final long id;
  private final String name;
  private final String job;
  private final String comment;
  private final long timestamp;
}

/** Servlet that returns comments. */
@WebServlet("/list-data")
public class ListCommentsServlet extends HttpServlet {

  private static final DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Define a query rule that prioritizes latest messages
    Query query = new Query("Message").addSort("timestamp", SortDirection.DESCENDING);
    PreparedQuery results = datastore.prepare(query);

    // Get comment limit and check validity.
    int commentLimit = getCommentLimit(request);
    if (commentLimit == -1) {
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
      String comment = (String) entity.getProperty("comment");
      long timestamp = (long) entity.getProperty("timestamp");
 
      Message msg = new Message(id, name, job, comment, timestamp);
      messages.add(msg);
    }

    response.setContentType("application/json;");
    response.getWriter().println(convertToJsonUsingGson(messages));
  }

  /** Returns comment limit entered by the user, or -1 if the number entered was invalid. */
  private int getCommentLimit(HttpServletRequest request) {
    try {
      commentLimit = Integer.parseInt(request.getParameter("limit"));
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + commentLimitString);
      return -1;
    }

    // Check if the input is negative.
    if (commentLimit < 0) {
      System.err.println("Comment limit must not be negative");
      return -1;
    }

    return commentLimit;
  }

  /**
   * Converts an ArrayList instance into a JSON string using the Gson library.
   */
  private static final <T> String convertToJsonUsingGson(ArrayList<T> messages) {
    Gson GSON = new Gson();
    return GSON.toJson(messages);
  }
}