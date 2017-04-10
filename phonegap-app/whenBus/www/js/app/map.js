if ("geolocation" in navigator) {
  /* geolocation is available */

  function addMarker(coord, title, map, infoWindow) {
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

    map = new google.maps.Map(document.getElementById('map'), {
      zoom: 16,
      center: center
    });

    console.log('Called');

    var id = localStorage.getItem("whenBusMapId");

    if (id === '1') {

      // var busStop =  {lat: 12.98624, lng: 80.248201};

      var output = {
        "distance": 102,                        
        "stopName": 'GC',                       
        "coord" : {                               
          "lat" : 12.98624,
          "lng" : 80.248201
        }
      }

      var busStop = output.coord;

      // addMarker(center, 'Current Location',map, infoWindow);
      // addMarker(busStop, 'Nearest Bus Stop', map, infoWindow);

      var directionsService = new google.maps.DirectionsService,
          directionsDisplay = new google.maps.DirectionsRenderer({
                                map: map
                              });

      var PointA = new google.maps.LatLng(center.lat, center.lng),
          PointB = new google.maps.LatLng(busStop.lat, busStop.lng);

      calculateAndDisplayRoute(directionsService, directionsDisplay, PointA, PointB);

      $("#map_data").html('\
        <ul class="list-group">\
          <li class="list-group-item list-group-item-info">Nearest Stop Information</li>\
          <li class="list-group-item list-group-item-basic"> \
          <pre>Name     : '+output.stopName+'</pre>\
          <pre>Distance : '+(Math.round( output.distance /100 ) / 10)+'Km</pre></li>\
        </ul>\
      ');

    } if (id == '2') {

      var output = {
        "id" : 1,                          
        "stop" : "GC",                     
        "busLoc" : {                               
          "lat" : 12.98624,
          "lng" : 80.248201
        },
        "time" : 330,                           
        "stopList" : ["A","B"]                
      }

      addMarker(center, "Your Location", map, infoWindow);
      addMarker(output.busLoc, "Bus's Location", map, infoWindow);


      $("#map_data").html('\
        <ul class="list-group">\
          <li class="list-group-item list-group-item-info">Next Bus Information</li>\
          <li class="list-group-item list-group-item-basic"> \
          <pre>Bus Number : '+localStorage.getItem("busNo")+'</pre>\
          <pre>Stop       : '+output.stop+'</pre>\
          <pre>ETA        : '+Math.round(output.time/60)+':'+(output.time%60)+'</pre></li>\
        </ul>\
        ');                                         

    }

    google.maps.event.addDomListener(window, 'load', initMap );
    setTimeout(initMap, 39000);
  }


} else {
  /* geolocation IS NOT available */
  alert("GEO-LOCATION not available");
}