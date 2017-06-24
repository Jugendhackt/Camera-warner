package org.jugendhackt.camera_warner.Utils;

import android.provider.ContactsContract;

import org.jugendhackt.camera_warner.Data.Providers.DataProvider;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

public class DataProviderManager {

    private HashMap<String, DataProvider> cameraList;
    private HashMap<String, Boolean> isActive;

    public DataProviderManager()
    {
        cameraList = new LinkedHashMap<>();
        isActive = new LinkedHashMap<>();
    }

    private void addDataProvider(DataProvider camera, String tag)
    {
        cameraList.put(tag, camera);
        isActive.put(tag, true);
    }

    public void addDataProvider(DataProvider camera, String tag, boolean shouldBeActive)
    {
        cameraList.put(tag, camera);
        isActive.put(tag, shouldBeActive);
    }

    public void add(DataProvider provider, String tag)
    {
        if(!cameraList.containsKey(tag))
        {
            addDataProvider(provider, tag);
        }
        else
        {
            enable(tag);
        }
    }

    public void disableAll()
    {
        setAllTo(false);
    }

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

    public boolean isEnabled(String tag)
    {
        return isActive.get(tag);
    }

    private void enable(String tag)
    {
        isActive.put(tag, true);
    }

    public void disable(String tag)
    {
        isActive.put(tag, false);
    }

    public DataProvider getDataProvider(String tag)
    {
        return cameraList.get(tag);
    }

    public List<DataProvider> getAllDataProviders()
    {
        return new LinkedList<>(cameraList.values());
    }

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

}
