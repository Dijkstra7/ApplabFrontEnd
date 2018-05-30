package com.example.katrin.testsnepet;

import android.os.Parcel;
import android.os.Parcelable;

public class FlagPositions implements Parcelable {

    private int[] coords;
    private boolean flagsreceived = false;

    public FlagPositions() {
        this(6);
    }

    public FlagPositions(int number_of_flags) {
        coords = new int[number_of_flags];
        for (int i = 0; i<coords.length;i++){
            coords[i] = -1;
        }
        flagsreceived = false;
    }

    public boolean isFlagsreceived() {
        return flagsreceived;
    }

    public void setFlagsreceived(boolean flagsreceived) {
        this.flagsreceived = flagsreceived;
    }

    protected FlagPositions(Parcel in) {
        coords = in.createIntArray();
        flagsreceived = in.readByte() != 0;
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

    public int getCoord(int i) {
        return coords[i-1];
    }

    public void setCoord(int coord, int pos) {
        this.coords[pos-1] = coord;
    }

}
