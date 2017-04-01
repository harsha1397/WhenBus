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
var suggestRouter = require('./api/suggest.js')

app.use('/',router);
app.use('/suggest/',suggestRouter);

// Bind to a config.port
app.listen(config['port'], () => {
	console.log("Listening to port "+config['port']);
});
