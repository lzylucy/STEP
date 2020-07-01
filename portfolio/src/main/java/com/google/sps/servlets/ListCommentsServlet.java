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
    
  public Message(long id, String name, String latitude, String longitude,
                 String job, String email, String comment, String imageUrl, 
                 long timestamp) {
    this.id = id;
    this.latitude = latitude;
    this.longitude = longitude;
    this.name = name;
    this.job = job;
    this.email = email;
    this.comment = comment;
    this.imageUrl = imageUrl;
    this.timestamp = timestamp;
  }

  private final long id;
  private final String latitude;
  private final String longitude;
  private final String name;
  private final String job;
  private final String email;
  private final String comment;
  private final String imageUrl;
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
      Message msg = new Message(entity.getKey().getId(), 
                                (String) entity.getProperty("name"), 
                                (String) entity.getProperty("lat"),
                                (String) entity.getProperty("long"),
                                (String) entity.getProperty("job"), 
                                (String) entity.getProperty("email"), 
                                (String) entity.getProperty("comment"), 
                                (String) entity.getProperty("imageUrl"),
                                (long) entity.getProperty("timestamp"));
      messages.add(msg);
    }

    response.setContentType("application/json;");
    response.getWriter().println(Utilities.convertToJson(messages));
  }
}