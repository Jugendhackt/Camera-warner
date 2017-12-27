package org.jugendhackt.camera_warner.Services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jugendhackt.camera_warner.Data.Providers.DataProvider;
import org.jugendhackt.camera_warner.Data.Providers.FakeCameraProvider;
import org.jugendhackt.camera_warner.Data.Providers.JuvenalDataProvider;
import org.jugendhackt.camera_warner.Data.Providers.OSMDataProvider;
import org.jugendhackt.camera_warner.MapsActivity;
import org.jugendhackt.camera_warner.R;
import org.jugendhackt.camera_warner.ServiceCallbacks;
import org.jugendhackt.camera_warner.Utils.DataProviderManager;

import java.util.List;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

/**
 * This Service is supposed to be running in the background permanently to be able to notify the user of any cctv in their surrounding.
 * All data that is received is broadcasted. If additional data (e.g. list of cameras is needed) the ui can start service. The data will then again be broadcasted from {@link #onStartCommand}
 */
public class LocationService extends Service implements Observer {

    //the interval in which the service wishes to be notified of the users location (expected, min) in ms
    static int INTERVAL = 1000 * 15;
    static int FASTEST_INTERVAL = 1000 * 5;
    private FusedLocationProviderClient mClient;
    //called for the location updates; needed to properly unregister the callback
    private LocationCallback callback;
    //the last received location
    private Location lastLocation;

    //manages all the DataProviders
    private DataProviderManager manager;

    //keeps track whether the ongoing warning notification is shown
    private boolean notificationIsShown;

    //the notification IDs can used to access the notification after it is display
    //to update or cancel it
    //the ongoing notification that warns the user of cameras
    private static final int CAMERA_WARNING_NOTIFICATION_ID = 1243;
    //the ongoing notification that shows the user that the service is running und gives him the ability to control it
    private static final int CAMERA_FOREGROUND_SERVICE_NOTIFICATION_ID = 3421;

