package com.sensorapp;

import com.google.android.gms.maps.model.Polygon;

import java.util.ArrayList;

/**
 * Created by abhilash on 22/1/18
 */

public class GridNode {
    private ArrayList<GridNode> children;
    private Polygon polygon;
    private String type="node";

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
