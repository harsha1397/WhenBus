/*
 * The entry point of the server
 */

var express = require('express');
var bodyParser = require('body-parser')
var router = express.Router();

var config = require('./config.js');

var mongo = require('mongodb');
var monk = require('monk');
var db = monk('localhost:27017/WhenBusDB');

var app = express();

// parse application/json
app.use(bodyParser.json())

// parse application/x-www-form-urlencoded
app.use(bodyParser.urlencoded({ extended: false }))


// Make db accessible to routers
app.use(function(req,res,next){
    req.db = db;
    console.log("REQ : \n",req.body);
    next();
});

router.get('/', function (req, res) {
  res.send('WhenBus-v1.0');
});


// Add the routers defined
var suggestRouter = require('./api/suggest.js')
var infoRouter = require('./api/info.js')
var feedbackRouter = require('./api/feedback.js')
var syncRouter = require('./api/sync.js')

app.use('/', router);
app.use('/suggest/', suggestRouter);
app.use('/info/', infoRouter);
app.use('/feedback/', feedbackRouter);
app.use('/sync/', syncRouter);

// Bind to a config.port
app.listen(config['port'], () => {
	console.log("Listening to port "+config['port']);
});
