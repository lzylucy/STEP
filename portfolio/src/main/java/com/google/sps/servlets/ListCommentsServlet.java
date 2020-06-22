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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.google.gson.Gson;
import com.google.sps.data.Message;
import java.util.ArrayList;

/** Servlet that returns comments. */
@WebServlet("/list-data")
public class ListCommentsServlet extends HttpServlet {

  private static final DatastoreService DATASTORE = 
    DatastoreServiceFactory.getDatastoreService();
  private static final Gson GSON = new Gson();
  
  @Override
  public void doGet(HttpServletRequest request, 
                    HttpServletResponse response) throws IOException {
    // Define a query rule that prioritizes latest messages
    Query query = new Query("Message").addSort("timestamp", 
                                               SortDirection.DESCENDING);
    PreparedQuery results = DATASTORE.prepare(query);
 
    // Retrieve all history comments from datastore
    ArrayList<Message> messages = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      long id = entity.getKey().getId();
      String name = (String) entity.getProperty("name");
      String job = (String) entity.getProperty("job");
      String comment = (String) entity.getProperty("comment");
      long timestamp = (long) entity.getProperty("timestamp");
 
      Message msg = new Message(id, name, job, comment, timestamp);
      messages.add(msg);
    }

    response.setContentType("application/json;");
    response.getWriter().println(convertToJson(messages));
  }

  /**
   * Converts an ArrayList instance into a JSON string using the Gson library.
   */
  private static final <T> String convertToJson(ArrayList<T> messages) {
    return GSON.toJson(messages);
  }
}