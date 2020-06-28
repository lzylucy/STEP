/** Creates a map and adds it to the page. */
function initMap() {
  const map = new google.maps.Map(
      document.getElementById('map'),
      {center: {lat: 39.92, lng: 116.38}, zoom: 4});
  
  addMarkersAndInfoWindows(map);
}

function addMarkersAndInfoWindows(map) {
  const LIMIT = 100;
  fetch(`/list-data?limit=${LIMIT}`)
    .then(response => response.json())
    .then((stats) => {
      if (stats) {
        stats.forEach((message) => {
          var marker = new google.maps.Marker({
            position: {lat: Number(message.latitude), 
                       lng: Number(message.longitude)},
            map: map,
            title: "comment"
          });
          var infowindow = new google.maps.InfoWindow({
            content: createCommentElement(message)
          });

          marker.addListener('click', function() {
            infowindow.open(map, marker);
          });
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

  return commentElement;
}