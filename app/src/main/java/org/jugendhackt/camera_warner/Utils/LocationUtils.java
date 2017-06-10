package org.jugendhackt.camera_warner.Utils;

import android.location.Location;

import org.jugendhackt.camera_warner.Data.Camera;

import java.util.List;

/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

public class LocationUtils {

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

    public static float distanceBetween(double latitude1, double longitude1, double latitude2, double longitude2)
    {
        float[] result = new float[3];

        Location.distanceBetween(latitude1, longitude1, latitude2, longitude2, result);

        return result[0];
    }

}
