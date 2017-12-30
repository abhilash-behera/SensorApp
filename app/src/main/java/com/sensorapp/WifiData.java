package com.sensorapp;

/**
 * Created by Abhilash on 09-12-2017
 */

public class WifiData {
    private String connection;
    private String speed;
    private String network;
    private String progress;
    private String rssi;
    private String latitude;
    private String longitude;



    public WifiData(String connection, String speed, String network, String progress, String rssi, String latitude, String longitude) {

        this.connection = connection;
        this.speed = speed;
        this.network = network;
        this.progress = progress;
        this.rssi = rssi;
        this.latitude = latitude;
        this.longitude = longitude;
    }

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

    public String getRssi() {
        return rssi;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }
}
