package com.sensorapp;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by abhilash on 24/1/18
 */

public class DataPoint {
    private LatLng position;
    private Double measurement;
    private String alpha;

    public DataPoint(LatLng position,Double measurement){
        this.position=position;
        this.measurement=measurement;
    }

    public LatLng getPosition(){
        return position;
    }

    public String getAlpha(){
        return alpha;
    }

    public Double getMeasurement(){
        return measurement;
    }

    public void setAlpha(String alpha) {
        this.alpha = alpha;
    }
}
