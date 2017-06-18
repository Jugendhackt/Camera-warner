package org.jugendhackt.camera_warner.Data.Providers;

import android.location.Location;

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
 * Created by Julian Mundhahs on 10.06.2017.
 */

/**
 * This DataProvider provides the data that is also available on juvenal.org.
 * It is blocking.
 */
public class JuvenalDataProvider extends AbstractDataProvider{

    private static final String URL = "http://www.juvenal.org/api/cameras";

    /**
     * Actually loads data from the data source
     * @return the data that has been loaded
     */
    @Override
    protected List<Camera> forceFetch() {
        JSONArray result = new JSONArray();
        try {
            result = new JSONObject(NetworkUtils.getResponseFromURL(URL, NetworkUtils.GeoJSON)).getJSONArray("features");
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
}
