package org.jugendhackt.camera_warner;


import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jugendhackt.camera_warner.Data.Camera;
import org.jugendhackt.camera_warner.Data.DataProvider;
import org.jugendhackt.camera_warner.Data.ServiceCallbacks;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, ServiceCallbacks {

    //TODO: add (proper) documentation

    private GoogleMap mMap;
    static String TAG = "MapsActivity";

    private Marker locationMarker;
    private Circle radiusCircle;

    private BroadcastReceiver receiver;
    private BroadcastReceiver receiver1;

    private boolean followed;

    private LocationService myService;
    private boolean bound = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            LocationService.LocalBinder binder = (LocationService.LocalBinder) service;
            myService = binder.getService();
            bound = true;
            myService.setCallback(MapsActivity.this); // register
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
            myService = null;
        }
    };

    @Override
    protected void onDestroy() {
        unregisterReceiver(receiver);
        unregisterReceiver(receiver1);
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(getResources().getString(R.string.broadcast_camera));
        receiver = new android.content.BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "received Cameras");
                addData(intent.<Camera>getParcelableArrayListExtra("list"));
            }
        };
        this.registerReceiver(receiver, filter);

        IntentFilter filter1 = new IntentFilter();
        filter1.addAction(getResources().getString(R.string.broadcast_location));
        receiver1 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "recived Location");
                Location location = new Location("");
                location.setLatitude(intent.getDoubleExtra("latitude", 0));
                location.setLongitude(intent.getDoubleExtra("longitude", 0));
                updateLocationOnMap(location);
            }
        };
        this.registerReceiver(receiver1, filter1);

        Intent intent = new Intent(this, LocationService.class);
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.map_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemID = item.getItemId();

        switch (itemID) {
            case R.id.action_settings:
                Intent mapsActivity = new Intent(this, SettingsActivity.class);
                startActivity(mapsActivity);
                return true;
            case R.id.action_add:
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://172.16.107.61/neuekamera.html"));
                startActivity(browserIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void addCamera(LatLng location) {
        mMap.addMarker(new MarkerOptions().position(location).title("Eine Kamera").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
    }

    private void addCamera(Camera camera) {
        addCamera(new LatLng(camera.getLatitude(), camera.getLongitude()));
    }

    private void updateLocationOnMap(@NonNull Location location) {
        if (locationMarker != null) {
            locationMarker.remove();
        }
        Log.d(TAG, String.valueOf(location.getAccuracy()));
        Log.d(TAG, String.valueOf(location.getLatitude()));
        Log.d(TAG, String.valueOf(location.getLongitude()));
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        if (!followed) {
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));
            followed = true;
        }

        locationMarker = mMap.addMarker(new MarkerOptions().position(userLocation).title("Your location"));

        if (radiusCircle != null) {
            radiusCircle.remove();
        }
        radiusCircle = mMap.addCircle(new CircleOptions().center(locationMarker.getPosition()).radius(Double.parseDouble(PreferenceManager.getDefaultSharedPreferences(this).getString(getString(R.string.pref_radius_key), getString(R.string.pref_radius_default)))).fillColor(Color.argb(195, 102, 147, 173)));
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
    }

    private void addData(List<Camera> data) {
        for (Camera camera : data) {
            addCamera(camera);
        }
    }

    private void attachCallback() {
        bindService(new Intent(this, LocationService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void newData() {
        for(DataProvider provider : myService.providers)
        {
            addData(provider.getAllCameras());
        }
    }

    @Override
    public void postionUpdate() {
        updateLocationOnMap(myService.lastLocation);
    }
}
