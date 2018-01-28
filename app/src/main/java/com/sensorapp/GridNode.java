package com.sensorapp;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by abhilash on 22/1/18
 */

public class GridNode {
    private ArrayList<GridNode> children=new ArrayList<>();
    private Polygon polygon;
    private String type="node";
    public static final String TYPE_OBSTACLE="obstacle";
    public static final String TYPE_NODE="node";

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public ArrayList<GridNode> getChildren() {
        return children;
    }

    public void setChildren(ArrayList<GridNode> children) {
        this.children = children;
    }

    public Polygon getPolygon() {
        return polygon;
    }

    public void setPolygon(Polygon polygon) {
        this.polygon = polygon;
    }

}
