package com.golftronics.golfball.ble;

import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "puttdata_table")
public class PuttData {

    @PrimaryKey(autoGenerate = true)
    private int id;

    private long date;

    private String userID;

    private int putt_number;

    private double velocity;

    private double distance;

    private int slope;


    private double stimp;

    private boolean made;


    private double continuation_distance;


    private double target_distance;


    private double miss_distance;


    private String stopped;


    public PuttData(long date, String userID, int putt_number, double velocity, double distance,
                    int slope, double stimp, boolean made, double continuation_distance,
                    double target_distance, double miss_distance, String stopped) {

        this.date = date;
        this.userID = userID;
        this.putt_number = putt_number;
        this.velocity = velocity;
        this.distance = distance;
        this.slope = slope;
        this.stimp = stimp;
        this.made = made;
        this.continuation_distance = continuation_distance;
        this.target_distance = target_distance;
        this.miss_distance = miss_distance;
        this.stopped = stopped;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {

        return id;
    }

    public long getDate() {


        return date;
    }

    public String getUserID() {
        return userID;
    }

    public double getVelocity() {
        return velocity;
    }

    public double getDistance() {
        return distance;
    }

    public boolean isMade() {
        return made;
    }

    public int getSlope() {
        return slope;
    }

    public double getStimp() {
        return stimp;
    }

    public int getPutt_number() {
        return putt_number;
    }

    public double getContinuation_distance() {
        return continuation_distance;
    }

    public double getTarget_distance() {
        return target_distance;
    }

    public double getMiss_distance() {
        return miss_distance;
    }

    public String getStopped() {
        return stopped;
    }
}
