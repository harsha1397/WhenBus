var express = require('express')
var router = express.Router()

var sanity_check = require('../common/sanity.js')
var util = require('./util.js')

router.get('/', function(req, res) {
  res.send('SUGGEST API');
});

// Send the Nearest Bus Stop Information
// Coordinates and Name
// Logic : Send the Geographically the Nearest Bus Stop
router.post('/nearestStop', function(req, res) {
  var query = req.body;
  if (
    sanity_check.isRequired(query.coord) &&
    sanity_check.isRequired(query.coord.lat) &&
    sanity_check.isRequired(query.coord.lng)
  ) {
    var db = req.db;
    var processing = util.geographicallyNearest(db, query.coord)
    processing.then(
      (result) => {
        res.send(result);
      }
    )
    .catch(
      (reason) => {
        console.error(reason);
        res.status(500);
        res.send();
      }
    )
  } else {
    res.status(400);
    res.send();
  }
});

// Send atmost 5 possible Bus Numbers that reach Destination
// * Infer source from Location of user [ or explicitly mentioned ]
// Logic : Intersection of bus Numbers at destination and source
router.post('/bus', function(req, res) {
  var query = req.body;
  if (
    sanity_check.isRequired(query.dest) &&
    sanity_check.isRequired(query.coord) &&
    sanity_check.isRequired(query.coord.lat) &&
    sanity_check.isRequired(query.coord.lng)
  ) {
    var db = req.db;
    if (query.src) {

      var collection = db.get("BusStop");
      collection.findOne({"stopName": query.dest},{},function(err, record) {
        if (err) {
          console.error(err);
          res.status(500);
          res.send();
          return;
        }
        var busesAtDest = record.busList;
        collection.findOne({"stopName": query.src},{}, function(err, element) {
          if (err) {
            console.error(err);
            res.status(500);
            res.send();
            return;
          }
          var busesAtSrc = element.busList;
          var suggestedBuses = busesAtDest.filter((n) => {
                            return busesAtSrc.indexOf(n) !== -1;
                          });
          var busCollection = db.get("Bus");
          busCollection.find(
            {busNo : { $in : suggestedBuses}},
            {},
            function(err, documents) {
              if (err) {
                console.error(err);
                res.status(500);
                res.send();
                return;
              }
              documents = documents.map((document) => {
                var time;
                for (var i=0; i<document.Timings.length; i++) {
                  if (document.Timings[i].busStop === query.src) {
                    time = document.Timings[i].time;
                    break;
                  }
                }
                return ({
                  "bus_no" : document["busNo"],
                  "src" : query.src,
                  "time" : time
                });
              });
              documents.sort((A,B) => {
                return A.time - B.time;
              });
              var date = new Date();
              var threshold = date.getHours()*60 + date.getMinutes();

              console.log(documents);

              documents = documents.filter((document) => {
                return (document.time >= threshold);
              });
              // Send Top Five Results
              res.send(documents.slice(0,5));
            });
        });
      });

    } else {
      var processing = util.sourceAndBusSuggest(db, query.coord, query.dest);
      processing.then(
        (result) => {
          res.send(result);
        }
      )
      .catch(
        (reason) => {
          console.error(reason);
          res.status(500);
          res.send();
        }
      )
    }

  } else {
    res.status(400);
    res.send();
  }
});

module.exports = router;
