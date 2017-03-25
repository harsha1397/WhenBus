var mongoose = require('mongoose');
var Schema = mongoose.Schema,
    ObjectId = Schema.ObjectId;

// Bus Schema
var BusSchema = new Schema({
  id : Number,
  busNo : String,
  parent : ObjectId,
  averageSpeed : Number,
  Timings : [{busStop : ObjectId, Time : Number}]
});

var Bus = mongoose.model('Bus', BusSchema);

// Export Bus model
module.exports = Bus;