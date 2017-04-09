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
    }

    nearestStop = function() {
    	console.log("P");
    	$("#home").hide();
    	$("#suggestDisplay").hide();
    	$("#MapDisplay").load('map.html');
    	$("#MapDisplay").show();
    }

    search = function() {
    	var dest = $("#dest").val();
    	$("#home").hide();
    	$("#MapDisplay").hide();
    	$("#suggestDisplay").show();
    }

    home = function() {  	
    	$("#suggestDisplay").hide();
    	$("#MapDisplay").hide();
    	$("#home").show();
    }
});
