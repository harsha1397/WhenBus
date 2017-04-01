var mongoose = require('mongoose');
var Schema = mongoose.Schema,
    ObjectId = Schema.ObjectId;

// Bus Schema
var BusSchema = new Schema({
  id : Number,
  busNo : String,
  parent : ObjectId,
  averageSpeed : Number,
  Timings : [{busStop : ObjectId, time : Number}],
  averageDelay : Number,
  currLoc : {
    lat : Number,
    lng : Number
  }
});

var Bus = mongoose.model('Bus', BusSchema);

// Export Bus model
module.exports = Bus;
