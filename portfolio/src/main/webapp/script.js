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
function getCommentsUsingArrowFunctions() {
    fetch('/data').then(response => response.json()).then((stats) => {   
      console.log(stats)
      const statsListElement = document.getElementById('msg-container');
      statsListElement.innerHTML = '';
      
      if (stats) {
        for (let i=0; i<stats.length; i++) {
          statsListElement.appendChild(
              createListElement(stats[i].name + " -- " + stats[i].job, 
                                stats[i].comment));
        }
      }
    });
}

/** 
 * Creates a <li> element containing commenter identity 
 * and a child <div> element containing the comment.
 */
function createListElement(identity, comment) {
  const liElement = document.createElement('li');
  liElement.innerText = identity;
  const divElement = document.createElement('div')
  divElement.className = "comment"
  divElement.innerText = comment
  liElement.appendChild(divElement);
  return liElement;
}