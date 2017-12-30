package com.sensorapp;



public class DoublePoint {
    double x;
    double y;

    public DoublePoint(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public DoublePoint getPoints() {
        return new DoublePoint(x, y);
    }
}
