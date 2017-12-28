package org.jugendhackt.camera_warner.Data.Providers;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jugendhackt.camera_warner.Data.Model.Camera;
import org.jugendhackt.camera_warner.Utils.NetworkUtils;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

public class OSMDataProvider extends AbstractDataProvider {

    private static final String URL = "http://overpass-api.de/api/interpreter";
    private static final String queryTemplate = "[out:json][timeout:25];\n\n(node[\"man_made\"=\"surveillance\"](%2.2f, %2.2f, %2.2f, %2.2f));\nout body;";

    @Override
    public Set<Camera> loadData(Location newLocation) {
        if(newLocation==null)
        {
            Log.d("OSMDataProvider", "location is null. ignoring");
            return new LinkedHashSet<>();
        }

        Log.d("OSMDataProvider", String.format("Fetching data for Latitude=%f, Longitude=%f", newLocation.getLatitude(), newLocation.getLongitude()));
        return fetchForLocation(newLocation);
    }

    private Set<Camera> executeAndFetchFromQuery(String query)
    {
        JSONArray result = new JSONArray();
        String resultStr = NetworkUtils.getResponseWithPost(URL, NetworkUtils.OverpassQL, query);

        if(resultStr == null || resultStr.length() == 0)
        {
            Log.d("OSMDataProvider", "fetch failed");
            return new LinkedHashSet<>();
        }

        try {
            result = new JSONObject(resultStr).getJSONArray("elements");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Set<Camera> cameras = new LinkedHashSet<>(result.length());
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

    protected Set<Camera> fetchForLocation(Location location)
    {
        return executeAndFetchFromQuery(formatQuery(location));
    }

    private String formatQuery(Double x1, Double y1, Double x2, Double y2)
    {
        return String.format(Locale.US, queryTemplate, x1, y1, x2, y2);
    }

    private String formatQuery(Location location)
    {
        return formatQuery(location.getLatitude()-0.05, location.getLongitude()-0.05, location.getLatitude()+0.05, location.getLongitude()+0.05);
    }
}
