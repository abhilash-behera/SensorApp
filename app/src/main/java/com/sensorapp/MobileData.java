package com.sensorapp;

/**
 * Created by Abhilash on 09-12-2017
 */

public class MobileData {
    private String connection;
    private String speed;
    private String network;
    private String progress;
    private String latitude;
    private String longitude;

    public String getConnection() {
        return connection;
    }

    public String getSpeed() {
        return speed;
    }

    public String getNetwork() {
        return network;
    }

    public String getProgress() {
        return progress;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public MobileData(String connection, String speed, String network, String progress, String latitude, String longitude) {

        this.connection = connection;
        this.speed = speed;
        this.network = network;
        this.progress = progress;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
