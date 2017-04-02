// Convert Degree to Radians
function toRadians(deg) {
  return (deg * Math.PI) / 180;
};

// Distance in metres between two Coordinates
function distance(coordA, coordB) {
  var R = 6371e3; // metres
  var φ1 = toRadians(coordA.lat);
  var φ2 = toRadians(coordB.lat);
  var Δφ = toRadians(coordB.lat - coordA.lat);
  var Δλ = toRadians(coordB.lng - coordA.lng);

  var a = Math.sin(Δφ/2) * Math.sin(Δφ/2) +
          Math.cos(φ1) * Math.cos(φ2) *
          Math.sin(Δλ/2) * Math.sin(Δλ/2);

  var c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
  var d = R * c;
  return d;
};

function geographicallyNearest(db, coord) {
  var processing = new Promise((resolve, reject) => {
    var collection = db.get('BusStop');
    collection.find({}, {}, function(err, documents) {
      if (err)
        reject(err);
      else {
        D = [];
        MAX_RANGE = Infinity; // metres
        for (var i=0; i<documents.length; i++) {
          var d = distance(documents[i].coord, coord);
          if ( d <= MAX_RANGE )
            D.push({
              "distance" : d,
              "stopName" : documents[i]["stopName"],
              "coord" : documents[i]["coord"]
            });
        }
        D.sort((A,B) => {
          return A.distance - B.distance;
        });
        resolve(D[0]);
      }
    });
  });
  return processing;
};

function sourceAndBusSuggest(db, coord, dest) {

  var processing = new Promise((resolve, reject) => {
    var collection = db.get("BusStop");
    collection.findOne({"stopName" : dest},{}, function(err, record) {
      if (err) reject(err);
      var suggestedBuses = record.busList;
      var busCollection = db.get("Bus");
      busCollection.find(
        {busNo : {$in : suggestedBuses}},
        {},
        function(err, documents) {
          if (err) reject(err);
          var suggestionPromise = [];
          documents.forEach((document, i) => {
            var Timings = document["Timings"]
            Timings.forEach((element, j) => {
              var p = new Promise((accept, decline) => {
                collection.findOne(
                  {"stopName" : element.busStop},
                  {},
                  function(err, entity) {
                    if (err) decline(err);
                    var d = distance(entity.coord, coord);
                    accept(
                      {
                        "distance" : d,
                        "bus_no" : document["busNo"],
                        "src" : element.busStop,
                        "time" : element.time
                      }
                    );
                  }
                );
              });
              suggestionPromise.push(p);
            })
          });

          Promise.all(suggestionPromise).then((suggestion) => {

            suggestion.sort((A,B) => {
              var Δdistance = A.distance - B.distance;
              var walking_speed = 83.3333;  // m/min
              var Δwalk_diff = (Δdistance/walking_speed);
              return A.time - B.time + Δwalk_diff;
            });
            var date = new Date();
            var threshold = date.getHours()*60 + date.getMinutes();

            suggestion = suggestion.filter((document) => {
              return (document.time >= threshold);
            });
            // Send Top Five Results
            resolve(suggestion.slice(0,5));

          }).catch ( (reason) => {
            reject(reason);
          });

        }
      );
    });
  });
  return processing;
};

module.exports = {
  "toRadians" : toRadians,
  "distance" : distance,
  "geographicallyNearest" : geographicallyNearest,
  "sourceAndBusSuggest" : sourceAndBusSuggest
};
