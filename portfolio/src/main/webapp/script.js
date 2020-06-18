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
 * Fetches comments from DataServlet and adds them to the page
 */
function loadComments() {
  //TODO: replace the hard-code value with user defined value
  fetch('/list-data?comment-limit=3').then(response => response.json()).then((stats) => {   
    console.log(stats)
    const statsListElement = document.getElementById('msg-container');
    statsListElement.innerHTML = '';
      
    if (stats) {
      stats.forEach((message) => {
        statsListElement.appendChild(createCommentElement(message));
      })
    }
  });
}

/** Creates an element that represents a comment, including its delete button. */
function createCommentElement(message) {
  const commentElement = document.createElement('li');
  commentElement.innerText = message.name + "--" + message.job;

  const divElement = document.createElement('div')
  divElement.className = "comment"
  divElement.innerText = message.comment
  commentElement.appendChild(divElement);

  const deleteButtonElement = document.createElement('button');
  deleteButtonElement.innerText = 'Delete';
  deleteButtonElement.addEventListener('click', () => {
    deleteComment(message);
    commentElement.remove();
  });
  commentElement.appendChild(deleteButtonElement);

  return commentElement;
}

/** Tells the server to delete a comment. */
function deleteComment(message) {
  const params = new URLSearchParams();
  params.append('id', message.id);
  // TODO: what if deleting the data is not successful, how to check
  fetch('/delete-data', {method: 'POST', body: params});
}