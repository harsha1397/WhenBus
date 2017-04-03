var express = require('express')
var router = express.Router()

var sanity_check = require('../common/sanity.js')
var ObjectId = require('mongodb').ObjectId;

router.get('/', function(req, res) {
  res.send('FEEDBACK API');
});

router.post('/access', function(req, res) {
  var query = req.body;
  if (
    sanity_check.isRequired(query.end) &&
    sanity_check.isRequired(query.src) &&
    sanity_check.isRequired(query.busNo) &&
    sanity_check.isRequired(query.id) &&
    sanity_check.isRequired(query.source) &&
    sanity_check.isRequired(query.destination)
  ) {
    var db = req.db;
    var collection = db.get("FPU");
    collection.insert(query, function(err, record) {
      if (err) {
        console.error(err);
        res.status(500);
        res.send();
      } else {
        res.send({
          "key" : record._id
        });
      }
    });
  } else {
    res.status(400);
    res.send();
  }
});

router.post('/send', function(req, res) {
  var query = req.body;
  if (
    sanity_check.isRequired(query.key) &&
    sanity_check.isRequired(query.coord) &&
    sanity_check.isRequired(query.coord.lat) &&
    sanity_check.isRequired(query.coord.lng)
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
        var BusCollection = db.get('Bus');
        if (query.stop) {
          BusCollection.findOne(
            {
              "id" : FPU.id,
              "busNo" : FPU.busNo,
              "source" : FPU.source,
              "destination": FPU.destination
            },
            {},
            function(err, bus) {
              if (err) {
                console.error(err);
                res.status(500);
                res.send();
              } else {
                var timings = bus.Timings;
                var delta = 0;
                for(var i=0; i<timings.length; i++) {
                  if (timings[i].busStop === query.stop) {
                    var date = new Date();
                    var currTime = date.getHours()*60 + date.getMinutes();
                    delta = timings[i].time - currTime;
                  }
                  timings[i].time -= delta;
                }

                BusCollection.update(
                  {
                    "id" : FPU.id,
                    "busNo" : FPU.busNo,
                    "source" : FPU.source,
                    "destination": FPU.destination
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
                      if (FPU.end == query.stop) {
                        FPUcollection.remove({_id : fpu_id},
                          function(err, del){
                            if(err) {
                              console.error(err);
                              res.status(500);
                              res.send();
                            } else {
                              res.send({
                                "status" : "DROP"
                              })
                            }
                          });
                      } else {
                        res.send({
                          "status" : "OK"
                        })
                      }
                    }
                  }
                );
              }
            });
        } else {        // Just got co-ordinates
          res.send({
            "status" : "DROP"
          });
        }
      }
    });
  } else {
    res.status(400);
    res.send();
  }
});

module.exports = router;
