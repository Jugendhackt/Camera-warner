package org.jugendhackt.camera_warner.Data.Providers;

import android.location.Location;

import org.jugendhackt.camera_warner.Data.Model.Camera;
import org.jugendhackt.camera_warner.Utils.LocationUtils;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Provides a basic frame for coding a DataProvider. Only @see forceFetch which loads the Data synchronously has to be implemented.
 */
public abstract class AbstractDataProvider implements DataProvider {

    //to avoid having to fetch the data every time
    Set<Camera> camerasCache = new LinkedHashSet<>();

    protected abstract Set<Camera> loadData(Location location);

    @Override
    public boolean hasData() {
        return !camerasCache.isEmpty();
    }

    @Override
    public Set<Camera> getAllCameras() {
        return camerasCache;

    }

    @Override
    public Camera getNearestCamera(Location location) {
        return LocationUtils.getNearestTo(location, camerasCache);
    }

    @Override
    public float distanceToNearestCamera(Location location) {
        Camera nearestCamera = getNearestCamera(location);
        return LocationUtils.distanceBetween(location, nearestCamera);
    }

    @Override
    public Set<Camera> getCamerasInRange(double latitude, double longitude, int radius) {
        return null;
    }

    @Override
    public Set<Camera> updateLocation(Location newLocation)
    {
        Set<Camera> cameras = loadData(newLocation);
        camerasCache.addAll(cameras);
        return cameras;
    }
}
