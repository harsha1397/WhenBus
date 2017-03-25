/*
 * The entry point of the server
 */

var express = require('express')
var config = require('./config.js')
var mongoose = require('mongoose');


mongoose.connect('mongodb://localhost/WhenBusDB');


var app = express()

var User = require('./models/User');


app.get('/test', function (req, res) {
	
});

app.get('/', function (req, res) {
  res.send('WhenBus');
});
 
app.listen(config['port']);