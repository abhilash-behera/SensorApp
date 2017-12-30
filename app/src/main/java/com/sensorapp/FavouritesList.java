package com.sensorapp;


public class FavouritesList {

    private  String id,source,destination,distance,averageNoise,algorithm="Default";
    //parametrize constructer
    public FavouritesList(String id, String source, String destination, String distance, String averageNoise) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.averageNoise = averageNoise;
    }
    public FavouritesList(String id, String source, String destination, String distance, String averageNoise,String algorithm) {
        this.id = id;
        this.source = source;
        this.destination = destination;
        this.distance = distance;
        this.averageNoise = averageNoise;
        this.algorithm = algorithm;
    }
    //geter seter..
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getDistance() {
        return distance;
    }

    public void setDistance(String distance) {
        this.distance = distance;
    }

    public String getAverageNoise() {
        return averageNoise;
    }

    public String getAlgorithm() {
        return algorithm;
    }

    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    public void setAverageNoise(String averageNoise) {
        this.averageNoise = averageNoise;
    }

    //setting all value in string..
    @Override
    public String toString() {
        return "FavouritesList{" +
                "id='" + id + '\'' +
                ", source='" + source + '\'' +
                ", destination='" + destination + '\'' +
                ", distance='" + distance + '\'' +
                ", averageNoise='" + averageNoise + '\'' +
                '}';
    }
}
