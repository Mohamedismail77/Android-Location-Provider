package com.cordova.plugin.location.provider;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FetchAddressIntentService extends IntentService {
    protected ResultReceiver mReceiver;
    public static String mPackageName;
    private String PACKAGE_NAME_KEY = "package_name";


    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */

    public FetchAddressIntentService() {
        super("FetchAddressIntentService");
    }


    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        //Creating the geoCoder to get the address according to local setting of the user
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        if (intent == null) {
            return;
        }
        String errorMessage = "";
        String TAG = "RequestState";
        // Get the location passed to this service through an extra.
        double latitude = intent.getDoubleExtra(Constants.LOCATION_LATITUDE_EXTRA,0);
        double longitude = intent.getDoubleExtra(Constants.LOCATION_LONGITUDE_EXTRA,0);
        mPackageName = intent.getStringExtra(PACKAGE_NAME_KEY);
        mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

        // Creating an empty list to store received addresses

        List<Address> addresses = null;

        try {
            // Getting the address from location provided to intent
            addresses = geocoder.getFromLocation(
                    latitude,
                    longitude,
                    // In this sample, get just a single address.
                    1);
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            errorMessage = "Service not available";
            Log.e(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            errorMessage = "Invalied latitude or longitude values";
            Log.e(TAG, errorMessage + ". " +
                    "Latitude = " + latitude +
                    ", Longitude = " +
                    longitude, illegalArgumentException);
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size()  == 0) {
            if (errorMessage.isEmpty()) {
                errorMessage = "No Addresses found";
                Log.e(TAG, errorMessage);
            }
            deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
        } else {
            Address address = addresses.get(0);
            ArrayList<String> addressFragments = new ArrayList<>();

            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for(int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                addressFragments.add(address.getAddressLine(i));
            }
            Log.i(TAG, "Address found");
            deliverResultToReceiver(Constants.SUCCESS_RESULT,
                    TextUtils.join(System.getProperty("line.separator"),
                            addressFragments));
        }


    }

    /* Result receiver
     * @param: resultCode => to provide the state of request as success or failure
     * @param: message => the returned message to requested activity
     */


    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

    public static final class Constants {
        public static final int SUCCESS_RESULT = 0;
        private static final int FAILURE_RESULT = 1;
        private static final String PACKAGE_NAME = mPackageName;

        public static final String RECEIVER = PACKAGE_NAME + ".RECEIVER";
        public static final String RESULT_DATA_KEY = PACKAGE_NAME +
                ".RESULT_DATA_KEY";
        public static final String LOCATION_LATITUDE_EXTRA = PACKAGE_NAME +
                ".LOCATION_LATITUDE_EXTRA";

        public static final String LOCATION_LONGITUDE_EXTRA = PACKAGE_NAME +
                ".LOCATION_LONGITUDE_EXTRA";
    }
}
