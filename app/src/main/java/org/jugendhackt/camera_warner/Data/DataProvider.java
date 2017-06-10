package org.jugendhackt.camera_warner.Data;

import org.jugendhackt.camera_warner.Data.Camera;

import java.util.List;

/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

/**
 * This class is a abstraction of a data provider.
 * Every data source that should be used should implement this.
 */
public interface DataProvider {

    /**
     * This Method should return all the cameras the data source has available as a list
     * @return a list of all the available cameras
     */
    public List<Camera> getAllCameras();

    /**
     * Only get the camera that is nearest to the specified location
     * @param latitude the latitude of the location
     * @param longitude the longitude of the location
     * @return the camera nearest to the specified location
     */
    public Camera getNearestCamera(double latitude, double longitude);

    /**
     * Calculates the distance to the nearest camera to the specified location
     * @param latitude the latitude of the location
     * @param longitude the longitude of the location
     * @return the distance to the nearest camera in meters
     */
    public float distanceToNearestCamera(double latitude, double longitude);

    /**
     * Only get the cameras that are in the specified radius from the specified location
     * @param latitude the latitude of the location
     * @param longitude the longitude of the location
     * @param radius the radius in meters in which the cameras will be returned
     * @return the cameras in the radius from the location
     */
    public List<Camera> getCamerasInRange(double latitude, double longitude, int radius);

}
