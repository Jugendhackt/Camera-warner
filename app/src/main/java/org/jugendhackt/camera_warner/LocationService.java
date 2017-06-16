package org.jugendhackt.camera_warner;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
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

import org.jugendhackt.camera_warner.Data.Camera;
import org.jugendhackt.camera_warner.Data.DataProvider;
import org.jugendhackt.camera_warner.Data.DatabaseDataProvider;
import org.jugendhackt.camera_warner.Data.FakeCameraProvider;
import org.jugendhackt.camera_warner.Data.JuvenalDataProvider;
import org.jugendhackt.camera_warner.Data.ServiceCallbacks;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

/**
 * This Service is supposed to be running in the background permanently to be able to notify the user of any cctv in their surrounding.
 * All data that is received is broadcasted. If additional data (e.g. list of cameras is needed) the ui can start service. The data will then again be broadcasted from {@link #onStartCommand}
 */
public class LocationService extends Service {

    private class FillDataProvider extends AsyncTask<DataProvider, Void, DataProvider>
    {

        @Override
        protected DataProvider doInBackground(DataProvider... params) {
            DataProvider provider = params[0];

            provider.fetchData();
            return provider;
        }

        @Override
        protected void onPostExecute(DataProvider provider) {
            super.onPostExecute(provider);

            providers.add(provider);

            serviceCallbacks.newData();
            //sendAllCamerasCacheToActivity(provider);
        }
    }

    //logging
    public static final String TAG = "LocationService";

    //the interval in which the service wishes to be notified of the users location (expected, min)
    static int INTERVAL = 1000 * 15;
    static int FASTEST_INTERVAL = 1000 * 5;
    private FusedLocationProviderClient mClient;
    //called for the location updates; needed to properly unregister the callback
    private LocationCallback callback;
    //the last received location
    public Location lastLocation;

    //the data provide from which we will get our data
    //private DataProvider provider;
    public List<DataProvider> providers = new LinkedList<>();

    //This notification ID can be used to access our notification after we've displayed it. This
    //can be handy when we need to cancel the notification, or perhaps update it. This number is
    //arbitrary and can be set to whatever you like.
    private static final int CAMERA_WARNING_NOTIFICATION_ID = 1243;

    @Override
    public void onCreate() {
        super.onCreate();

        mClient = LocationServices.getFusedLocationProviderClient(this);

        //implementation of the location update callback
        //what happens when the service receives the user location is defined here
        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Log.d("LocationService", "gotLocation");
                lastLocation = locationResult.getLastLocation();

                for(DataProvider thisProvider : providers)
                {
                    if (thisProvider.hasData()) {
                        //sendAllCamerasCacheToActivity(thisProvider);
                        serviceCallbacks.newData();

                        if (thisProvider.distanceToNearestCamera(lastLocation) < Float.parseFloat(PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString(getString(R.string.pref_radius_key), getString(R.string.pref_radius_default)))
                                && PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.pref_active_key), getResources().getBoolean(R.bool.pref_active_default))) {
                            sendCameraWarning();
                            Log.d(TAG, "a camera is to near");
                        }
                    }
                }

            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        Set<String> selections = preferences.getStringSet(getString(R.string.data_provider_key), null);
        for (String string : selections) {
            DataProvider provider = null;
            if (string.equals(getString(R.string.data_provider_juvenal_values))) {
                provider = new JuvenalDataProvider();
            } else if (string.equals(getString(R.string.data_provider_dummy_values))) {
                provider = new FakeCameraProvider();
            } else if (string.equals(getString(R.string.data_provider_custom_values))) {
                provider = new DatabaseDataProvider();
            }
            providers.add(provider);
            Log.d(TAG, string);
        }

        //try to initialize lastLocation with the systems last known location until we have a gps position
        mClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            lastLocation = location;
                            Log.d(TAG, "sending last location");
                            serviceCallbacks.postionUpdate();
                            //sendLastLocationToActivity();
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

        for(DataProvider provider : providers)
        {
            new FillDataProvider().execute(provider);
        }

        Log.d(TAG, "started Service");

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }

    /**
     * Notifies the user that is a camera inside the radius of him that he defined
     */
    private void sendCameraWarning() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setColor(ContextCompat.getColor(this, R.color.colorPrimary))
                .setSmallIcon(R.drawable.ic_warning_black_24dp)
                //.setLargeIcon()
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

    @Override
    public void onDestroy() {
        //properly remove the callback
        mClient.removeLocationUpdates(callback);

        super.onDestroy();
    }

    private final IBinder binder = new LocalBinder();
    private ServiceCallbacks serviceCallbacks;

    @Override
    public IBinder onBind(Intent p1) {
        return binder;
    }

    public class LocalBinder extends Binder {
        public LocationService getService() {
            return LocationService.this;
        }
    }

    public void setCallback(ServiceCallbacks serviceCallbacks) {
        this.serviceCallbacks = serviceCallbacks;
    }
}
