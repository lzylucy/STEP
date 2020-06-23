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

import com.google.sps.servlets.Utilities;
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
import java.util.ArrayList;

/** An item containing visitor information and comment. */
final class Message {
    
  public Message(long id, String name, String job, String email, 
                 String comment, long timestamp) {
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
@WebServlet("/list-data")
public class ListCommentsServlet extends HttpServlet {

  private static final DatastoreService DATASTORE = 
    DatastoreServiceFactory.getDatastoreService();
  
  @Override
  public void doGet(HttpServletRequest request, 
                    HttpServletResponse response) throws IOException {
    // Define a query rule that prioritizes latest messages
    Query query = new Query("Message").addSort("timestamp", 
                                               SortDirection.DESCENDING);
    PreparedQuery results = DATASTORE.prepare(query);

    // Get comment limit and convert to integer.
    // Set comment limit to 0 if input is invalid
    int commentLimit = 0;
    try {
      commentLimit = Integer.parseInt(Utilities.getParameterWithDefault(
        request, "limit", "0"));
      commentLimit = Math.max(commentLimit, 0);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int");
    }

    // Retrieve all history comments from datastore
    // If limit > number of comments, return all comments;
    // otherwise, return number of comments according to the limit
    ArrayList<Message> messages = new ArrayList<>();
    for (final Entity entity : results.asIterable(
           FetchOptions.Builder.withLimit(commentLimit))) {
      final long id = entity.getKey().getId();
      final String name = (String) entity.getProperty("name");
      final String job = (String) entity.getProperty("job");
      final String email = (String) entity.getProperty("email");
      final String comment = (String) entity.getProperty("comment");
      final long timestamp = (long) entity.getProperty("timestamp");
 
      Message msg = new Message(id, name, job, email, comment, timestamp);
      messages.add(msg);
    }

    response.setContentType("application/json;");
    response.getWriter().println(Utilities.convertToJson(messages));
  }
}