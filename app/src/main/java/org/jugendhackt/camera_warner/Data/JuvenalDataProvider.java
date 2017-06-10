package org.jugendhackt.camera_warner.Data;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jugendhackt.camera_warner.Utils.LocationUtils;
import org.jugendhackt.camera_warner.Utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

public class JuvenalDataProvider implements DataProvider {

    //TODO: add (proper) documentation

    private static List<Camera> camerasCache;

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
    public List<Camera> getAllCameras() {
        if (camerasCache == null) {
            camerasCache = forceFetch();
        }
        return camerasCache;

    }

    @Override
    public Camera getNearestCamera(double latitude, double longitude) {
        if (camerasCache == null) {
            camerasCache = forceFetch();
        }
        return LocationUtils.getNearestTo(latitude, longitude, camerasCache);
    }

    @Override
    public float distanceToNearestCamera(double latitude, double longitude) {
        Camera nearestCamera = getNearestCamera(latitude, longitude);
        return LocationUtils.distanceBetween(latitude, longitude, nearestCamera.getLatitude(), nearestCamera.getLongitude());
    }

    @Override
    public List<Camera> getCamerasInRange(double latitude, double longitude, int radius) {
        return null;
    }
}
