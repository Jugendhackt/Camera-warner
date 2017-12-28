package org.jugendhackt.camera_warner.Data.Providers;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jugendhackt.camera_warner.Data.Model.Camera;
import org.jugendhackt.camera_warner.Utils.NetworkUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

public class OSMDataProvider extends AbstractDataProvider {

    private static final String URL = "http://overpass-api.de/api/interpreter";
    private static final String staticQuery = "[out:json][timeout:25];\n\n(area[name=\"Hamburg\"];)->.a; \n\n(node[\"man_made\"=\"surveillance\"](area.a));\nout body;";
    private static final String queryTemplate = "[out:json][timeout:25];\n\n(node[\"man_made\"=\"surveillance\"](%f, %f, %f, %f));\nout body;";

    @Override
    protected List<Camera> forceFetch() {
        return executeAndFetchFromQuery(staticQuery);
    }

    private List<Camera> executeAndFetchFromQuery(String query)
    {
        JSONArray result = new JSONArray();
        String resultStr = NetworkUtils.getResponseWithPost(URL, NetworkUtils.OverpassQL, query);

        if(resultStr == null || resultStr.length() == 0)
        {
            Log.d("OSMDataProvider", "fetch failed");
            return new LinkedList<>();
        }

        try {
            result = new JSONObject(resultStr).getJSONArray("elements");
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

    protected List<Camera> fetchForLocation(Location location)
    {
        return executeAndFetchFromQuery(formatQuery(location));
    }

    private String formatQuery(Double x1, Double y1, Double x2, Double y2)
    {
        return String.format(Locale.US, queryTemplate, x1, y1, x2, y2);
    }

    private String formatQuery(Location loc1, Location loc2)
    {
        return formatQuery(loc1.getAltitude(), loc1.getLatitude(), loc2.getAltitude(), loc2.getLatitude());
    }

    private String formatQuery(Location location)
    {
        return formatQuery(location.getAltitude()-0.05, location.getLatitude()-0.5, location.getAltitude()+0.05, location.getLatitude()+0.5);
    }
}
