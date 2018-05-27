package com.cordova.plugin.location.provider;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.widget.Toast;

import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;

import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static android.app.Activity.RESULT_CANCELED;


//Native interface class as defined in plugin.xml
public class LocationProvider extends CordovaPlugin implements LocationController.onLocationUpdated, LocationController.onAddressFound {

    private final int REQUEST_CHECK_SETTINGS = 105;
    private final int LOCATION_SETTING_REQUEST = 106;
    private LocationController mLocationController;
    public CallbackContext updateLocation;
    public CallbackContext addressCallBack;

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        mLocationController = new LocationController(cordova.getActivity());
    }

    /*
     * Communicate with  js interface and accept methods and parameters;
     * @param action: called method by name as string from js interface;
     * @param args: json array contains argument passed to action;
     * @param callbackContext: callback function success and error;
     * @return: boolean as true if action found and called and false else;
     */
    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        switch (action){
            case "getCurrentLocation":
                getLastKnownLocation(callbackContext);
                return true;
            case "getLocationUpdate":
                getLocationUpdate(args.getInt(0), args.getInt(1), callbackContext);
                return true;
            case "getLocationAddress":
                getLocationAddress(args.getDouble(0), args.getDouble(1), callbackContext);
                return true;
        }
        return super.execute(action, args, callbackContext);
    }

    /*
     * Interface for js to get current location using google play service;
     * @return: success callback contain the current location in form of longitude and latitude;
     */
    private void getLastKnownLocation(CallbackContext callbackContext) throws JSONException {
        if(mLocationController.getmCurrentLocation() != null){
            Location location = mLocationController.getmCurrentLocation();
            //Return location latitude and longitude as json object
            JSONObject currentLocation = new JSONObject();
            currentLocation.put("latitude",location.getLatitude());
            currentLocation.put("longitude",location.getLongitude());
            callbackContext.success( currentLocation);
        } else {
            // In case of null location
            callbackContext.error("can't get location on this time try again later");
        }

    }

    /*
     * Interface for js to get location update using google play service;
     * @param: interval => an integer refer to interval of getting location update;
     * @param: fastestUpdateInterval =>  the fastest rate in milliseconds at which your app can handle location updates;
     * @return: plugin result not a success callback to remain the listener;
     */
    private void getLocationUpdate(int updateInterval,int fastestUpdateInterval,CallbackContext callbackContext) throws JSONException {

        // set new location request parameter
        mLocationController.getLocationUpdate(this,updateInterval,fastestUpdateInterval);
        // Get current location
        Location location = mLocationController.getmCurrentLocation();
        JSONObject currentLocation = new JSONObject();
        if(location != null){
            //Return location latitude and longitude as json object
            currentLocation.put("latitude",location.getLatitude());
            currentLocation.put("longitude",location.getLongitude());
        }

        // Return plugin result to keep callback
        PluginResult pluginResult = new PluginResult(PluginResult.Status.OK, currentLocation);
        pluginResult.setKeepCallback(true); // keep callback
        callbackContext.sendPluginResult(pluginResult);
        updateLocation = callbackContext;

    }

    /*
     * Interface for js to get location Address using Geocoder service;
     * @param: latitude => a double refer to latitude of requesting address;
     * @param: longitude =>  a double refer to longitude of requesting address;
     * @return: plugin  success callback if address is found || and error callback in case of null address;
     */

    private void getLocationAddress(double latitude, double longitude, CallbackContext callbackContext){
        mLocationController.getAddress(this, latitude, longitude);
        addressCallBack = callbackContext;
    }

    /*
     * Interface for js to stop google play service;
     * must be called at onPause or onDestroy;
     */
    private void stop(){

    }

    @Override
    public void setOnLocationUpdate(Location location) {
        if(updateLocation != null){

            // Get current location
            //Return location latitude and longitude as json object
            JSONObject currentLocation = new JSONObject();
            try {
                currentLocation.put("latitude",location.getLatitude());
                currentLocation.put("longitude",location.getLongitude());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Return plugin result to keep callback
            PluginResult pluginResult = new PluginResult(PluginResult.Status.OK,currentLocation);
            pluginResult.setKeepCallback(true); // keep callback
            updateLocation.sendPluginResult(pluginResult);
        }
    }

    @Override
    public void setOnAddressFound(String address) {

        if(addressCallBack != null){
            addressCallBack.success(address);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(requestCode == REQUEST_CHECK_SETTINGS){
            //Check if the user give answer ok or cancel
            if(resultCode == RESULT_CANCELED){
                //Alert user to open location settings
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(cordova.getContext());
                alertDialog.setTitle("Location is disabled");
                alertDialog.setMessage("Please enable location to get location updates");
                alertDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Open location Setting
                        Intent viewIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);

                        cordova.startActivityForResult(LocationProvider.this,viewIntent,LOCATION_SETTING_REQUEST);

                    }
                });
                alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                alertDialog.create().show();

            }

        }

    }


    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions, int[] grantResults) throws JSONException {

        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            //permission is granted
            //initiate fused location
            mLocationController = new LocationController(cordova.getActivity());
        }
    }

    @Override
    public void onStop() {
        //Stop google api client
        mLocationController.stop();
        super.onStop();
    }

}


