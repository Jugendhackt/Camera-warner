package org.jugendhackt.camera_warner.Data;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Julian Mundhahs on 10.06.2017.
 */
/**
 * This Class is the Data class to store single cameras;
 */
public class Camera implements Parcelable{

    //latitude and longitude of the corresponding camera
    private double latitude;
    private double longitude;

    /**
     * Standard constructor
     * @param latitude the latitude of the camera
     * @param longitude the longitude of the camera
     */
    public Camera(double latitude, double longitude)
    {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    /**
     * Gets the cameras latitude
     * @return the latitude of the camera
     */
    public double getLatitude()
    {
        return latitude;
    }

    /**
     * Gets the cameras longitude
     * @return the longitude of the camera
     */
    public double getLongitude()
    {
        return longitude;
    }

    //the rest is only needed for the class to be parelable
    public static final Parcelable.Creator<Camera> CREATOR
            = new Parcelable.Creator<Camera>() {
        public Camera createFromParcel(Parcel in) {
            return new Camera(in);
        }

        public Camera[] newArray(int size) {
            return new Camera[size];
        }
    };

    /**
     * Describe the kinds of special objects contained in this Parcelable instance.
     * @return identifier for the kind
     * @see Parcelable
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * The constructor for the parcelable interface
     * Needed for the class to be parcelable
     * @param in the parcel that the data is stored in
     * @see Parcelable
     */
    public Camera(Parcel in)
    {
        latitude = in.readDouble();
        longitude = in.readDouble();
    }

    /**
     * Writes the data of the class to a parcel
     * Needed for the class to be parcelable
     * @param dest the parcel to write to
     * @param flags flags; unused
     * @see Parcelable
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
    }
}
