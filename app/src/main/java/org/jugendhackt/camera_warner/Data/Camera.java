package org.jugendhackt.camera_warner.Data;

/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

public class Camera {

    private double latitude;
    private double longitude;

    private boolean hasDirection;
    private int direction;

    public Camera(double latitude, double longitude, int direction)
    {
        this.latitude = latitude;
        this.longitude = longitude;

        this.direction = direction;
        hasDirection = true;
    }

    public Camera(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;

        hasDirection = false;
    }

    public double getLatitude()
    {
        return latitude;
    }

    public double getLongitude()
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
