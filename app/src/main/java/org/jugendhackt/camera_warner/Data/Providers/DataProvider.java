package org.jugendhackt.camera_warner.Data.Providers;

import android.location.Location;

import org.jugendhackt.camera_warner.Data.Model.Camera;

import java.util.Set;

/**
 * This class is a abstraction of a data provider.
 * Every data source that should be used should implement this.
 */
public interface DataProvider {

    /**
     * Check if data has been cached
     * @return if data is cached
     */
    public boolean hasData();

    /**
     * This Method should return all the cameras the data source has available as a Set
     * @return a Set of all the available cameras
     */
    public Set<Camera> getAllCameras();

    /**
     * Only get the camera that is nearest to the specified location
     * @param location the location relative which the camera is looked up
     * @return the camera nearest to the specified location
     */
    public Camera getNearestCamera(Location location);

    /**
     * Calculates the distance to the nearest camera to the specified location
     * @param location the location relative to which the distance to the nearest camera is returned
     * @return the distance to the nearest camera in meters
     */
    public float distanceToNearestCamera(Location location);

    /**
     * Only get the cameras that are in the specified radius from the specified location
     * @param latitude the latitude of the location
     * @param longitude the longitude of the location
     * @param radius the radius in meters in which the cameras will be returned
     * @return the cameras in the radius from the location
     */
    public Set<Camera> getCamerasInRange(double latitude, double longitude, int radius);

    /**
     * Notifies the DataProvider that the users location has changed.
     * This is important because the it may need the location to know for which location Data should be loaded
     * and also if the user moves out of the previous search location.
     * @param newLocation the new location of the user
     */
    public Set<Camera> updateLocation(Location newLocation);

}
