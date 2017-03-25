var mongoose = require('mongoose');
var Schema = mongoose.Schema;

// BusStop Schema
var busStopSchema = new Schema({
  name: String,
  coordinates : {
  	lat : Number,
  	long : Number
  },
  busList : [String],
  created_at: Date,
  updated_at: Date
});

var BusStop = mongoose.model('BusStop', busStopSchema);

// Export BusStop model
module.exports = BusStop;