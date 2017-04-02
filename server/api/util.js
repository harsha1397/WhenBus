// Convert Degree to Radians
function toRadians(deg) {
  return (deg * Math.PI) / 180;
}

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
}

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
}


module.exports = {
  "toRadians" : toRadians,
  "geographicallyNearest" : geographicallyNearest
}
