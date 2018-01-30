package com.sensorapp;

/**
 * Created by abhilash on 30/1/18
 */

public class TraceNode {
    GridNode node;
    GridNode parent;

    public GridNode getNode() {
        return node;
    }

    public void setNode(GridNode node) {
        this.node = node;
    }

    public GridNode getParent() {
        return parent;
    }

    public void setParent(GridNode parent) {
        this.parent = parent;
    }
}
