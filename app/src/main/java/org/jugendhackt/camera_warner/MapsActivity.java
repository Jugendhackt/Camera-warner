package org.jugendhackt.camera_warner;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.NonNull;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import org.jugendhackt.camera_warner.Data.Camera;
import org.jugendhackt.camera_warner.Data.DataProvider;
import org.jugendhackt.camera_warner.Data.DatabaseDataProvider;
import org.jugendhackt.camera_warner.Data.JuvenalDataProvider;
import org.jugendhackt.camera_warner.Utils.NetworkUtils;

import java.util.List;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback,
        LoaderManager.LoaderCallbacks<List<Camera>> {

    private GoogleMap mMap;
    private FusedLocationProviderClient mClient;
    static String TAG = "MapsActivity";
    private LocationCallback callback;

    private Marker lastMarker;
    static int INTERVAL = 1000 * 30;
    static int FASTEST_INTERVAL = 1000 * 15;

    private static final int SQL_SEARCH_LOADER = 22;

    class IncomingHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 12:
                    Log.d(TAG, "callback");
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    private Messenger myService;
    private boolean bound = false;
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            // cast the IBinder and get MyService instance
            myService = new Messenger(service);
            Log.d(TAG, "conncted");

            try {
                myService.send(Message.obtain(null, 13, this.hashCode(), 0));
            } catch (RemoteException e) {
                e.printStackTrace();
            }

            bound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    };

    private void attachCallback() {
        Intent intent = new Intent(MapsActivity.this, LocationCallback.class);

        bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mClient = LocationServices.getFusedLocationProviderClient(this);

        callback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                Location location = locationResult.getLastLocation();
                updateLocationOnMap(location);
            }
        };
        LocationRequest request = new LocationRequest()
                .setInterval(INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL)
                .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);

        mClient.requestLocationUpdates(request, callback, null);

        /*
         * Initialize the loader
         */
        getSupportLoaderManager().initLoader(SQL_SEARCH_LOADER, null, this);

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
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("http://172.16.107.61/neuekamera.html"));
                startActivity(browserIntent);
        }

        return super.onOptionsItemSelected(item);
    }

    private void addCamera(LatLng location) {
        mMap.addMarker(new MarkerOptions().position(location).title("Eine Kamera"));
    }

    private void addCamera(Camera camera)
    {
        addCamera(new LatLng(camera.getLatitude(), camera.getLongitude()));
    }

    private void updateLocationOnMap(@NonNull Location location) {
        if (lastMarker != null) {
            lastMarker.remove();
        }
        Log.d(TAG, String.valueOf(location.getAccuracy()));
        Log.d(TAG, String.valueOf(location.getLatitude()));
        Log.d(TAG, String.valueOf(location.getLongitude()));
        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15));

        lastMarker = mMap.addMarker(new MarkerOptions().position(userLocation).title("Your location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
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

        mClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            updateLocationOnMap(location);
                        }
                    }
                });
    }

    @Override
    public Loader<List<Camera>> onCreateLoader(int id, Bundle args) {
        return new AsyncTaskLoader<List<Camera>>(this) {
            List<Camera> queryResult;

            @Override
            public void deliverResult(List<Camera> data) {
                queryResult = data;
                super.deliverResult(data);
            }

            @Override
            protected void onStartLoading() {
                if (queryResult != null) {
                    deliverResult(queryResult);
                } else {
                    forceLoad();
                }
            }

            @Override
            public List<Camera> loadInBackground() {
                //DataProvider provider = new DatabaseDataProvider();
                DataProvider provider = new JuvenalDataProvider();

                return provider.getAllCameras();
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<List<Camera>> loader, List<Camera> data) {
        if (null == data) {
            showErrorMessage();
        } else {
            addData(data);
        }

    }

    private void addData(List<Camera> data) {
        for (Camera camera : data) {
            addCamera(camera);
        }

    }

    private void showErrorMessage() {
        Toast.makeText(this, "Failed to fetch data", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onLoaderReset(Loader<List<Camera>> loader) {

    }
}
