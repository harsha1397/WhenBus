var express = require('express')
var router = express.Router()

router.get('/', function(req, res) {
  res.send('SYNC API');
});


router.get('/stops', function(req, res) {
  var db = req.db;
  var collection = db.get('BusStop');
  collection.find({},{fields : {busList: 0, _id : 0}}, function(err, documents) {
    if (err) {
      console.error(err);
      res.status(500);
      res.send();
    } else {
      res.send(documents);
    }
  });
});

router.get('/bus', function(req, res) {
  var db = req.db;
  var collection = db.get('MasterBus');
  collection.find({},
    {fields : {
      busNo : 1,
      source : 1,
      destination : 1,
      _id : 0
    }},
    function(err, documents) {
    if (err) {
      console.error(err);
      res.status(500);
      res.send();
    } else {
      res.send(documents);
    }
  });
});

module.exports = router;
