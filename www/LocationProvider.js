var exec = require('cordova/exec');

//watching for location updates as pair of longtidue and latidude
exports.getCurrentLocation = function (success, error) {
    //check if typeof message is string
    if(typeof success !== "function"){
      //throw an error to user
      throw new Error("Location Provider need a onRequestLocation function");
    } else if (typeof error !== "function") {
      //throw an error to user
      throw new Error("Please define the an error callback as function");
    } else{
      //call native side to get current location
      exec(success, error, 'LocationProvider', 'getCurrentLocation', []);
    }
};

//watching for location updates as pair of longtidue and latidude
exports.getLocationUpdate = function (interval, success, error) {
    //check if typeof message is string
    if(typeof success !== "function"){
      //throw an error to user
      throw new Error("Location Provider need a onRequestLocation function");
    } else if (typeof error !== "function") {
      //throw an error to user
      throw new Error("Please define the an error callback as function");
    } else if(typeof interval !== "number") {
        throw new Error("Please define the interval to get location updates as number");
    } else{
      //call native side to get current location
      exec(success, error, 'LocationProvider', 'getCurrentLocation', []);
    }
};
