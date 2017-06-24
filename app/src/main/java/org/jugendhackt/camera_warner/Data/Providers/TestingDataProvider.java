package org.jugendhackt.camera_warner.Data.Providers;

import org.jugendhackt.camera_warner.Data.Model.Camera;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Julian Mundhahs on 22.06.2017.
 */

public class TestingDataProvider extends AbstractDataProvider {

    private List<Camera> tempStorage;

    public TestingDataProvider(List<Camera> data)
    {
        tempStorage = data;
    }

    public TestingDataProvider(Camera[] data)
    {
        tempStorage = new LinkedList<>();
        for(Camera camera : data)
        {
            tempStorage.add(camera);
        }
    }

    @Override
    protected List<Camera> forceFetch() {
        return tempStorage;
    }
}
