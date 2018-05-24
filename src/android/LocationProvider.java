package com.cordova.plugin.location.provider;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.location.Location;
import android.widget.Toast;
import android.content.Context;


//Native interface class as defined in plugin.xml
public class LocationProvider extends CordovaPlugin {

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
    }

    /*
    * Communicate with  js interface and accept methods and parameters;
    * @param action: called method by name as string from js interface;
    * @param 
    */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        return super.execute(action, args, callbackContext);
    }

    /*
    * Interface for js to get current location using google play service;
    * @return: success callback contain the current location in form of longitude and latitude;
    */
    private void getLastKnownLocation(){

    }

    /*
    * Interface for js to get location update using google play service;
    * @param: interval => an integer refer to interval of getting location update;
    * @param: fastestUpdateInterval =>  the fastest rate in milliseconds at which your app can handle location updates;
    * @return: plugin result not a success callback to remain the listener;
    */
    private void getLocationUpdate(int updateInterval,int fastestUpdateInterval){

    }
    
    /*
    * Interface for js to get location Address using Geocoder service;
    * @param: latitude => a double refer to latitude of requesting address;
    * @param: longitude =>  a double refer to longitude of requesting address;
    * @return: plugin  success callback if address is found || and error callback in case of null address;
    */

    private void getLocationAddress(double latitude, double longitude){

    }

    /*
    * Interface for js to stop google play service;
    * must be called at onPause or onDestroy;
    */
    private void stop(){

    }

}


