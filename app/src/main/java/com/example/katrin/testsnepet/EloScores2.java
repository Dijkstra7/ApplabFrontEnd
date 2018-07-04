package com.example.katrin.testsnepet;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Stores the ELO scores from a user in a float array. Implements Parcelable to enable the passing
 * on of the data through intent. However, due to unmarshalling error this class is not in use at
 * the moment.
 *
 * @author Rick Dijkstra
 * @version 1.0, 5 jun 2018
 */
public class EloScores2 implements Parcelable {
    /** Create list storing the ELO scores. */
    private float[] elo_scores;

    /** Keep track of how many ELO scores have been added. */
    private int added_numbers;

    /** Indicate whether all ELO scores have been received. */
    private boolean elo_scores_received;

    /**
     * Getter for the flag elo_scores_received
     * @return  true if all ELO scores are received
     * @since 1.0
     */
    public boolean isElo_scores_received() {
        return elo_scores_received;
    }

    /**
     * Setter for flag indicating whether all ELO scores have been received.
     * @param elo_scores_received   should be true if all scores have been received
     * @since 1.0
     */
    public void setElo_scores_received(boolean elo_scores_received) {
        this.elo_scores_received = elo_scores_received;
    }

    /**
     * Constructor that defaults the number of scores to 6.
     * @since 1.0
     */
    public EloScores2(){
        this(6);
    }

    /**
     * Constructor with a specific number of ELO scores being set. Creates array to store the ELO
     * score values and set them to the default value of 0. Sets flag of all scores being received
     * to false and the number of received scores to 0.
     *
     * @param number_of_elo_scores  the number of scores that will be stored
     * @since 1.0
     */
    public EloScores2(int number_of_elo_scores){
        // Store the ELO scores as floats
        elo_scores = new float[number_of_elo_scores];

        // Set scores to default value
        for (int i = 0; i < number_of_elo_scores; i++) {
            elo_scores[i] = 0;
        }

        // Set flags and number of scores received.
        elo_scores_received = false;
        added_numbers = 0;
    }

    /**
     * Constructor that is called after class has been sent through an intent.
     * @param in    Parcel containing the information of the class.
     * @since 1.0
     */
    protected EloScores2(Parcel in) {
        // Recreate values
        elo_scores = in.createFloatArray();
        elo_scores_received = in.readByte() != 0;
    }

    /**
     * Getter for ELO scores. Only returns the ELO score at a certain position
     * @param pos   indicates position of ELO score to be returned
     * @return  The ELO score at position pos.
     * @since 1.0
     */
    public float getElo_scores(int pos) {
        return elo_scores[pos];
    }

    /**
     * Setter for ELO scores. Adds ELO score at new position, adds to the counter of ELO scores
     * being added and checks whether enough ELO scores have been added.
     * @param score ELO score to be added
     * @param pos   indicates the position where the ELO score will be added in the array
     * @since 1.0
     */
    public void setElo_scores(float score, int pos) {
        this.elo_scores[pos] = score;
        added_numbers += 1;
        // If there are as much ELO scores added as are in the array, we assume all scores have been
        // added.
        elo_scores_received = (added_numbers>=elo_scores.length);
    }

    public static final Creator<EloScores2> CREATOR = new Creator<EloScores2>() {
        @Override
        public EloScores2 createFromParcel(Parcel in) {
            return new EloScores2(in);
        }

        @Override
        public EloScores2[] newArray(int size) {
            return new EloScores2[size];
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
