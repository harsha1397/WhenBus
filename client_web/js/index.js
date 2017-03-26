if ("geolocation" in navigator) {
  /* geolocation is available */

  function addMarker(coord, title, map, infoWindow) {
    console.log(title)
    marker = new google.maps.Marker({
        position: coord,
        title : title,
        map: map
    });

  }

  function calculateAndDisplayRoute(directionsService, directionsDisplay, pointA, pointB) {
    directionsService.route({
      origin: pointA,
      destination: pointB,
      travelMode: google.maps.TravelMode.WALKING
    }, function(response, status) {
      if (status == google.maps.DirectionsStatus.OK) {
        directionsDisplay.setDirections(response);
      } else {
        // window.alert('Directions request failed due to ' + status);
        console.log('Directions Service Failed!');
      }
    });
  }


  function initMap() {

    var center = {lat: 12.98624, lng: 80.238201};

    navigator.geolocation.getCurrentPosition(function(position) {
      center.lat = position.coords.latitude;
      center.lng = position.coords.longitude;
    });

    var infoWindow = new google.maps.InfoWindow();

    var map = new google.maps.Map(document.getElementById('map'), {
      zoom: 16,
      center: center
    });

    // addMarker(center, 'Current Location',map, infoWindow);

    var busStop =  JSON.parse(localStorage.getItem('nearestStop'));

    // addMarker(busStop, 'Nearest Bus Stop', map, infoWindow);

    var directionsService = new google.maps.DirectionsService,
        directionsDisplay = new google.maps.DirectionsRenderer({
                              map: map
                            });

    var PointA = new google.maps.LatLng(center.lat, center.lng),
        PointB = new google.maps.LatLng(busStop.lat, busStop.lng);

    calculateAndDisplayRoute(directionsService, directionsDisplay, PointA, PointB);

    google.maps.event.addDomListener(window, 'load', initMap );
  }


} else {
  /* geolocation IS NOT available */
  alert("GEO-LOCATION not available");
}