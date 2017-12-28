package org.jugendhackt.camera_warner.Data.Providers;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jugendhackt.camera_warner.Data.Model.Camera;
import org.jugendhackt.camera_warner.Utils.NetworkUtils;

import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * This class provides data from local MySQL Database that is accessible via a php script.
 * It therefore is blocking.
 */
public class DatabaseDataProvider extends AbstractDataProvider {


    //the url where this database is located at
    public final static String URL = "http://172.16.107.61/cameras.php";

    //to avoid having to fetch the data every time
    private static Set<Camera> camerasCache = new LinkedHashSet<>();

    @Override
    public Set<Camera> updateLocation(Location newLocation) {
        if(camerasCache.isEmpty()) camerasCache = loadData(newLocation);
        return camerasCache;
    }

    /**
     * Actually loads data from the data source
     * @return the data that has been loaded
     */
    @Override
    protected Set<Camera> loadData(Location location) {
        try {
            return parseFromJSONArray(new JSONArray(NetworkUtils.getResponseFromHttpUrl(URL)));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Parses the format specific to this database's output and return it as List of Cameras
     * @param array the json array that contains the different camera object
     * @return the list of cameras
     */
    private Set<Camera> parseFromJSONArray(JSONArray array) {
        Set<Camera> cameras = new LinkedHashSet<>(array.length());
        for (int i = 0; i < array.length(); i++) {
            try {
                JSONObject dieseCamera = array.getJSONObject(i);
                Camera camera = new Camera(dieseCamera.getDouble("latitude"), dieseCamera.getDouble("longitude"));
                cameras.add(camera);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return cameras;
    }
}
