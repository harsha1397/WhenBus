var express = require('express')
var router = express.Router()

var sanity_check = require('../common/sanity.js')

router.get('/', function(req, res) {
  res.send('FEEDBACK API');
});


router.get('/send', function(req, res) {
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
