cordova.define("android-location-provider.LocationProvider", function(require, exports, module) {
  var exec = require('cordova/exec');
  
  /* Get last known location as pair of longtidue and latidude
   * @param: success => callback will fired if location is successfully retrieve
   * @param: error => callback will fired if location is null 
   */
  exports.getLastKnownLocation = function (success, error) {
      //check if typeof success is function
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
  
  /* GetLocation update as pair of longtidue and latidude;
   * @param: updateInterval => the rate in milliseconds at which your app prefers to receive location updates.
   *        **Note that the location updates may be faster than this rate if another app is receiving updates,
   *        at a faster rate, or slower than this rate, or there may be no updates at all
   *        (if the device has no connectivity, for example);
   *
   * @param: fastestUpdateInterval =>  the fastest rate in milliseconds at which your app can handle location updates;
   * @param: success => callback will fired if location is successfully retrieve
   * @param: error => callback will fired if location is null 
   */
  exports.getLocationUpdate = function (updateInterval, fastestUpdateInterval, success, error) {
      //check if typeof success is function
      if(typeof success !== "function"){
        //throw an error to user
        throw new Error("Location Provider need a onRequestLocation function");
      } else if (typeof error !== "function") {
        //throw an error to user
        throw new Error("Please define the an error callback as function");
      } else if(typeof updateInterval !== "number") {
          throw new Error("Please define the interval to get location updates as number");
      } else if(typeof fastestUpdateInterval !== "number") {
          throw new Error("Please define the interval to get location updates as number");
      } else{
        //call native side to get current location
        exec(success, error, 'LocationProvider', 'getLocationUpdate', [updateInterval, fastestUpdateInterval]);
      }
  };
  
  /*
   * Interface for js to get location Address using Geocoder service;
   * @param: latitude => a double refer to latitude of requesting address;
   * @param: longitude =>  a double refer to longitude of requesting address;
   * @param: success => callback will fired if address is found;
   * @param: error => callback will fired if address is null or something is wrong;
   */
   exports.getLocationAddress = function (latitude, longitude, success, error) {
      //check if typeof message is string
      if(typeof success !== "function"){
        //throw an error to user
        throw new Error("Location Provider need a onRequestLocation function");
      } else if (typeof error !== "function") {
        //throw an error to user
        throw new Error("Please define the an error callback as function");
      } else if(typeof latitude !== "number") {
          //throw an error to user
          throw new Error("Please define the latitude to get location updates as number");
      } else if (typeof longitude !== "number") {
          throw new Error("Please define the longitude to get location updates as number");
      } else{
        //call native side to get current location
        exec(success, error, 'LocationProvider', 'getLocationAddress', [latitude,longitude]);
      }
  };
  
  });
  