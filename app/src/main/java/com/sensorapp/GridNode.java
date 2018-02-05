package com.sensorapp;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abhilash on 22/1/18
 */

public class GridNode {
    public static final String TYPE_OBSTACLE="obstacle";
    public static final String TYPE_NODE="node";
    public static final String TYPE_SOURCE="source";
    public static final String TYPE_DESTINATION="destination";


    public Polygon polygon;
    public String type;
    public Double f=0.0d;
    public Double g=0.0d;
    public Double h=0.0d;
    public int a=-1;
    public int b=-1;
    public GridNode parent;
    public Boolean opened=false;
    public Boolean closed=false;

    public LatLng getLocation(){
        Double avgLatitude=0.0d;
        Double avgLongitude=0.0d;

        for(int i=0;i<polygon.getPoints().size()-1;i++){
            avgLatitude+=polygon.getPoints().get(i).latitude;
            avgLongitude+=polygon.getPoints().get(i).longitude;
        }
        avgLatitude=avgLatitude/4;
        avgLongitude=avgLongitude/4;

        return new LatLng(avgLatitude,avgLongitude);
    }

    public Double getF(){
        return g+h;
    }

}
