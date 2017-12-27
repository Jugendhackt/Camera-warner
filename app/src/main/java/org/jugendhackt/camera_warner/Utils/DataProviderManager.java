package org.jugendhackt.camera_warner.Utils;

import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import org.jugendhackt.camera_warner.Data.Providers.DataProvider;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;

/**
 * This class manages all dataproviders. It keeps every Provider that was ever loaded in memory until it is gcd. It therefore is not memory efficient.
 *
 * Different DataProviders can be enabled or disabled to make the hiding of some DataProviders possible.
 */
public class DataProviderManager extends Observable {

    //TODO: add method to be notified of location updates to pass it down to the single DataProviders

    //contains all the DataProviders
    private HashMap<String, DataProvider> cameraList;
    //contains which DataProvider is active
    private HashMap<String, Boolean> isActive;

    public DataProviderManager()
    {
        cameraList = new LinkedHashMap<>();
        isActive = new LinkedHashMap<>();
    }

    /**
     * Wrapper for @see {@link #addDataProvider(DataProvider, String, boolean)} Note that the DataProvider will only be loaded but not enabled with this method.
     * @param camera The DataProvider that should be added
     * @param tag The tag which will be used for access to the DataProvider
     */
    private void addDataProvider(DataProvider camera, String tag)
    {
        addDataProvider(camera, tag, false);
    }

    /**
     * Adds the DataProvider to the list of data providers. It's data will also be loaded asynchronously.
     * @param camera The DataProvider which will be added to the list
     * @param tag The tag which will be used for access to the DataProvider
     * @param shouldBeActive Whether the DataProvider should be marked as active once it is loaded
     */
    public void addDataProvider(DataProvider camera, String tag, boolean shouldBeActive)
    {
        cameraList.put(tag, camera);
        isActive.put(tag, false);

        new FillDataProviderTask().execute(new FillDataProviderTaskParams(camera, tag));
    }

    /**
     * Adds DataProvider to the list with @see {@link #addDataProvider(DataProvider, String, boolean)} and lets it be enabled when finished or enables it when it already is loaded
     * @param provider The DataProvider to be loaded and enabled
     * @param tag The tag which will be used for access to the DataProvider
     */
    public void add(DataProvider provider, String tag)
    {
        if(!cameraList.containsKey(tag))
        {
            addDataProvider(provider, tag, true);
        }
        else
        {
            enable(tag);
        }
    }

    /**
     * Marks all the DataProviders as disabled.
     */
    public void disableAll()
    {
        setAllTo(false);
    }

    /**
     * Marks all the DataProviders as enabled.
     */
    public void enableAll()
    {
        setAllTo(true);
    }

    private void setAllTo(boolean bool)
    {
        for(String tag : isActive.keySet())
        {
            isActive.put(tag, bool);
        }
    }

    /**
     * Querys the a DataProvider via the previously specified tag whether it is enabled or not
     * @param tag The tag of the DataProvider
     * @return The status of the DataProvider
     */
    public boolean isEnabled(String tag)
    {
        //TODO: could theoretically throw npe if the mapping of tag is null, shouldn't be able to get null anyways (fix?)
        return isActive.containsKey(tag) && isActive.get(tag);
    }

    /**
     * Enables the DataProvider with the specified tag
     * @param tag The tag of the DataProvider
     * @return Whether the operation was successful (The DataProvider has to exist to be enabled.
     */
    private boolean enable(String tag)
    {
        if(isActive.containsKey(tag))
        {
            isActive.put(tag, true);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Disables the DataProvider with the specified tag
     * @param tag The tag of the DataProvider
     * @return Whether the operation was successful (The DataProvider has to exist to be enabled.
     */
    public boolean disable(String tag)
    {
        if(isActive.containsKey(tag))
        {
            isActive.put(tag, false);
            return true;
        }
        else {
            return false;
        }
    }

    /**
     * Returns the DataProvider Object for the tag
     * @param tag The tag of the DataProvider
     * @return The DataProvider with the given Tag
     * @throws IllegalArgumentException If the tag doesn`t map to a DataProvider
     */
    public DataProvider getDataProvider(String tag)
    {
        if(!cameraList.containsKey(tag)) throw new IllegalArgumentException("The specified key is not mapped to a DataProvider!");
        return cameraList.get(tag);
    }

    /**
     * Returns every DataProvider that is loaded
     * @return Every DataProvider that is loaded
     */
    public List<DataProvider> getAllDataProviders()
    {
        return new LinkedList<>(cameraList.values());
    }

    /**
     * Returns every DataProvider which is enabled.
     * @return All DataProviders that are enabled
     */
    public List<DataProvider> getDataProviders()
    {
        List<DataProvider> providers = new LinkedList<>();
        for(String tag : isActive.keySet())
        {
            if(isActive.get(tag) && cameraList.get(tag).hasData())
            {
                providers.add(cameraList.get(tag));
            }
        }
        return providers;
    }

    /**
     * Checks if a camera is nearer than radius to referenceLocation
     * @param radius The radius in which a camera will trigger around referenceLocation
     * @param referenceLocation The Location which is used as reference to check for Cameras
     * @return Whether a camera is in the radius radius of the Location referenceLocation
     */
    public boolean isCameraNearerThan(float radius, Location referenceLocation)
    {
        for (DataProvider provider : getDataProviders()) {
            if (provider.hasData()) {
                if (provider.distanceToNearestCamera(referenceLocation) < radius) {
                    return true;
                }
            }
        }
        return false;
    }


    //Loads the Data for the DataProviders and when it is loaded set the status to what was specified beforehand
    private class FillDataProviderTask extends AsyncTask<FillDataProviderTaskParams, Void, FillDataProviderTaskParams> {

        @Override
        protected FillDataProviderTaskParams doInBackground(FillDataProviderTaskParams... params) {
            FillDataProviderTaskParams taskParams = params[0];
            DataProvider provider = taskParams.provider;

            provider.fetchData();
            return taskParams;
        }

        @Override
        protected void onPostExecute(FillDataProviderTaskParams provider) {
            super.onPostExecute(provider);

            enable(provider.tag);
            Log.d("DataProviderManager", "loaded: " + provider.tag);
            setChanged();
            notifyObservers();
        }
    }

    //Custom DataStructure to store both the DataProvider and it's tag in one object to pass it through the AsyncTask
    private static class FillDataProviderTaskParams {
        DataProvider provider;
        String tag;

        FillDataProviderTaskParams(DataProvider dataProvider, String strTag)
        {
            provider = dataProvider;
            tag = strTag;
        }
    }

}
