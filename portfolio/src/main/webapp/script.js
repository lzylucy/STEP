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

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['One who wants to wear the crown, bears the crown.',
      'In lumine Tuo videbimus lumen',
      'YOLO!', 
      'Wear your failure as a badge of honor'];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

/**
 * Adds a random fun fact to the page.
 */
function addRandomFunFact() {
  const funfacts =
      ['My favorite flower is Sunflower',
      'My favorite movie is \"The Devil Wears Prada\"',
      'My favorite place in NYC is MET!', 
      'The first broadway show I\'ve ever watched is \"Chicago\"',
      'I\'m a Kpop fan',
      'I don\'t have any pets. I\'ve only kept goldfish before'];

  // Pick a random fun fact.
  const funfact = funfacts[Math.floor(Math.random() * funfacts.length)];

  // Add it to the page.
  const funfactContainer = document.getElementById('funfact-container');
  funfactContainer.innerText = funfact;
}

/**
 * Checks user authentication. 
 * If logged in, displays comments; if not, displays login link
 */
function loadComments() {
  fetch('/login').then(response => response.text()).then(stats => {
    if (stats.trim() === "okay") {
      getComments();
    } else {
      const statsListElement = document.getElementById('msg-container');
      statsListElement.innerHTML = "<p>Login <a href=\"" + stats + 
        "\">here</a> to see comments</p>";
    }
  });
}

/**
 * Fetches comments from DataServlet and adds them to the page
 */
function getComments() {
  const limit = document.getElementById('limit').value;

  // Pop up an alert window if input is invalid
  if (limit < 0) {
    alert("Please enter a non-negative integer");
    return;
  }

  // Load messages if input is valid
  fetch(`/list-data?limit=${limit}`)
    .then(response => response.json())
    .then((stats) => {
      const statsListElement = document.getElementById('msg-container');
      statsListElement.innerHTML = '';

      if (stats) {
        stats.forEach((message) => {
          statsListElement.appendChild(createCommentElement(message));
        });
      }
  });
}

/** Creates an element that represents a comment. */
function createCommentElement(message) {
  const commentElement = document.createElement('li');
  commentElement.innerText = message.name + "--" + message.job 
                                          + "--" + message.email;

  const divElement = document.createElement('div')
  divElement.className = "comment"
  divElement.innerText = message.comment
  commentElement.appendChild(divElement);
  
  if (message.imageUrl) {
    commentElement.appendChild(createImageElement(message.imageUrl));
  }

  return commentElement;
}

/** Creates an element that represents a clickable image. */
function createImageElement(imageUrl) {
  const referElement = document.createElement('a');
  referElement.href = imageUrl;
  const imageElement = document.createElement('img');
  imageElement.src = imageUrl;
  referElement.appendChild(imageElement);
  return referElement;
}

/** Tells the server to delete all comments. */
function deleteAllComments() {
  fetch('/delete-data', {method: 'POST'}).then(() => {
    loadComments();
  });
}

/** Fetches blobstore url and then shows form  */
function fetchBlobstoreUrlAndShowForm() {
  fetch('/blobstore-image-upload')
    .then((response) => {
    return response.text();
    })
    .then((imageUploadUrl) => {
    const messageForm = document.getElementById('my-form');
    messageForm.action = imageUploadUrl;
    messageForm.classList.remove('hidden');
    });
}

/** Obtains user's geo location and adds the info to form  */
function getUserGeoInfo() {
  var geoSuccess = function(position) {
    document.getElementById('lat').value = position.coords.latitude;
    document.getElementById('long').value= position.coords.longitude;
  };
  var geoError = function(error) {
    // error.code can be:
    //   0: unknown error
    //   1: permission denied
    //   2: position unavailable (error response from location provider)
    //   3: timed out
    console.log('Error occurred. Error code: ' + error.code);
  };
  navigator.geolocation.getCurrentPosition(geoSuccess, geoError);
}

window.onload = function() {
  loadComments();
  fetchBlobstoreUrlAndShowForm();
  getUserGeoInfo();
}