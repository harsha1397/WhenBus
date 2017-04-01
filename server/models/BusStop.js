var mongoose = require('mongoose');
var Schema = mongoose.Schema;

// BusStop Schema
var busStopSchema = new Schema({
  stopName: String,
  coord : {
  	lat : Number,
  	lng : Number
  },
  busList : [String],
  created_at: Date,
  updated_at: Date
});

var BusStop = mongoose.model('BusStop', busStopSchema);

// Export BusStop model
module.exports = BusStop;
