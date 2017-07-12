package org.jugendhackt.camera_warner;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
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
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import org.jugendhackt.camera_warner.Data.Model.Camera;
import org.jugendhackt.camera_warner.Data.Providers.DataProvider;
import org.jugendhackt.camera_warner.Preferences.SettingsActivity;
import org.jugendhackt.camera_warner.Services.LocationService;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, ServiceCallbacks {

    private GoogleMap mMap;

    private Marker locationMarker;
    private Circle radiusCircle;

    private boolean followed;

    private ClusterManager<Camera> myClusterManager;

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

    private void setupCluster()
    {
        myClusterManager = new ClusterManager<>(this, mMap);
        myClusterManager.setRenderer(new MyCustomRender(this, mMap, myClusterManager));

        mMap.setOnCameraIdleListener(myClusterManager);
        mMap.setOnMarkerClickListener(myClusterManager);
    }

    @Override
    protected void onStop() {
        super.onStop();

        if(bound)
        {
            myService.setCallback(null);
            unbindService(serviceConnection);
            bound = false;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        Intent startService = new Intent(this, LocationService.class);
        startService.setAction("START");
        startService(startService);
    }

    @Override
    protected void onStart() {
        super.onStart();

        attachCallback();
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
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://juvenal.org"));
                startActivity(browserIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void attachCallback() {
        bindService(new Intent(this, LocationService.class), serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private void updateLocationOnMap(@NonNull Location location) {
        if (locationMarker != null) {
            locationMarker.remove();
        }

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
        setupCluster();
    }

    private class MyCustomRender extends DefaultClusterRenderer<Camera> {


        public MyCustomRender(Context context, GoogleMap map, ClusterManager<Camera> clusterManager) {
            super(context, map, clusterManager);
        }

        @Override
        protected void onBeforeClusterItemRendered(Camera item, MarkerOptions markerOptions) {
            markerOptions.title("Eine Kamera").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
        }
    }

    private void addData(List<Camera> data) {
        myClusterManager.addItems(data);
    }

    private void clearItems()
    {
        if(myClusterManager != null)
        {
            myClusterManager.clearItems();
            mMap.clear();
            myClusterManager.cluster();
        }
    }


    @Override
    public void newData() {
        clearItems();

        for(DataProvider provider : myService.getProviders())
        {
            addData(provider.getAllCameras());
        }
    }

    @Override
    public void positionUpdate() {
        updateLocationOnMap(myService.getLastLocation());
    }
}
