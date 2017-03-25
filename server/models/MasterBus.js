var mongoose = require('mongoose');
var Schema = mongoose.Schema,
		ObjectId = Schema.ObjectId;

// MasterBus Schema
var MasterBusSchema = new Schema({
  busNo : String,
  source : ObjectId, // BusStop ObjectId
  destination : ObjectId, // BusStop ObjectId
  serviceType : String,
  noOfBuses : Number,
  averageTravelTime : Number,
  busStopList : [ObjectId],
  busList : [ObjectId],
  created_at: Date,
  updated_at: Date
});

var MasterBus = mongoose.model('MasterBus', MasterBusSchema);

// Export MasterBus model
module.exports = MasterBus;