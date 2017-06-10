package org.jugendhackt.camera_warner;

import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
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

import java.util.List;


/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

public class LocationService extends Service{
    private final IBinder binder = new Binder();

    static int INTERVAL = 1000 * 30;
    static int FASTEST_INTERVAL = 1000 * 15;
    private FusedLocationProviderClient mClient;
    private LocationCallback callback;
    private Location lastLocation;

    private DataProvider provider;
    public boolean camerasAvailable;
    private List<Camera> allCamerasCache;

    final Messenger mMessenger = new Messenger(new IncomingHandler());

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 13:
                    Log.d("LocationService", "message arrived");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        mClient = LocationServices.getFusedLocationProviderClient(this);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                lastLocation = locationResult.getLastLocation();
            }
        };
        LocationRequest request = new LocationRequest()
                .setInterval(INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mClient.requestLocationUpdates(request, callback, null);

        mClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if(location != null)
                        {
                            lastLocation = location;
                        }
                    }
                });

        provider = new DatabaseDataProvider();
        new Runnable() {
            @Override
            public void run() {
                allCamerasCache = provider.getAllCameras();
            }
        }.run();
    }

    @Override
    public IBinder onBind(Intent p1) {
        return mMessenger.getBinder();
    }

    public List<Camera> getAllCameras()
    {
        return allCamerasCache;
    }

    public Location getLastLocation()
    {
        return lastLocation;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        mClient.removeLocationUpdates(callback);

        super.onDestroy();
    }
}
