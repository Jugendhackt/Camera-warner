package org.jugendhackt.camera_warner.Data;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jugendhackt.camera_warner.Utils.LocationUtils;
import org.jugendhackt.camera_warner.Utils.NetworkUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

/**
 * This DataProvider provides the data that is also available on juvenal.org.
 * It is blocking.
 */
public class JuvenalDataProvider implements DataProvider {

    //the url where this database is located at
    public final static String URL = "http://www.juvenal.org/api/cameras";

    //to avoid having to fetch the data every time
    private static List<Camera> camerasCache = new LinkedList<>();

    /**
     * Actually loads data from the data source
     * @return the data that has been loaded
     */
    private List<Camera> forceFetch() {
        JSONArray result = new JSONArray();
        try {
            result = new JSONObject(NetworkUtils.getResponseFromJuvenal()).getJSONArray("features");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List<Camera> cameras = new ArrayList<>(result.length());
        for (int i = 0; i < result.length(); i++) {
            JSONArray coordinates = null;
            try {
                coordinates = result.getJSONObject(i).getJSONObject("geometry").getJSONArray("coordinates");
                Camera camera = new Camera(coordinates.getDouble(1), coordinates.getDouble(0));
                cameras.add(camera);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return cameras;
    }

    @Override
    public boolean hasData() {
        return !camerasCache.isEmpty();
    }

    @Override
    public List<Camera> getAllCameras() {
        if (camerasCache.isEmpty()) {
            camerasCache = forceFetch();
        }
        return camerasCache;

    }

    @Override
    public Camera getNearestCamera(Location location) {
        if (camerasCache.isEmpty()) {
            camerasCache = forceFetch();
        }
        return LocationUtils.getNearestTo(location, camerasCache);
    }

    @Override
    public float distanceToNearestCamera(Location location) {
        Camera nearestCamera = getNearestCamera(location);
        return LocationUtils.distanceBetween(location, nearestCamera);
    }

    @Override
    public List<Camera> getCamerasInRange(double latitude, double longitude, int radius) {
        return null;
    }
}
