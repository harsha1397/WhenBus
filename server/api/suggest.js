var express = require('express')
var router = express.Router()

var sanity_check = require('../common/sanity.js')

router.get('/', function(req, res) {
  res.send('SUGGEST API');
});

// Send the Nearest Bus Stop Information
// Coordinates and Name
// Logic : Send the Geographically the Nearest Bus Stop
router.get('/nearestStop', function(req, res) {
  var query = req.query;
  if (
    sanity_check.isRequired(query.coord) &&
    sanity_check.isRequired(query.coord.lat) &&
    sanity_check.isRequired(query.coord.lng)
  ) {

    res.send(query);
  } else
    res.status(400);
    res.send();
});

// Send atmost 5 possible Bus Numbers that reach Destination
// * Infer source from Location of user [ or explicitly mentioned ]
// Logic : Intersection of bus Numbers at destination and source
router.get('/bus', function(req, res) {
  var query = req.query;
  if (
    sanity_check.isRequired(query.dest)
  ) {
    
    res.send(query);
  } else {
    res.status(400);
    res.send();
  }
});

module.exports = router;
