package org.jugendhackt.camera_warner;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.support.annotation.Nullable;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jugendhackt.camera_warner.Data.Camera;
import org.jugendhackt.camera_warner.Data.DataProvider;
import org.jugendhackt.camera_warner.Data.DatabaseDataProvider;
import org.jugendhackt.camera_warner.Data.JuvenalDataProvider;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

/**
 * This Service is supposed to be running in the background permanently to be able to notify the user of any cctv in their surrounding.
 * All data that is received is broadcasted. If additional data (e.g. list of cameras is needed) the ui can start service. The data will then again be broadcasted from {@link #onStartCommand}
 */
public class LocationService extends Service {

    //logging
    public static final String TAG = "LocationService";

    //the interval in which the service wishes to be notified of the users location (expected, min)
    static int INTERVAL = 1000;
    static int FASTEST_INTERVAL = 500;
    private FusedLocationProviderClient mClient;
    //called for the location updates; needed to properly unregister the callback
    private LocationCallback callback;
    //the last received location
    //TODO: check if actually needed
    private Location lastLocation;

    //the data provide from which we will get our data
    private DataProvider provider;
    //TODO: remove because the dataproviders are already implementing caching
    private List<Camera> allCamerasCache = new LinkedList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        mClient = LocationServices.getFusedLocationProviderClient(this);

        //implementation of the location update callback
        //what happens when the service receives the user location is defined here
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.e("LocationService", "getLocation");
                lastLocation = locationResult.getLastLocation();

                sendLastLocationToActivity();

                if (allCamerasCache != null) {
                    if(provider.distanceToNearestCamera(lastLocation.getLatitude(), lastLocation.getLongitude()) < Float.parseFloat(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(R.string.pref_radius_key), getString(R.string.pref_radius_default))))
                    {
                        Log.e(TAG, "a camera is to near");
                    }
                }
            }
        };

        //the DataProvider; any can be inserted here
        //TODO: make the selectable in the settings
        //or
        //TODO: add multiple ones
        provider = new JuvenalDataProvider();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        //try to initialize lastLocation with the systems last known location until we have a gps position
        mClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            lastLocation = location;
                            Log.e(TAG, "sending last location");
                            sendLastLocationToActivity();
                        }
                    }
                });

        //defines what and who often location updates should be received
        LocationRequest request = new LocationRequest()
                .setInterval(INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        //actually request the location updates
        mClient.requestLocationUpdates(request, callback, null);

        //TODO: we're not on the main thread; deactivate network on main thread
        new Thread() {
            @Override
            public void run() {
                Log.e(TAG, "gettingData");
                allCamerasCache = provider.getAllCameras();
                Log.e(TAG, "gotData");

                sendAllCamerasCacheToActivity();
            }
        }.start();

        Log.d(TAG, "started Service");

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    /**
     * Broacast all cameras. Only internally in this packet
     */
    private void sendAllCamerasCacheToActivity() {
        Intent intent = new Intent();
        intent.setAction(getResources().getString(R.string.broadcast_camera));
        ArrayList<Camera> cameras = new ArrayList<>(allCamerasCache);
        intent.putParcelableArrayListExtra("list", cameras);
        intent.setPackage("org.jugendhackt.camera_warner");
        sendBroadcast(intent);
    }

    /**
     * Broacast the location in lastLocation. Only internally in this packet
     */
    private void sendLastLocationToActivity() {
        Intent intent = new Intent();
        intent.setAction(getResources().getString(R.string.broadcast_location));
        intent.putExtra("latitude", lastLocation.getLatitude());
        intent.putExtra("longitude", lastLocation.getLongitude());
        intent.setPackage("org.jugendhackt.camera_warner");
        sendBroadcast(intent);
    }

    @Override
    public void onDestroy() {
        //properly remove the callback
        mClient.removeLocationUpdates(callback);

        super.onDestroy();
    }

    /**
     * not used
     * @param intent
     * @return
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
