package org.jugendhackt.camera_warner.Utils;

import android.location.Location;

import org.jugendhackt.camera_warner.Data.Model.Camera;

import java.util.List;

/**
 * This class provides some wrappers for functions concerning the comparison of two locations
 */
public class LocationUtils {


    /**
     * This method provides the Camera out of the list that is nearest to the given location
     * @param latitude latitude of the location
     * @param longitude longitude of the location
     * @param cameras a list of cameras out of which the nearest will be returned
     * @return the camera nearest to the location
     */
    public static Camera getNearestTo(double latitude, double longitude, List<Camera> cameras)
    {
        float[] result = new float[3];

        float distance = Float.MAX_VALUE;
        Camera nearest = cameras.get(0);

        for (Camera camera : cameras) {
            Location.distanceBetween(latitude, longitude, camera.getLatitude(), camera.getLongitude(), result);

            if (result[0] < distance) {
                distance = result[0];
                nearest = camera;
            }
        }

        return nearest;
    }

    /**
     * Wrapper  for {@link #getNearestTo(double, double, List)}
     * @param location the location to which the nearest camera will be returned
     * @param cameras a list of cameras out which the nearest will be returned
     * @return the nearst camera
     */
    public static Camera getNearestTo(Location location, List<Camera> cameras)
    {
        return getNearestTo(location.getLatitude(), location.getLongitude(), cameras);
    }

    /**
     * Returns the distance between two locations in m
     * @param latitude1 latitude of the 1st location
     * @param longitude1 longitude of the 1st location
     * @param latitude2 latitude of the 2nd location
     * @param longitude2 longitude of the 2nd location
     * @return the distance between the locations in m
     */
    public static float distanceBetween(double latitude1, double longitude1, double latitude2, double longitude2)
    {
        float[] result = new float[3];

        Location.distanceBetween(latitude1, longitude1, latitude2, longitude2, result);

        return result[0];
    }

    /**
     * A wrapper for {@link #distanceBetween(double, double, double, double)}
     * @param latitude the latitude of the 1st location
     * @param lonitude the longitude of the 1st location
     * @param camera the camera (2nd location)
     * @return the distance between the 1st location and the camera
     */
    public static float distanceBetween(double latitude, double lonitude, Camera camera)
    {
        return distanceBetween(latitude, lonitude, camera.getLatitude(), camera.getLongitude());
    }

    /**
     * A wrapper for {@link #distanceBetween(double, double, double, double)}
     * @param location the 1st location
     * @param latitude2 the latitude of the 2nd location
     * @param longitude2 the longitude of the 2nd location
     * @return the distance beteen the 1st and 2nd location
     */
    public static float distanceBetween(Location location, double latitude2, double longitude2)
    {
        return distanceBetween(location.getLatitude(), location.getLongitude(), latitude2 ,longitude2);
    }

    /**
     * A wrapper for {@link #getNearestTo(double, double, List)}
     * @param location the 1st location
     * @param camera the camera (2nd location)
     * @return the distance between the 1st location and the camera
     */
    public static float distanceBetween(Location location, Camera camera)
    {
        return distanceBetween(location.getLatitude(), location.getLongitude(), camera.getLatitude(), camera.getLongitude());
    }
}
