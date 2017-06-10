package org.jugendhackt.camera_warner.Data;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jugendhackt.camera_warner.Utils.LocationUtils;
import org.jugendhackt.camera_warner.Utils.NetworkUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

public class DatabaseDataProvider implements DataProvider {

    private static List<Camera> camerasCache;

    private List<Camera> forceFetch() {
        try {
            return parseFromJSONArray(new JSONArray(NetworkUtils.getResponseFromHttpUrl(NetworkUtils.LOCAL_DATABASE_URL)));
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
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

    public List<Camera> parseFromJSONArray(JSONArray array) {
        List<Camera> cameras = new ArrayList<>(array.length());
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
