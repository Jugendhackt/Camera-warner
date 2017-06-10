package org.jugendhackt.camera_warner.Utils;

import org.jugendhackt.camera_warner.Data.Camera;

import java.util.List;

/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

public interface DataProvider {

    public List<Camera> getAllCameras();

    public Camera getNearestCamera(double latitude, double longitude);

    public List<Camera> getCamerasInRange(double latitude, double longitude, int radius);

}
