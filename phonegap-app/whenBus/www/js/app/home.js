$( function() {
    $("#dest").autocomplete({
      source: stopList
    });

    $("#busNo").autocomplete({
      source: busNoList
    })

    populateDirModal = function() {
    	var busNo = $("#busNo").val()

    	if (busInfo[busNo]) {  // Populate Direction 
    		$("#dirModTitle").html("Which direction are you travelling in?");
    		$("#dirErrMsg").removeClass("alert alert-danger");
    		$("#dirErrMsg").html("");

    		var A = busInfo[busNo].source;
    		var B = busInfo[busNo].destination;

    		$("#dirDisplay").html('\
    		<button onclick="info(\''+busNo+'\',\''+A+'\',\''+B+'\')" class="btn btn-primary btn-block"  data-dismiss="modal"> \
    		'+A +' to '+B+'	\
    		</button>\
    		<button onclick="info(\''+busNo+'\',\''+B+'\',\''+A+'\')" class="btn btn-primary btn-block" data-dismiss="modal"> \
    		'+B +' to '+A+'	\
    		</button>\
    			');

    	} else {			// ERROR
    		$("#dirModTitle").html("ERROR!");
    		$("#dirErrMsg").addClass("alert alert-danger");
				$("#dirErrMsg").html("<strong>Error: </strong> Please consider entering a valid bus number");
    		$("#dirDisplay").html("");
    	}
    }

    info = function(busNo, src, dest) {
    	console.log(busNo, src, dest);
        // Set ID = 2 // info
        localStorage.setItem("whenBusMapId", 2);
        localStorage.setItem("busNo", busNo);
        localStorage.setItem("start_point", src);
        localStorage.setItem("end_point", dest);

        $("#home").hide();
        $("#suggestDisplay").hide();
        $("#MapDisplay").load('map.html');
        $("#MapDisplay").show();
    }

    nearestStop = function() {
    	console.log("NS");
    	// Set ID = 1 nearest Bus
        localStorage.setItem("whenBusMapId", 1);

        $("#home").hide();
        $("#suggestDisplay").hide();
    	$("#MapDisplay").load('map.html');
    	$("#MapDisplay").show();
    };

    search = function() {
    	var dest = $("#dest").val();

        if (stopInfo[dest]) {
            $("#suggTitle").html("Search Results");
            $("#suggErrMsg").removeClass("alert alert-danger");
            $("#suggErrMsg").html("");

            // API
            output = [
              {
                "bus_no": "5C",             
                "start_point": "A",
                "end_point" : "B",
                "src"   : "GC",             
                "time"  : 330,              
              },
              {
                "bus_no": "5C",             
                "start_point": "A",
                "end_point" : "B",
                "src"   : "GB",             
                "time"  : 340,              
              }                                             
            ];

            displaycontent = ''

            for (i in output ) {
              var bus = output[i];
              displaycontent += 
              '<button type="button" class="btn btn-basic btn-block" \
              onclick="info(\''+bus.bus_no+'\',\''+bus.start_point+'\',\''+bus.end_point+'\')">\
              <pre> Bus : '+bus.bus_no+'    ETA:'+Math.round(bus.time/60)+':'+bus.time%60+ ' \
              </button>'  
            }

            if (output.length === 0) {
                displaycontent = '<div class="alert alert-warning"> Sorry! Couldn\'t fetch any usefull results </div>';
            }

            $("#suggDisplay").html(displaycontent);

        } else {
            $("#suggTitle").html("Something went wrong!");
            $("#suggErrMsg").addClass("alert alert-danger");
            $("#suggErrMsg").html("<strong>Error: </strong> Please enter a valid stop name");
            $("#suggDisplay").html("");
        }
        
        $("#home").hide();
        $("#MapDisplay").empty();
    	$("#MapDisplay").hide();
    	$("#suggestDisplay").show();
    }

    home = function() {  	
    	$("#suggestDisplay").hide();
        $("#MapDisplay").empty();
    	$("#MapDisplay").hide();
    	$("#home").show();
    }
});
