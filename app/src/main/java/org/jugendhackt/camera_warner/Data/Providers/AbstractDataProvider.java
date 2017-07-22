package org.jugendhackt.camera_warner.Data.Providers;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jugendhackt.camera_warner.Data.Model.Camera;
import org.jugendhackt.camera_warner.Utils.LocationUtils;
import org.jugendhackt.camera_warner.Utils.NetworkUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides a basic frame for coding a DataProvider. Only @see forceFetch which loads the Data synchronously has to be implemented.
 */
public abstract class AbstractDataProvider implements DataProvider {

    //to avoid having to fetch the data every time
    private List<Camera> camerasCache = new LinkedList<>();

    /**
     * Actually loads data from the data source. This is done synchronously
     * @return the data that has been loaded
     */
    protected abstract List<Camera> forceFetch();

    @Override
    public void fetchData() {
        if (camerasCache.isEmpty()) {
            camerasCache = forceFetch();
        }
    }

    @Override
    public boolean hasData() {
        return !camerasCache.isEmpty();
    }

    @Override
    public List<Camera> getAllCameras() {
        if (camerasCache.isEmpty()) {
            camerasCache = forceFetch();
        }
        return camerasCache;

    }

    @Override
    public Camera getNearestCamera(Location location) {
        if (camerasCache.isEmpty()) {
            camerasCache = forceFetch();
        }
        return LocationUtils.getNearestTo(location, camerasCache);
    }

    @Override
    public float distanceToNearestCamera(Location location) {
        Camera nearestCamera = getNearestCamera(location);
        return LocationUtils.distanceBetween(location, nearestCamera);
    }

    @Override
    public List<Camera> getCamerasInRange(double latitude, double longitude, int radius) {
        return null;
    }
}
