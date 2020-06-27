/** Creates a map and adds it to the page. */
function initMap() {
  const map = new google.maps.Map(
      document.getElementById('map'),
      {center: {lat: 39.92, lng: 116.38}, zoom: 4});
}