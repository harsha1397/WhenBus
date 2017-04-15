var express = require('express');
var router = express.Router();

var sanity_check = require('../common/sanity.js');
var stopLoc = require('../common/stopLoc.js');

var ObjectId = require('mongodb').ObjectId;
var util = require('./util.js');

router.get('/', function(req, res) {
  res.send('FEEDBACK API');
});

router.post('/access', function(req, res) {
  var query = req.body;
  console.log(query);
  if (
    sanity_check.isRequired(query.dest) &&
    sanity_check.isRequired(query.src) &&
    sanity_check.isRequired(query.busNo) &&
    sanity_check.isRequired(query.id) &&
    sanity_check.isRequired(query.start_point) &&
    sanity_check.isRequired(query.end_point)
  ) {
    var db = req.db;
    var collection = db.get("FPU");
    collection.insert(query, function(err, record) {
      if (err) {
        console.error(err);
        res.status(500);
        res.send();
      } else {
        console.log({
          "key" : record._id
        });
        res.send({
          "key" : record._id
        });
      }
    });
  } else {
    console.log("partial info");
    res.status(400);
    res.send();
  }
});

router.post('/send', function(req, res) {

  var query = req.body;
  console.log(query);
  if (
    sanity_check.isRequired(query.key) &&
    sanity_check.isRequired(query.coord) &&
    sanity_check.isRequired(query.coord.lat) &&
    sanity_check.isRequired(query.coord.lng) &&
    sanity_check.isRequired(query.timestamp)
  ) {
    var db = req.db;
    var FPUcollection = db.get("FPU");
    var fpu_id = new ObjectId(query.key);
    FPUcollection.findOne({_id : fpu_id}, {}, function(err, FPU) {
      if (err) {
        console.error(err);
        res.status(500);
        res.send();
      } else {
        if (FPU === {}) {
          res.send({
            "status" : "DROP"
          });
        } else {

          var BusCollection = db.get('Bus');
          BusCollection.findOne(
            {
              "id" : FPU.id,
              "busNo" : FPU.busNo,
              "source" : FPU.start_point,
              "destination": FPU.end_point
            },
            {},
            function(err, bus) {
              if (err) {
                console.error(err);
                res.status(500);
                res.send();
              } else {
                var timings = bus.Timings;
                var distances = []
                for (var i=0; i < timings.length; i++) {
                  var stopCoord = stopLoc[timings[i].busStop];
                  var d = util.distance(stopCoord, query.coord);
                  distances.push({
                    "index" : i,
                    "distance" : d,
                    "stopName" : timings[i].busStop
                  });
                }

                distances.sort((A,B) => {
                  return (A.distance - B.distance);
                });


                var stop1 = distances[0];
                var stop2 = distances[1];
                if (stop1.index < stop2.index) {
                  query.stop = stop2.stopName;
                } else {
                  query.stop = stop1.stopName;
                }

                var delta = 0;
                for(var i=0; i<timings.length; i++) {
                  if (timings[i].busStop === query.stop) {
                    var currTime = timings[i].time;
                    if (Math.abs(query.timestamp - currTime) <= 30) // at Max 30 min delay
                      currTime = query.timestamp;
                    delta = timings[i].time - (currTime);
                  }
                  timings[i].time -= delta;
                }

                BusCollection.update(
                  {
                    "id" : FPU.id,
                    "busNo" : FPU.busNo,
                    "source" : FPU.start_point,
                    "destination": FPU.end_point
                  },
                  {
                     $set:
                     {
                       "Timings" : timings,
                       "currLoc" : query.coord
                     }
                  },
                  function(err, documents) {
                    if (err) {
                      console.error(err);
                      res.status(500);
                      res.send();
                    } else {
                      if (FPU.dest == query.stop) {
                        FPUcollection.remove({_id : fpu_id},
                          function(err, del){
                            if(err) {
                              console.error(err);
                              res.status(500);
                              res.send();
                            } else {
                              res.send({
                                "status" : "DROP"
                              });
                            }
                          });
                      } else {
                        res.send({
                          "status" : "OK"
                        });
                      }
                    }
                  }
                );
            }
          });
        }
      }
    });
  } else {
    res.status(400);
    res.send();
  }
});

router.post('/end', function(req, res) {
  var query = req.body;
  if (
    sanity_check.isRequired(query.key)
  ) {
    var db = req.db;
    var FPUcollection = db.get("FPU");
    var fpu_id = new ObjectId(query.key);
    FPUcollection.remove({_id : fpu_id},
      function(err, del){
        if(err) {
          console.error(err);
          res.status(500);
          res.send();
        } else {
          res.send({
            "status" : "success"
          });
        }
      });
  } else {
    res.status(400);
    res.send();
  }
});

module.exports = router;
