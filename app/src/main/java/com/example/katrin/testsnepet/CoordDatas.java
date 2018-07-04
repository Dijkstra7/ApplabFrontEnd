package com.example.katrin.testsnepet;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stores a list of all the coordinates of the movement of the car in a CoordData array. Implements
 * Parcelable to enable the passing on of the data through intent. However, due to unmarshalling
 * error this class is not in use at the moment.
 *
 * @author Rick Dijkstra
 * @version 1.0, 5 jun 2018
 */
public class CoordDatas implements Parcelable {
    /** List storing the coordinate arrays. */
    private CoordData[] coord_data_list;

    /** Counter of how many coordinate arrays are stored. */
    private int added_numbers;

    /** Boolean that stores whether all coordinate arrays have been received. */
    private boolean coord_data_received;

    /**
     * Check for whether all coordinates are received.
     * @return  True if all coordinates are received.
     * @since 1.0
     */
    public boolean isCoord_data_received() {
        return coord_data_received;
    }

    /**
     * Setter for the coord_data_received variable.
     * @param coord_data_received   Boolean, which should be true if all coordinates are received.
     * @since 1.0
     */
    public void setCoord_data_received(boolean coord_data_received) {
        this.coord_data_received = coord_data_received;
    }

    /**
     * Constructor for the CoordDatas class. When nothing is specified, the default amount of 6
     * arrays is assumed.
     * @since 1.0
     */
    public CoordDatas(){
        this(6);
    }

    /**
     * Constructor for the CoordDatas class. Sets up CoordData array of a certain number and fills
     * it with default arrays. Also sets boolean indicating that everything has been received to
     * false and number of added arrays to 0.
     * @param number_of_coord_data_list The number of CoordData arrays that we want to store.
     * @since 1.0
     */
    public CoordDatas(int number_of_coord_data_list){
        // Create new CoordData array
        coord_data_list = new CoordData[number_of_coord_data_list];

        // Fill array with default coordinate arrays.
        for (int i = 0; i < number_of_coord_data_list; i++) {
            coord_data_list[i] = new CoordData(new float[]{0});
        }

        // No data has been received yet.
        this.coord_data_received = false;
        added_numbers=0;
    }

    /**
     * Constructor for when the class has been passed through an intent.
     * @param in    Parcel containing the information for the class constructor
     * @since 1.0
     */
    protected CoordDatas(Parcel in) {
        // Unpack the CoordData arrays.
        coord_data_list = in.createTypedArray(CoordData.CREATOR);

        // Set the all data received flag.
        coord_data_received = in.readByte() != 0;
    }

    /**
     * Getter for a CoordData array.
     * @param pos   integer indicating which array you want returned
     * @return      CoordData array at position pos
     * @since 1.0
     */
    public CoordData getCoord_data(int pos) {
        return coord_data_list[pos];
    }

    /**
     * Overwrites a CoordData array and keeps track of how many arrays have been changed. If there
     * are as much changes as there are arrays, it assumes that all data has been received.
     * @param data  the CoordData array that contains coordinates for the car movement
     * @param pos   the position at which the array has to be saved.
     * @since 1.0
     */
    public void setCoord_data(CoordData data, int pos) {
        this.coord_data_list[pos] = data;
        added_numbers += 1;
        coord_data_received = (added_numbers==coord_data_list.length);
    }

    public static final Creator<CoordDatas> CREATOR = new Creator<CoordDatas>() {
        @Override
        public CoordDatas createFromParcel(Parcel in) {
            return new CoordDatas(in);
        }

        @Override
        public CoordDatas[] newArray(int size) {
            return new CoordDatas[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedArray(coord_data_list, flags);
        dest.writeByte((byte) (coord_data_received ? 1:0));
    }
}
