package org.jugendhackt.camera_warner.Utils;

import org.jugendhackt.camera_warner.Data.Camera;

import java.util.List;

/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

public interface DataProvider {

    List<Camera> getAllCameras();

    List<Camera> getNearestCamera(long latitude, long longitude);

    List<Camera> getCamerasInRange(long latitude, long longitude, int radius);

}
