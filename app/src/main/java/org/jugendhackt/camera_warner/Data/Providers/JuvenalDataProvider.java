package org.jugendhackt.camera_warner.Data.Providers;

import android.location.Location;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jugendhackt.camera_warner.Data.Model.Camera;
import org.jugendhackt.camera_warner.Utils.NetworkUtils;

import java.util.LinkedHashSet;
import java.util.Set;

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
    protected Set<Camera> loadData(Location location) {
        JSONArray result = new JSONArray();
        String resultStr = NetworkUtils.getResponseFromURL(URL, NetworkUtils.GeoJSON);

        if(resultStr == null || resultStr.length() == 0)
        {
            Log.d("JuvenalDataProvider", "fetch failed");
            return new LinkedHashSet<>();
        }

        try {
            result = new JSONObject(resultStr).getJSONArray("features");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Set<Camera> cameras = new LinkedHashSet<>(result.length());
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
    public Set<Camera> updateLocation(Location newLocation) {
        if(camerasCache.isEmpty())
        {
            camerasCache.addAll(loadData(newLocation));
        }
        return camerasCache;
    }
}
