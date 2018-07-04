package com.example.katrin.testsnepet;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stores the positions of the flags. Implements Parcelable to be able to be sent through an intent.
 * By storing the position in a different class we decrease the amount of variables to be sent
 * through an intent.
 * @author Rick Dijkstra
 * @version 1.0, 5 jun 2018
 */
public class FlagPositions implements Parcelable {
    /** Stores the coordinates of the flags. */
    private int[] coords;

    /** Indicates whether all coordinates have been received. */
    private boolean flagsreceived = false;

    /**
     * Constructor for FlagPositions that sets it to a default value of 6 coordinates.
     */
    public FlagPositions() {
        this(6);
    }

    /**
     * Creates new array to store a certain number of flag coordinates. Sets the coordinates to a
     * default of -1. Sets flag whether all coordinates have been received to false.
     * @param number_of_flags   The number of coordinates that will be stored.
     * @since 1.0
     */
    public FlagPositions(int number_of_flags) {
        // Integer array to store the data.
        coords = new int[number_of_flags];

        // Set coordinates to default value.
        for (int i = 0; i<coords.length;i++){
            coords[i] = -1;
        }

        // Set flag.
        flagsreceived = false;
    }

    /**
     * Constructor for when class has been passed through intent
     * @param in    Contains the variables of the class that have to be set.
     */
    protected FlagPositions(Parcel in) {
        coords = in.createIntArray();
        flagsreceived = in.readByte() != 0;
    }

    /**
     * Getter to see whether all coordinates have been set.
     * @return  true if all flags have been set
     * @since 1.0
     */
    public boolean isFlagsreceived() {
        return flagsreceived;
    }

    /**
     * Setter of the flag whether all coordinates have been set.
     * @param flagsreceived should be true if all coordinates have been set.
     * @since 1.0
     */
    public void setFlagsreceived(boolean flagsreceived) {
        this.flagsreceived = flagsreceived;
    }

    /**
     * Getter for a flag coordinate. Due to numbering of the API the position is 1 off.
     * @param i position of the flag coordinate in the coordinate array plus 1
     * @return  the coordinate of the flag
     * @since 1.0
     */
    public int getCoord(int i) {
        return coords[i-1];
    }

    /**
     * sets a flag coordinate. Due to the numbering of the API positions are given with a +1 value.
     * @param coord the coordinate to be stored
     * @param pos   the position +1 where the coordinate will be stored in the array
     */
    public void setCoord(int coord, int pos) {
        this.coords[pos-1] = coord;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeIntArray(coords);
        dest.writeByte((byte) (flagsreceived ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<FlagPositions> CREATOR = new Creator<FlagPositions>() {
        @Override
        public FlagPositions createFromParcel(Parcel in) {
            return new FlagPositions(in);
        }

        @Override
        public FlagPositions[] newArray(int size) {
            return new FlagPositions[size];
        }
    };

}
