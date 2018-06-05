package com.example.katrin.testsnepet;

import android.os.Parcel;
import android.os.Parcelable;

public class EloScores1 implements Parcelable {

    private float[] elo_scores;
    private int added_numbers;

    public boolean isElo_scores_received() {
        return elo_scores_received;
    }

    public void setElo_scores_received(boolean elo_scores_received) {
        this.elo_scores_received = elo_scores_received;
    }

    private boolean elo_scores_received;

    public EloScores1(){
        this(6);
    }

    public EloScores1(int number_of_elo_scores){
        elo_scores = new float[number_of_elo_scores];
        for (int i = 0; i < number_of_elo_scores; i++) {
            elo_scores[i] = 0;
        }
        elo_scores_received = false;
        added_numbers = 0;
    }

    protected EloScores1(Parcel in) {
        elo_scores = in.createFloatArray();
        elo_scores_received = in.readByte() != 0;
    }

    public float getElo_scores(int pos) {
        return elo_scores[pos];
    }

    public void setElo_scores(float score, int pos) {
        this.elo_scores[pos] = score;
        added_numbers += 1;
        elo_scores_received = (added_numbers==elo_scores.length);
    }

    public static final Creator<EloScores1> CREATOR = new Creator<EloScores1>() {
        @Override
        public EloScores1 createFromParcel(Parcel in) {
            return new EloScores1(in);
        }

        @Override
        public EloScores1[] newArray(int size) {
            return new EloScores1[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeFloatArray(elo_scores);
        dest.writeByte((byte) (elo_scores_received ? 1:0));
    }
}
