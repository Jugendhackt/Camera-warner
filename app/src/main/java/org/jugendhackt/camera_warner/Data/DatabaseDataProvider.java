package org.jugendhackt.camera_warner.Data;

import android.location.Location;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
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
            return parseFromJSONArray(new JSONArray(NetworkUtils.getResponseFromHttpUrl(NetworkUtils.buildUrl())));
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
        float[] result = {};

        float distance = Float.MAX_VALUE;
        Camera nearest = camerasCache.get(0);

        for (Camera camera : camerasCache) {
            Location.distanceBetween(latitude, longitude, camera.getLatitude(), camera.getLongitude(), result);

            if (result[0] < distance) {
                distance = result[0];
                nearest = camera;
            }
        }

        return nearest;

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
