# Android Location Provider for Cordova r1.0.0
###### Android Location Provider is a cordova plugin allow users to interact with native android location services, through google play services location provider 15.0.1.

## It's easy to use:

#### Using with cordova:

```javascript
    Location.Provider.< featuer >(args[], callback_handler, error_handler);
    callback_handler = function(args){
        //your code goes here
    }
    error_handler = function(){
        //your error handler goes here
    }
```
#### Using with ionic:

```javascript
    (<any>window).Location.Provider.< featuer >(args[], callback_handler, error_handler);
    function callback_handler(callback_args){
        //your code goes here
    }
    function error_handler {
        //your error handler goes here
    }
```
## Features:

#### Get the most known location of device

```javascript
    getLastKnownLocation(onLocationResult, onError);

    onLocationResult = function(location){
        // This function return json object with latitude and longitude
        var latitude = location.latitude;
        var longitude = location.longitude;
        //your code goes here
    }
    onError = function(){
        //your error handler goes here
    }
```
#### Watching for location updates

```javascript
    getLocationUpdate(updateInterval, fastestUpdateInterval, onLocationUpdate, onError);
    onLocationUpdate = function(location){
        // This function return json object with latitude and longitude
        var latitude = location.latitude;
        var longitude = location.longitude;
    }
    onError = function(){
        //your error handler goes here
    }
```
#### Get formated adderss as string

```javascript
    getLocationAddress(latitude, longitude, onAddressFound, onError);
    onAddressFound = function(address){
        // address is a string with most accurate address found by geocoder

    }
    onError = function(){
        //your error handler goes here
    }
```

## How to install
```bash
    cordova plugin add android-location-provider
```


