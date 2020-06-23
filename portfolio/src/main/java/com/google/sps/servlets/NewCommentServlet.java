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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.Entity;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that stores new comments. */
@WebServlet("/new-data")
public class NewCommentServlet extends HttpServlet {

  private static final DatastoreService DATASTORE = 
    DatastoreServiceFactory.getDatastoreService();
  private static final UserService USERSERVICE =
    UserServiceFactory.getUserService();
    
  @Override
  public void doPost(HttpServletRequest request, 
                     HttpServletResponse response) throws IOException {
    // Retrieve information from the form and add timestamp
    final String name = Utilities.getParameterWithDefault(
      request, "user-name", "Anonymous");
    final String job = Utilities.getParameterWithDefault(
      request, "jobs", "Other");
    final String email = USERSERVICE.getCurrentUser().getEmail();
    final String comment = Utilities.getParameterWithDefault(
      request, "visitor-comment", "");
    final long timestamp = System.currentTimeMillis();

    // Store the information if comment is non-empty
    if (!comment.isEmpty()) {
      Entity messageEntity = new Entity("Message");
      messageEntity.setProperty("name", name);
      messageEntity.setProperty("job", job);
      messageEntity.setProperty("email", email);
      messageEntity.setProperty("comment", comment);
      messageEntity.setProperty("timestamp", timestamp);
      DATASTORE.put(messageEntity);
    }

    // Redirect back to the HTML page.
    response.sendRedirect("/index.html");
  }
}