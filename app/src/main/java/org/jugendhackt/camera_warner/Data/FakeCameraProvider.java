package org.jugendhackt.camera_warner.Data;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

public class FakeCameraProvider implements DataProvider{

    //TODO: add (proper) documentation

    private Camera[] cameras = {new Camera(-31, 142), new Camera(53.563, 9.971), new Camera(0, 0)};

    @Override
    public List<Camera> getAllCameras() {
        return new LinkedList<>(Arrays.asList(cameras));
    }

    @Override
    public Camera getNearestCamera(double latitude, double longitude) {
        return cameras[1];
    }

    @Override
    public float distanceToNearestCamera(double latitude, double longitude) {
        return 42;
    }

    @Override
    public List<Camera> getCamerasInRange(double latitude, double longitude, int radius) {
        return new LinkedList<>(Arrays.asList(Arrays.copyOfRange(cameras, 0, 1)));
    }
}
