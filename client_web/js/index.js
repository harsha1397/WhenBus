if ("geolocation" in navigator) {
  /* geolocation is available */

  function initMap() {
    
  	var center = {lat: 12.986636, lng: 80.238780};

  	navigator.geolocation.getCurrentPosition(function(position) {
  		center.lat = position.coords.latitude;
  		center.long = position.coords.longitude;
		});

    

    var map = new google.maps.Map(document.getElementById('map'), {
      zoom: 16,
      center: center
    });

    var marker = new google.maps.Marker({
      position: center,
      map: map,
      title: 'Center of My World!'
    });
  }

} else {
  /* geolocation IS NOT available */
  alert("GEO-LOCATION not available");
}