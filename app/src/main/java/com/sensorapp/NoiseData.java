package com.sensorapp;

import android.location.Location;

/**
 * Created by Abhilash on 08-12-2017
 */

public class NoiseData {
    private String audioPath;
    private String avgDb;
    private String latitude;
    private String longitude;

    public String getAudioPath() {
        return audioPath;
    }

    public String getAvgDb() {
        return avgDb;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public NoiseData(String audioPath, String avgDb, String latitude, String longitude) {

        this.audioPath = audioPath;
        this.avgDb = avgDb;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}