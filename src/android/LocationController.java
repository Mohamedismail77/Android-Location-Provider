package com.cordova.plugin.location.provider;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

public class LocationController implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Context mContext;
    private Activity mActivity;
    private LocationRequest mLocationRequest;
    private FusedLocationProviderClient mFusedLocationClient;
    private GoogleApiClient mGoogleApiClient;
    private Task<LocationSettingsResponse> task;
    private final int REQUEST_CHECK_SETTINGS = 105;
    private final int LOCATION_SETTING_REQUEST = 106;
    private Location mCurrentLocation;
    private boolean mRequestingLocationUpdates = false;
    private LocationCallback mLocationCallback;
    private final String REQUEST_LOCATION_UPDATES_KEY = "location_update";
    private final String CURRENT_LOCATION_KEY = "current_location";
    private int LOCATION_UPDATE_INTERVAL = 1000;
    private int LOCATION_FASTEST_UPDATE_INTERVAL = 400;
    private AddressResultReceiver mResultReceiver;
    private String mAddressOutput;
    private String PACKAGE_NAME = "package_name";

    private onLocationUpdated onLoactionUpdated;
    public interface onLocationUpdated{
        void setOnLocationUpdate(Location location);
    }

    private onAddressFound onAddressFound;
    public interface onAddressFound{
        void setOnAddressFound(String address);
    }

    public LocationController(Activity activity){
        mActivity = activity;
        mContext = activity.getApplicationContext();
        //Create google api client
        mGoogleApiClient = new GoogleApiClient.Builder(mContext)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();
        // Connect google api client
        mGoogleApiClient.connect();

    }

    //Initiate FusedLocation and location request
    private void setmFusedLocationClient(int updateInterval, int fastestUpdateInterval){
        if (checkForLocationPermissions()) {
            mFusedLocationClient = LocationServices.getFusedLocationProviderClient(mContext);
            setLocationRequest(LocationRequest.PRIORITY_HIGH_ACCURACY,
                    updateInterval,
                    fastestUpdateInterval);
        }
    }

    //Check for android sdk version
    //if sdk >= 23 ask for permission
    private boolean checkForLocationPermissions(){

        if(Build.VERSION.SDK_INT >= 23){
            //Check for location permissions which declared in manifest
            if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(mActivity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
            }else{
                return true;
            }
        } else {
            return true;
        }
        return false;
    }

    /* Setting the parameter to set the location request
     * @param: priority => the priority of the request, which gives the Google Play services location services,
     *         a strong hint about which location sources to use.Check documentation for supported values;
     *
     * @param: updateInterval => the rate in milliseconds at which your app prefers to receive location updates.
     *        **Note that the location updates may be faster than this rate if another app is receiving updates,
     *        at a faster rate, or slower than this rate, or there may be no updates at all
     *        (if the device has no connectivity, for example);
     *
     * @param: fastestUpdateInterval =>  the fastest rate in milliseconds at which your app can handle location updates;
     *
     */

    public void setLocationRequest(final int priority, final int updateInterval, final int fastestUpdateInterval){
        //Get current location settings
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        //Next check whether the current location settings are satisfied
        SettingsClient client = LocationServices.getSettingsClient(mActivity);
        task = client.checkLocationSettings(builder.build());

        //Check setting if valid or not
        task.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                if (mRequestingLocationUpdates) {
                    startLocationUpdates();
                } else {
                    if(mFusedLocationClient != null && mCurrentLocation == null){
                        getLastKnownLocation();
                    }
                }
            }
        });

        //Check for that all settings are satisfied
        task.addOnSuccessListener(mActivity, new OnSuccessListener<LocationSettingsResponse>() {
            @Override
            public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                // All location settings are satisfied. The client can initialize
                // location requests here.
                //Initiate Location request
                mLocationRequest = new LocationRequest();
                //Set it's accuracy to high
                mLocationRequest.setPriority(priority);
                //Set update interval to be every 1s
                mLocationRequest.setInterval(updateInterval);
                //Set the upper limit to update rate
                mLocationRequest.setFastestInterval(fastestUpdateInterval);


            }
        });

        task.addOnFailureListener(mActivity, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                if (e instanceof ResolvableApiException) {
                    // Location settings are not satisfied, but this can be fixed
                    // by showing the user a dialog.
                    try {
                        // Show the dialog by calling startResolutionForResult(),
                        // and check the result in onActivityResult().
                        ResolvableApiException resolvable = (ResolvableApiException) e;
                        resolvable.startResolutionForResult(mActivity,
                                REQUEST_CHECK_SETTINGS);
                    } catch (IntentSender.SendIntentException sendEx) {
                        // Ignore the error.
                    }
                }
            }
        });



    }

    /*
     * Get last known location from fused location this
     * checking if the location is null and resolve this problem;
     *
     */
    @SuppressLint("MissingPermission")
    private void getLastKnownLocation(){

        mFusedLocationClient.flushLocations();
        mFusedLocationClient.getLastLocation().addOnSuccessListener(mActivity, new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    mCurrentLocation = location;
                } else {
                    //check location setting
                    //Get current location settings
                    //Get current location settings
                    setmFusedLocationClient(LOCATION_UPDATE_INTERVAL, LOCATION_FASTEST_UPDATE_INTERVAL);
                    LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                            .addLocationRequest(mLocationRequest);
                    //Next check whether the current location settings are satisfied
                    SettingsClient client = LocationServices.getSettingsClient(mActivity);
                    task = client.checkLocationSettings(builder.build());
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if(checkForLocationPermissions()){
            mFusedLocationClient.requestLocationUpdates(mLocationRequest,
                    mLocationCallback,
                    null /* Looper */);
        }

    }

    private void stopLocationUpdates() {
        mRequestingLocationUpdates = false;
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        // Check for location permissions is granted or not
        // Initiate fusedLocationClient
        setmFusedLocationClient(LOCATION_UPDATE_INTERVAL, LOCATION_FASTEST_UPDATE_INTERVAL);
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        try {
            if (connectionResult.getResolution() != null) {
                connectionResult.getResolution().send();
            }
        } catch (PendingIntent.CanceledException e) {
            e.printStackTrace();
        } catch (NullPointerException e){
            e.printStackTrace();
        }
    }

    public Location getmCurrentLocation(){
        return mCurrentLocation;
    }

    public void getLocationUpdate(LocationProvider provider, int updateInterval,int fastestUpdateInterval){

        LOCATION_UPDATE_INTERVAL = updateInterval;
        LOCATION_FASTEST_UPDATE_INTERVAL = fastestUpdateInterval;
        //set new location setting
        setmFusedLocationClient(LOCATION_UPDATE_INTERVAL, LOCATION_FASTEST_UPDATE_INTERVAL);
        onLoactionUpdated = (onLocationUpdated) provider;
        mRequestingLocationUpdates = true;
        //set new location request settings
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);
        //Next check whether the current location settings are satisfied
        SettingsClient client = LocationServices.getSettingsClient(mActivity);
        task = client.checkLocationSettings(builder.build());

        // Initiate Location callback to start fetching location updates
        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    return;
                }
                for (Location location : locationResult.getLocations()) {
                    // Update with location data
                    mCurrentLocation = location;
                    onLoactionUpdated.setOnLocationUpdate(mCurrentLocation);
                }
            }
        };

    }

    public void getAddress(LocationProvider provider, double latitude, double longitude){

        onAddressFound = (onAddressFound) provider;
        // Check if Geocoder is available
        if (!Geocoder.isPresent()) {
            Toast.makeText(mContext,
                    "No geCoder available ",
                    Toast.LENGTH_LONG).show();
            return;
        }
        // Start service and update UI to reflect new location
        mResultReceiver = new AddressResultReceiver(new Handler());

        // Start background services
        Intent intent = new Intent(mContext, FetchAddressIntentService.class);
        intent.putExtra(FetchAddressIntentService.Constants.RECEIVER, mResultReceiver);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_LATITUDE_EXTRA, latitude);
        intent.putExtra(FetchAddressIntentService.Constants.LOCATION_LONGITUDE_EXTRA, longitude);
        intent.putExtra(PACKAGE_NAME,mActivity.getApplicationContext().getPackageName());
        mActivity.startService(intent);
    }


    class AddressResultReceiver extends ResultReceiver {
        private AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            if (resultData == null) {
                return;
            }

            // Display the address string
            // or an error message sent from the intent service.
            mAddressOutput = resultData.getString(FetchAddressIntentService.Constants.RESULT_DATA_KEY);
            if (mAddressOutput == null) {
                mAddressOutput = "";
            }
            //return address to js interface
            onAddressFound.setOnAddressFound(mAddressOutput);
            // Show a toast message if an address was found.
            if (resultCode == FetchAddressIntentService.Constants.SUCCESS_RESULT) {
                Toast.makeText(mContext, "Address found", Toast.LENGTH_SHORT).show();
            }

        }
    }

    public void stop(){
        mGoogleApiClient.disconnect();
        stopLocationUpdates();
    }

}