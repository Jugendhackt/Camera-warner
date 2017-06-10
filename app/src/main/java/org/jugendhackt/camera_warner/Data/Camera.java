package org.jugendhackt.camera_warner.Data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Julian Mundhahs on 10.06.2017.
 */

public class Camera implements Parcelable{

    private double latitude;
    private double longitude;


    public Camera(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
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
