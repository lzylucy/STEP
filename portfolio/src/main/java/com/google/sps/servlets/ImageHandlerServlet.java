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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.blobstore.BlobInfo;
import com.google.appengine.api.blobstore.BlobInfoFactory;
import com.google.appengine.api.blobstore.BlobKey;
import com.google.appengine.api.blobstore.BlobstoreService;
import com.google.appengine.api.blobstore.BlobstoreServiceFactory;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.ServingUrlOptions;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/new-image")
public class ImageHandlerServlet extends HttpServlet {

  private static final BlobstoreService BLOBSTORESERVICE = 
    BlobstoreServiceFactory.getBlobstoreService();
  private static final ImagesService IMAGESSERVICE = 
    ImagesServiceFactory.getImagesService();
  private static final UserService USERSERVICE = 
    UserServiceFactory.getUserService();
  private static final DatastoreService DATASTORE = 
    DatastoreServiceFactory.getDatastoreService();

  @Override
  public void doGet(HttpServletRequest request, 
                     HttpServletResponse response) throws IOException {
    Query query = new Query("Image").addSort("timestamp", 
                                               SortDirection.DESCENDING);
    PreparedQuery results = DATASTORE.prepare(query);
    
    // Only return imageUrls at this point
    ArrayList<String> imageUrls = new ArrayList<>();
    for (final Entity entity : results.asIterable()) {
      final String imageUrl = (String) entity.getProperty("imageUrl");
      imageUrls.add(imageUrl);
    }

    response.setContentType("application/json;");
    response.getWriter().println(Utilities.convertToJson(imageUrls));
  }

  @Override
  public void doPost(HttpServletRequest request, 
                     HttpServletResponse response) throws IOException {                
    final String imageUrl = getUploadedFileUrl(request, "image");
    final String email = USERSERVICE.getCurrentUser().getEmail();
    final long timestamp = System.currentTimeMillis();

    if (imageUrl != null) {
      Entity imageEntity = new Entity("Image");
      imageEntity.setProperty("imageUrl", imageUrl);
      imageEntity.setProperty("email", email);
      imageEntity.setProperty("timestamp", timestamp);
      DATASTORE.put(imageEntity);
    }

    response.sendRedirect("/photo.html");
  }

  /** Returns a URL that points to the uploaded file, 
   *  or null if the user didn't upload a file. 
   */
  private String getUploadedFileUrl(HttpServletRequest request, String name) {
    Map<String, List<BlobKey>> blobs = BLOBSTORESERVICE.getUploads(request);
    List<BlobKey> blobKeys = blobs.get(name);

    // User submitted form without selecting a file, so we can't get a URL. 
    // (dev server)
    if (blobKeys == null || blobKeys.isEmpty()) {
      return null;
    }

    // Get key of the image uploaded (single upload)
    BlobKey blobKey = blobKeys.get(0);

    // User submitted form without selecting a file, so we can't get a URL. 
    // (live server)
    BlobInfo blobInfo = new BlobInfoFactory().loadBlobInfo(blobKey);
    if (blobInfo.getSize() == 0) {
      BLOBSTORESERVICE.delete(blobKey);
      return null;
    }

    ServingUrlOptions options = ServingUrlOptions.Builder.withBlobKey(blobKey);

    // To support running in Google Cloud Shell with AppEngine's devserver, 
    // we must use the relative path to the image, 
    // rather than the path returned by imagesService which contains a host.
    try {
      URL url = new URL(IMAGESSERVICE.getServingUrl(options));
      return url.getPath();
    } catch (MalformedURLException e) {
      return IMAGESSERVICE.getServingUrl(options);
    }
  }
}