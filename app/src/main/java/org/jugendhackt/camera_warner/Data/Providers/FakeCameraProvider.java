package org.jugendhackt.camera_warner.Data.Providers;

import android.location.Location;

import org.jugendhackt.camera_warner.Data.Model.Camera;

import java.util.LinkedHashSet;
import java.util.Set;


/**
 * This is just a DataProvider that provides *fake* and constant data. It should be used for test purposes only!
 * It therefore is not blocking.
 */
public class FakeCameraProvider extends AbstractDataProvider {

    @Override
    protected Set<Camera> loadData(Location location) {
        Set<Camera> cameras = new LinkedHashSet<>();
        cameras.add(new Camera(-31, 142));
        cameras.add(new Camera(53.563, 9.971));
        cameras.add(new Camera(0, 0));
        return cameras;
    }
}
