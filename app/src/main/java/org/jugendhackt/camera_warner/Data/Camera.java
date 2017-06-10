package org.jugendhackt.camera_warner.Data;

/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

public class Camera {

    private long latitude;
    private long longitude;

    private boolean hasDirection;
    private int direction;

    public Camera(long latitude, long longitude, int direction)
    {
        this.latitude = latitude;
        this.longitude = longitude;

        this.direction = direction;
        hasDirection = true;
    }

    public Camera(long latitude, long longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;

        hasDirection = false;
    }

    public long getLatitude()
    {
        return latitude;
    }

    public long getLongitude()
    {
        return longitude;
    }

    public boolean hasDirection()
    {
        return hasDirection;
    }

    public int getDirection() throws IllegalStateException
    {
        if(!hasDirection)
        {
            throw new IllegalStateException("This field isnt set");
        }

        return direction;
    }

    public int getDirection_raw()
    {
        return direction;
    }
}
