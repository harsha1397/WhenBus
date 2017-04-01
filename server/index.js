/*
 * The entry point of the server
 */

var express = require('express')
var router = express.Router()
var mongoose = require('mongoose');

var config = require('./config.js')


mongoose.connect('mongodb://localhost/WhenBusDB');

var app = express();

router.get('/', function (req, res) {
  res.send('WhenBus-v1.0');
});


// Add the routers defined
app.use('/',router);

app.listen(config['port'], () => {
	console.log("Listening to port "+config['port']);
});
