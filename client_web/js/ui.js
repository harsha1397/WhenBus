var fire_query = function() {
  var busNo = $("#busNo").val();
  var busSource = $("#busSource").val();
  var busDest = $("#busDest").val();
  console.log(busNo + " , " + busSource + " , "+ busDest);
  // API call

  // Hide Interface
  // $("#UserInterface").hide();

  // Populate the New Interface and show()

}

var nearestBusStop = function() {
  
}

var working = false;
$('.login').on('submit', function(e) {
  e.preventDefault();
  if (working) return;
  working = true;
  var $this = $(this),
    $state = $this.find('button > .state');
  $this.addClass('loading');
  $state.html('Requesting Information');

  setTimeout(function() {
    $this.addClass('ok');
    $state.html('');
    
    fire_query();
    setTimeout(function() {
      $state.html('Submit');
      $this.removeClass('ok loading');
      working = false;
    }, 4000);
  }, 3000);
});