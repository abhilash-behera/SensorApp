package com.sensorapp;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Abhilash on 03-01-2018
 */

public class Node {
    private LatLng latLng;
    private float g;
    private float h;
    private float f;

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public float getG() {
        return g;
    }

    public void setG(float g) {
        this.g = g;
    }

    public float getH() {
        return h;
    }

    public void setH(float h) {
        this.h = h;
    }

    public float getF() {
        return f;
    }

    public void setF(float f) {
        this.f = f;
    }
}
