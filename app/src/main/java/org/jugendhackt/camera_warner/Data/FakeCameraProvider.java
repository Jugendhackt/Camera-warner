package org.jugendhackt.camera_warner.Data;

import android.location.Location;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

/**
 * This is just a DataProvider that provides *fake* and constant data. It should be used for test purposes only!
 * It therefore is not blocking.
 */
public class FakeCameraProvider implements DataProvider{

    //a small array with some fake data
    private Camera[] cameras = {new Camera(-31, 142), new Camera(53.563, 9.971), new Camera(0, 0)};

    @Override
    public boolean hasData() {
        return true;
    }

    @Override
    public List<Camera> getAllCameras() {
        return new LinkedList<>(Arrays.asList(cameras));
    }

    @Override
    public Camera getNearestCamera(Location location) {
        return cameras[1];
    }

    @Override
    public float distanceToNearestCamera(Location location) {
        return 42;
    }

    @Override
    public List<Camera> getCamerasInRange(double latitude, double longitude, int radius) {
        return new LinkedList<>(Arrays.asList(Arrays.copyOfRange(cameras, 0, 1)));
    }
}
