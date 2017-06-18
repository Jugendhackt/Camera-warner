package org.jugendhackt.camera_warner.Data.Providers;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jugendhackt.camera_warner.Data.Model.Camera;
import org.jugendhackt.camera_warner.Utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Julian Mundhahs on 18.06.2017.
 */

public class OSMDataProvider extends AbstractDataProvider {

    private static final String URL = "http://overpass-api.de/api/interpreter";
    private static final String query = "[out:json][timeout:25];\n\n(area[name=\"Hamburg\"];)->.a; \n\n(node[\"man_made\"=\"surveillance\"](area.a));\nout body;";

    @Override
    protected List<Camera> forceFetch() {
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
}
