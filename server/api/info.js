var express = require('express')
var router = express.Router()

var sanity_check = require('../common/sanity.js')
var stopLoc = require('../common/stopLoc.js')
var util = require('./util.js')

router.get('/', function(req, res) {
  res.send('INFO API');
});


router.post('/bus', function(req, res) {
  var query = req.body;
  if (
    sanity_check.isRequired(query.bus_no) &&
    sanity_check.isRequired(query.start_point) &&
    sanity_check.isRequired(query.end_point) &&
    sanity_check.isRequired(query.coord) &&
    sanity_check.isRequired(query.coord.lat) &&
    sanity_check.isRequired(query.coord.lng)
  ) {
    var db = req.db;
    var nearestStopPromise;
    if (query.src) {
      nearestStopPromise = new Promise((resolve, reject) => {
        resolve(query.src);
      });
    } else {
      nearestStopPromise = new Promise((resolve, reject) => {
        var collection = db.get('MasterBus');
        collection.findOne(
          {
            busNo : query.bus_no,
            source : query.start_point,
            destination : query.end_point,
          },
          {},
          function(err, entity) {
            if (err) reject(err);
            else {
              coordinatePromise = []
              entity.busStopList.forEach((busStop, i) => {
                var p = new Promise((accept, decline) => {
                  var StopCollection = db.get('BusStop');
                  StopCollection.findOne(
                    {"stopName": busStop },
                    {},
                    function(err, element) {
                      if (err) decline(err);
                      var d = util.distance(element.coord, query.coord);
                      accept({
                          "distance" : d,
                          "stopName" : busStop
                      });
                    }
                  );
                });
                coordinatePromise.push(p);
              });
              Promise.all(coordinatePromise).then((distances) => {
                distances.sort((A,B) => {
                  return A.distance - B.distance;
                })
                resolve(distances[0].stopName);
              }).catch( (reason) => {
                reject(reason);
              });

            }
          }
        );
      });
    }

    nearestStopPromise.then((nearestStop) => {

      var BusCollection = db.get('Bus');
      BusCollection.find(
        {
          busNo : query.bus_no,
          source : query.start_point,
          destination : query.end_point,
        },
        {},
        function(err, documents) {
          documents = documents.map((document) => {
            var time = document.Timings.filter((entry) => {
              return (entry.busStop === nearestStop)
            })[0].time;

            var stopList = document.Timings.map((entry) => {
              return entry.busStop;
            });

            var src;
            if (query.src ) {
              src = query.src;
            } else {
              src = nearestStop;
            }

            var dest;
            if (query.dest) {
              dest = query.dest;
            } else {
              dest = query.end_point;
            }

            var src_index, dest_index;
            for(var i=0; i < stopList.length; i++) {
              if (stopList[i] == src)
                src_index = i;
              else if (stopList[i] == dest)
                dest_index = i;
              else
                continue;
            }
            stopList = stopList.splice(src_index, dest_index+1);

            var busLocation;
            if (document.currLoc && ((new Date() - document.updateAt) <= 600000) ) {
              busLocation = document.currLoc;
            } else {
              var timings = document.Timings;
              timings.sort((A,B) => {
                return A.time - B.time;
              });

              var date = new Date();
              var threshold = date.getHours()*60 + date.getMinutes();

              timings = timings.filter((A) => {
                return A.time > threshold;
              });

              if (timings.length === 0) {
                timings.push(document.Timings[0]);
              }

              busLocation = stopLoc[timings[0].busStop];
            }

            return {
              "id"  : document.id,
              "stop" : nearestStop,
              "busLoc" : busLocation,
              "time" : time,
              "stopList": stopList
            }
          });

          var date = new Date();
          var threshold = date.getHours()*60 + date.getMinutes();



          documents = documents.filter((document) => {
            return (document.time >= threshold);
          });

          documents.sort((A,B) => {
            return A.time - B.time;
          });


          res.send(documents[0]);
        }
      );

    }).catch((reason) => {
      console.error(reason);
      res.status(500);
      res.send();
    });

  } else {
    res.status(400);
    res.send();
  }
});

module.exports = router;
