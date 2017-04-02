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
router.get('/bus', function(req, res) {
  var query = req.query;
  if (
    sanity_check.isRequired(query.dest)
  ) {
    var db = req.db;
    var collection = db.get('BusStop');
    collection.find({}, {}, function(err, documents) {
        res.send(documents);
    });
    // res.send(query);
  } else {
    res.status(400);
    res.send();
  }
});

module.exports = router;
