package com.sensorapp;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by Abhilash on 03-01-2018
 */

public class Node {
    private LatLng latLng;
    private int g=0;
    private int h=0;
    private int f=0;
    private Node parent;
    private Node child=null;
    private boolean visited=false;

    public boolean isVisited() {
        return visited;
    }

    public void setVisited(boolean visited) {
        this.visited = visited;
    }

    public ArrayList<Node> adjacencyList=new ArrayList<>();

    public Node getChild() {
        return child;
    }

    public void setChild(Node child) {
        this.child = child;
    }

    public ArrayList<Node> getAdjacencyList() {
        return adjacencyList;
    }

    public void setAdjacencyList(ArrayList<Node> adjacencyList) {
        this.adjacencyList = adjacencyList;
    }

    public Node getParent() {
        return parent;
    }

    public void setParent(Node parent) {
        this.parent = parent;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public int getG() {
        return g;
    }

    public void setG(int g) {
        this.g = g;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public int getF() {
        return f;
    }

    public void setF(int f) {
        this.f = f;
    }
}
