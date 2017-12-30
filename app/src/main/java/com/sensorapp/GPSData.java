package com.sensorapp;

/**
 * Created by Abhilash on 10-12-2017
 */

public class GPSData {
    private String source;
    private String destination;
    private String distance;
    private String duration;
    private String latitude;
    private String longitude;

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getDistance() {
        return distance;
    }

    public String getDuration() {
        return duration;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public GPSData(String source, String destination, String distance, String duration, String latitude, String longitude) {

        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.duration = duration;
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
