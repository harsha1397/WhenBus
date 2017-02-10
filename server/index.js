/*
 * The entry point of the server
 */

var express = require('express')
var config = require('./config.js')
var app = express()
 
app.get('/', function (req, res) {
  res.send('WhenBus')
})
 
app.listen(config['port'])