$(document).ready(function() {   
            var sideslider = $('[data-toggle=collapse-side]');
            var sel = sideslider.attr('data-target');
            var sel2 = sideslider.attr('data-target-2');
            sideslider.click(function(event){
                $(sel).toggleClass('in');
                $(sel2).toggleClass('out');
            });
        });

function hide_nav() {
	var sideslider = $('[data-toggle=collapse-side]');
  var sel = sideslider.attr('data-target');
  var sel2 = sideslider.attr('data-target-2');
  
  $(sel).addClass('in');
  $(sel2).addClass('out');
}

function home () {
	$("#BusStopSuggestion").hide()
  $("#Suggestion").hide()
  $("#MapInterface").children().remove()
  $("#MapInterface").hide()     
  $("#UserInterface").show()
  hide_nav()
}