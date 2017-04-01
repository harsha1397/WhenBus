var mongoose = require('mongoose');
var Schema = mongoose.Schema;
    ObjectId = Schema.ObjectId;

// create FPU schema
var FPUSchema = new Schema({
  location: {
    lat : Number,
    long : Number
  },
  destination : ObjectId,
  boardingPoint : ObjectId,
  boardingTime : Number,
  travelTime : Number,
  bus : ObjectId,
  created_at: Date,
  updated_at: Date
});


var FPU = mongoose.model('FPU', FPUSchema);

// Export FPU model
module.exports = FPU;
