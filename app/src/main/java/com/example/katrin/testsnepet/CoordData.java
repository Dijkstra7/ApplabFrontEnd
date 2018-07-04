package com.example.katrin.testsnepet;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

/**
 * Stores the coordinates of the movement of the car in a float array. Implements Parcelable to
 * enable the passing on of the data through intent. However, due to unmarshalling error this class
 * is not in use at the moment.
 *
 * @author Rick Dijkstra
 * @version 1.0, 5 jun 2018
 */
public class CoordData implements Parcelable {
    /** Storage of the data*/
    private float[] coord_data;

    /**
     * Initilalize the class by storing a new float Array
     * @param coord_data    float array of the coordinates of the cars vertical movement.
     * @since 1.0
     */
    public CoordData(float[] coord_data){
        this.coord_data = coord_data;
    }

    /**
     * Initialize the class by unpacking the parcel from an intent.
     * @param in    the parcel containing the float array with the coordinates of the car.
     * @since 1.0
     */
    protected CoordData(Parcel in) {
        coord_data = in.createFloatArray();
    }

    /**
     * Getter method for the coordinate array
     * @return  The coordinate array.
     */
    public float[] getCoord_data() {
        return coord_data;
    }

    /**
     * Setter method for the coordinate array. Overwrites the previous data.
     * @param coord_data    float array containing the new coordinate values.
     */
    public void setCoord_data(float[] coord_data) {
        this.coord_data = coord_data;
    }

    public static final Creator<CoordData> CREATOR = new Creator<CoordData>() {
        @Override
        public CoordData createFromParcel(Parcel in) {
            return new CoordData(in);
        }

        @Override
        public CoordData[] newArray(int size) {
            return new CoordData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloatArray(coord_data);
    }

    @Override
    public String toString() {
        return "CoordData{" +
                "coord_data=" + Arrays.toString(coord_data) +
                '}';
    }
}
