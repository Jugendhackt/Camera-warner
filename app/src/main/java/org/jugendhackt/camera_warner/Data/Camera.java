package org.jugendhackt.camera_warner.Data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

public class Camera implements Parcelable{

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

    public static final Parcelable.Creator<Camera> CREATOR
            = new Parcelable.Creator<Camera>() {
        public Camera createFromParcel(Parcel in) {
            return new Camera(in);
        }

        public Camera[] newArray(int size) {
            return new Camera[size];
        }
    };


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

    @Override
    public int describeContents() {
        return 0;
    }

    public Camera(Parcel in)
    {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
