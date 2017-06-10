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

public class LocationService extends Service {

    //TODO: add (proper) documentation

    public static final String TAG = "LocationService";

    static int INTERVAL = 1000;
    static int FASTEST_INTERVAL = 500;
    private FusedLocationProviderClient mClient;
    private LocationCallback callback;
    private Location lastLocation;

    private DataProvider provider;
    private List<Camera> allCamerasCache = new LinkedList<>();

    @Override
    public void onCreate() {
        super.onCreate();

        mClient = LocationServices.getFusedLocationProviderClient(this);

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

        provider = new JuvenalDataProvider();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
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

        LocationRequest request = new LocationRequest()
                .setInterval(INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        mClient.requestLocationUpdates(request, callback, null);
        new Thread() {
            @Override
            public void run() {
                Log.e(TAG, "gettingData");
                allCamerasCache = provider.getAllCameras();
                Log.e(TAG, "gotData");

                sendAllCamerasCacheToActivity();
            }
        }.start();
        Log.d(TAG, "starting Service");
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    private void sendAllCamerasCacheToActivity() {
        Intent intent = new Intent();
        intent.setAction(getResources().getString(R.string.broadcast_camera));
        ArrayList<Camera> cameras = new ArrayList<>(allCamerasCache);
        intent.putParcelableArrayListExtra("list", cameras);
        intent.setPackage("org.jugendhackt.camera_warner");
        sendBroadcast(intent);
    }

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
        mClient.removeLocationUpdates(callback);

        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
