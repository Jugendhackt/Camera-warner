package org.jugendhackt.camera_warner.Data.Providers;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jugendhackt.camera_warner.Data.Model.Camera;
import org.jugendhackt.camera_warner.Utils.LocationUtils;
import org.jugendhackt.camera_warner.Utils.NetworkUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Julian Mundhahs on 18.06.2017.
 */

public class OSMProvider implements DataProvider {

    //the url where this database is located at
    private static final String URL = "http://overpass-api.de/api/interpreter";

    private static final String query = "[out:json][timeout:25];\n\n(area[name=\"Hamburg\"];)->.a; \n\n(node[\"man_made\"=\"surveillance\"](area.a));\nout body;";

    //to avoid having to fetch the data every time
    private static List<Camera> camerasCache = new LinkedList<>();

    private List<Camera> forceFetch() {
        JSONArray result = new JSONArray();
        try {
            result = new JSONObject(NetworkUtils.getResponseWithPost(URL, NetworkUtils.OverpassQL, query)).getJSONArray("elements");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List<Camera> cameras = new ArrayList<>(result.length());
        for (int i = 0; i < result.length(); i++) {
            JSONObject coordinates = null;
            try {
                coordinates = result.getJSONObject(i);
                Camera camera = new Camera(coordinates.getDouble("lat"), coordinates.getDouble("lon"));
                cameras.add(camera);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return cameras;
    }

    @Override
    public void fetchData() {
        camerasCache = forceFetch();
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
