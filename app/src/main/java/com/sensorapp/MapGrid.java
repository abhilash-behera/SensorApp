package com.sensorapp;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.maps.android.geometry.Bounds;

import java.util.ArrayList;

/**
 * Created by abhilash on 21/1/18
 */

public class MapGrid {
    private ArrayList<Polygon> gridList=new ArrayList<>();
    private GoogleMap map;

    public MapGrid(GoogleMap map){
        this.map=map;
    }


    public void drawGrid(LatLng start,LatLng destination,double clusterSize){
        cleanMap();
        Double minX=start.latitude;
        Double maxX=destination.latitude;
        Double minY=start.longitude;
        Double maxY=destination.longitude;

        int stepX=(int)Math.round((maxX-minX)/Double.valueOf(clusterSize));
        int stepY=(int)Math.round((maxY-minY)/Double.valueOf(clusterSize));
        Log.d("awesome","stepX: "+stepX+ " stepY: "+stepY);

        int step=stepX>stepY?stepX:stepY;

        for(int i=0;i<=step;i++){
            Double y=minY;
            for(int j=0;j<=step;j++){
                PolygonOptions polygonOptions=new PolygonOptions();
                polygonOptions.add(new LatLng(minX,y))
                        .add(new LatLng(minX,y+clusterSize))
                        .add(new LatLng(minX+clusterSize,y+clusterSize))
                        .add(new LatLng(minX+clusterSize,y));
                polygonOptions.strokeWidth(2.0f)
                        .strokeColor(0xff334455);
                Polygon polygon=map.addPolygon(polygonOptions);
                gridList.add(polygon);
                y+=clusterSize;
            }
            minX+=clusterSize;
        }
    }

    public GridNode getStartNode(){
        GridNode sourceNode=new GridNode();
        sourceNode.setPolygon(gridList.get(0));
        sourceNode.setChildren(getChildren(sourceNode));
        Log.d("awesome","Created source node: "+sourceNode);
        return sourceNode;
    }

    private ArrayList<GridNode> getChildren(GridNode parent){
        ArrayList<GridNode> children=new ArrayList<>();
        for(Polygon polygon:gridList){
            int matchCount=getMatchCount(polygon,parent.getPolygon());
            if(matchCount!=0&&matchCount<4){
                GridNode gridNode=new GridNode();
                gridNode.setPolygon(polygon);
                if(!isParent(parent,gridNode)){
                    children.add(gridNode);
                }
                Log.d("awesome","Added child: "+gridNode+" to"+parent);
            }
        }
        Log.d("awesome","Total children: "+children.size());

        for(GridNode gridNode:children){
            gridNode.setChildren(getChildren(gridNode));
        }

        return children;
    }

    private boolean isParent(GridNode parent,GridNode child){
        if(parent.getChildren()!=null){
            for(GridNode children:parent.getChildren()){
                if(childrenEqual(children,child)){
                    Log.d("awesome","Oops "+children+" was my parent node");
                    return true;
                }
            }
        }
        return false;
    }

    private boolean childrenEqual(GridNode children,GridNode child){
        int count=0;
        for(int i=0;i<children.getPolygon().getPoints().size()-1;i++){
            for(int j=0;j<child.getPolygon().getPoints().size()-1;j++){
                if(children.getPolygon().getPoints().get(i).latitude==child.getPolygon().getPoints().get(j).latitude&&children.getPolygon().getPoints().get(i).latitude==child.getPolygon().getPoints().get(j).longitude){
                    count+=1;
                    break;
                }
            }
        }

        if(count==4){
            return true;
        }else{
            return false;
        }
    }

    private int getMatchCount(Polygon a,Polygon b){
        int count=0;
        Log.d("awesome","a: "+a.getPoints().size()+" b: "+b.getPoints().size());
        for(int i=0;i<a.getPoints().size()-1;i++){
            for(int j=0;j<b.getPoints().size()-1;j++){
                if(a.getPoints().get(i).latitude==b.getPoints().get(j).latitude&&a.getPoints().get(i).longitude==b.getPoints().get(j).longitude){
                    count+=1;
                    break;
                }
            }
        }
        return count;
    }

    private void cleanMap(){
        for(Polygon polygon:gridList){
            polygon.remove();
        }
    }
}
