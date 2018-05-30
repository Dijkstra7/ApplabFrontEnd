package com.example.katrin.testsnepet;

import android.os.Parcel;
import android.os.Parcelable;

public class CoordData implements Parcelable {
    private float[] coord_data;

    public CoordData(float[] coord_data){
        this.coord_data = coord_data;
    }

    protected CoordData(Parcel in) {
        coord_data = in.createFloatArray();
    }

    public float[] getCoord_data() {
        return coord_data;
    }

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
}