    @Override
    public void onCreate() {
        super.onCreate();

        Log.d("LocationService", "onCreate");

        manager = new DataProviderManager();
        manager.addObserver(this);

        mClient = LocationServices.getFusedLocationProviderClient(this);

        //implementation of the location update callback
        //what happens when the service receives the user location is defined here
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                lastLocation = locationResult.getLastLocation();
                Log.d("LocationService", "new location received");
                notifyUIOfNewPosition();

                if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.pref_active_key), getResources().getBoolean(R.bool.pref_active_default))
                        && manager.isCameraNearerThan(Float.parseFloat(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(R.string.pref_radius_key), getString(R.string.pref_radius_default))), lastLocation)) {
                    enableCameraWarning();
                } else {
                    disableCameraWarning();
                }

            }
        };

        //build the notification for @see {@link #startForeground()} to keep the service from being killed
        startForeground(CAMERA_FOREGROUND_SERVICE_NOTIFICATION_ID, buildForegroundNotification());
    }

    private Notification buildForegroundNotification() {
        Intent startService = new Intent(this, LocationService.class);
        startService.setAction("START");
        PendingIntent startPendingIntent = PendingIntent.getService(this, 0, startService, 0);

        Intent stopService = new Intent(this, LocationService.class);
        stopService.setAction("STOP");
        PendingIntent stopPendingIntent = PendingIntent.getService(this, 0, stopService, 0);

        android.support.v7.app.NotificationCompat.Action stopAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_stop_black_24dp, getString(R.string.notification_foreground_action_stop_label), stopPendingIntent)
                        .build();

        android.support.v7.app.NotificationCompat.Action startAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_play_arrow_black_24dp, getString(R.string.notification_foreground_action_start_label), startPendingIntent)
                        .build();

        return
                new android.support.v7.app.NotificationCompat.Builder(this)
                        .addAction(stopAction)
                        .addAction(startAction)
                        .setSmallIcon(android.R.drawable.ic_dialog_info)
                        .setContentTitle("LocationService")
                        .setAutoCancel(true)
                        .setContentText(getString(R.string.service_foregroundNotification_text))
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(getString(R.string.service_foregroundNotification_bigText)))
                        .build();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d("LocationService", "onStartCommand");

        //handle the actions (e.g. the stop from the foreground notification)
        switch (intent.getAction()) {
            case "STOP":
                Log.d("LocationService", "stoping");
                stopForeground(true);
                stopSelf();
                break;
            default:
                Log.d("LocationService", "starting/continuing");
                break;
        }

        //mark all DataProviders as enabled that are enabled by the user
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> selections = preferences.getStringSet(getString(R.string.data_provider_key), null);
        manager.disableAll();
        if (selections != null) {
            for (String string : selections) {
                Log.d("LocationService", "Initializing: " + string);
                DataProvider provider = null;
                if (string.equals(getString(R.string.data_provider_juvenal_values))) {
                    provider = new JuvenalDataProvider();
                } else if (string.equals(getString(R.string.data_provider_dummy_values))) {
                    provider = new FakeCameraProvider();
                } else if (string.equals(getString(R.string.data_provider_osm_values))) {
                    provider = new OSMDataProvider();
                }
                manager.add(provider, string);
            }
        }
        notifyUIOfNewData();

        //defines what and who often location updates should be received
        LocationRequest request = new LocationRequest()
                .setInterval(INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            //try to initialize lastLocation with the systems last known location until we have a gps position
            mClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (location != null) {
                                lastLocation = location;
                                notifyUIOfNewPosition();
                            }
                        }
                    });

            //actually request the location updates
            mClient.requestLocationUpdates(request, callback, null);
        }

        //we shouldn't be killed because we are started as a foreground service
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d("LocationService", "onDestroy");

        //properly remove the callback
        mClient.removeLocationUpdates(callback);
        manager.deleteObserver(this);

        super.onDestroy();
    }

    /**
     * Notifies the user that is a camera inside the radius of him that he defined.
     * It is checked if the notification is already shown
     */
    private void enableCameraWarning() {
        if (!notificationIsShown) {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                    .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                    .setSmallIcon(R.drawable.ic_warning_black_24dp)
                    //.setLargeIcon()
                    .setOngoing(true)
                    .setContentTitle(this.getString(R.string.notification_camera_warning_title))
                    .setContentText(this.getString(R.string.notification_camera_warning_content))
                    .setStyle(new android.support.v4.app.NotificationCompat.BigTextStyle().bigText(this.getString(R.string.notification_camera_warning_content)))
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setAutoCancel(true)
                    .setContentIntent(contentIntent());

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                builder.setPriority(Notification.PRIORITY_HIGH);
            }

            NotificationManager notificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

        /* CAMERA_WARNING_NOTIFICATION_ID allows you to update or cancel the notification later on */
            notificationManager.notify(CAMERA_WARNING_NOTIFICATION_ID, builder.build());

            notificationIsShown = true;
        }
    }

    /**
     * Removes the notification shown with @see {@link #enableCameraWarning()}
     */
    private void disableCameraWarning() {
        if (notificationIsShown) {
            NotificationManager notificationManager = (NotificationManager)
                    this.getSystemService(Context.NOTIFICATION_SERVICE);

            notificationManager.cancel(CAMERA_WARNING_NOTIFICATION_ID);

            notificationIsShown = false;
        }
    }

    /**
     * Returns a pendingIntent to start the MapsActivity (main activity)
     *
     * @return a pendingIntent to start the MapsActivity
     */
    private PendingIntent contentIntent() {
        Intent startActivityIntent = new Intent(this, MapsActivity.class);
        return PendingIntent.getActivity(
                this,
                CAMERA_WARNING_NOTIFICATION_ID,
                startActivityIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private final IBinder binder = new LocalBinder();
    private ServiceCallbacks serviceCallbacks;

    @Override
    public boolean onUnbind(Intent intent) {
        Log.d("LocationService", "onUnBind");
        return super.onUnbind(intent);
    }

    @Override
    public IBinder onBind(Intent p1) {
        Log.d("LocationService", "onBind");
        return binder;
    }


    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    /**
     * Wrapper to call the callback to the UI
     */
    private void notifyUIOfNewData() {
        if (serviceCallbacks != null) {
            serviceCallbacks.newData();
        }
    }

    /**
     * Wrapper to call the callback to the UI
     */
    private void notifyUIOfNewPosition() {
        if (serviceCallbacks != null && lastLocation != null) {
            serviceCallbacks.positionUpdate();
        }
    }

    @Override
    public void update(Observable o, Object arg) {
        notifyUIOfNewData();
    }

    /**
     * The UI can attach a Callback here to be notified when new Data is available or the location changed.
     * @param serviceCallbacks The ServiceCallback (UI)
     */
    public void setCallback(ServiceCallbacks serviceCallbacks) {
        this.serviceCallbacks = serviceCallbacks;

        if (serviceCallbacks != null) {
            notifyUIOfNewPosition();
            notifyUIOfNewData();
        }
    }

    /**
     * Returns the last received Location for the UI. It will be called after the observer is notified of a change.
     * @return The last received Location
     */
    public Location getLastLocation() {
        return lastLocation;
    }

    /**
     * Returns all enabled DataProviders for the UI. It will be called after the observer was notified of a change.
     * @return All enabled DataProviders for the UI
     */
    public List<DataProvider> getProviders() {
        return manager.getDataProviders();
    }
}
