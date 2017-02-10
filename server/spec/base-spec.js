/*
 * Basic Testing module 
 *
 * description : Example Test case
 *
 */
var request = require("request");
var config = require("../config.js");

var base_url = "http://localhost:"+config['port']+"/";


describe("WhenBus Server", function() {
	describe("GET /", function() {
		
		it("returns status code 200", function(done) {
			request.get(base_url, function(error, response, body) {
				expect(response.statusCode).toBe(200);
				done();
      });
    });

    it("returns WhenBus", function(done) {
      request.get(base_url, function(error, response, body) {
        expect(body).toBe("WhenBus");
        done();
      });
    });

  });
});