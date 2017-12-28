package org.jugendhackt.camera_warner.Data.Providers;

import android.location.Location;

import org.jugendhackt.camera_warner.Data.Model.Camera;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A DataProvider that returns the Data given in the constructor. It is intended for testing.
 */
public class TestingDataProvider extends AbstractDataProvider {

    private Set<Camera> tempStorage;

    public TestingDataProvider(Set<Camera> data)
    {
        tempStorage = data;
    }

    public TestingDataProvider(Camera[] data)
    {
        tempStorage = new LinkedHashSet<>();
        tempStorage.addAll(Arrays.asList(data));
    }

    @Override
    protected Set<Camera> loadData(Location location) {
        return tempStorage;
    }
}
