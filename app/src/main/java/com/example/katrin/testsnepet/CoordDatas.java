package com.example.katrin.testsnepet;

import android.os.Parcel;
import android.os.Parcelable;

public class CoordDatas implements Parcelable {
    private CoordData[] coord_data_list;
    private int added_numbers;

    public boolean isCoord_data_received() {
        return coord_data_received;
    }

    public void setCoord_data_received(boolean coord_data_received) {
        this.coord_data_received = coord_data_received;
    }

    private boolean coord_data_received;

    public CoordDatas(){
        this(6);
    }

    public CoordDatas(int number_of_coord_data_list){
        coord_data_list = new CoordData[number_of_coord_data_list];
        this.coord_data_received = false;
        for (int i = 0; i < number_of_coord_data_list; i++) {
            coord_data_list[i] = new CoordData(new float[]{0});
        }
        added_numbers=0;
    }

    protected CoordDatas(Parcel in) {
        coord_data_list = in.createTypedArray(CoordData.CREATOR);
        coord_data_received = in.readByte() != 0;
    }

    public CoordData getCoord_data(int pos) {
        return coord_data_list[pos];
    }

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
