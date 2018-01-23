package com.sensorapp;


public class FavouriteData {
    private int id;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    private String source;
    private String destination;
    private String algorithm;
    private String polyPoints;
    private String dataPoints;
    private String measurements;
    private String type;

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public String getPolyPoints() {
        return polyPoints;
    }

    public String getDataPoints() {
        return dataPoints;
    }

    public String getMeasurements() {
        return measurements;
    }

    public String getType(){
        return type;
    }

    public FavouriteData(String source, String destination, String algorithm, String polyPoints, String dataPoints, String measurements,String type) {

        this.source = source;
        this.destination = destination;
        this.algorithm = algorithm;
        this.polyPoints = polyPoints;
        this.dataPoints = dataPoints;
        this.measurements = measurements;
        this.type=type;
    }
}
