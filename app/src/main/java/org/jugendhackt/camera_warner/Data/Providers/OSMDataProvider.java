package org.jugendhackt.camera_warner.Data.Providers;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jugendhackt.camera_warner.Data.Model.Camera;
import org.jugendhackt.camera_warner.Utils.NetworkUtils;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class OSMDataProvider extends AbstractDataProvider {

    private static final String URL = "http://overpass-api.de/api/interpreter";
    private static final String query = "[out:json][timeout:25];\n\n(area[name=\"Hamburg\"];)->.a; \n\n(node[\"man_made\"=\"surveillance\"](area.a));\nout body;";

    @Override
    protected List<Camera> forceFetch() {
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
}
