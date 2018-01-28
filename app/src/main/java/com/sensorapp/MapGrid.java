package com.sensorapp;

import android.util.Log;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;

/**
 * Created by abhilash on 21/1/18
 */

public class MapGrid {
    private ArrayList<Polygon> polygonList =new ArrayList<>();
    private ArrayList<GridNode> gridList;
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

        Log.d("awesome","minX:"+minX+",minY:"+minY);
        Log.d("awesome","maxX:"+maxX+",maxY:"+maxY);

        int stepX=(int)Math.round((maxX>minX?maxX-minX:minX-maxX)/Double.valueOf(clusterSize));
        int stepY=(int)Math.round((maxY>minY?maxY-minY:minY-maxY)/Double.valueOf(clusterSize));
        Log.d("awesome","stepX: "+stepX+ " stepY: "+stepY);

        int step=stepX>stepY?stepX:stepY;

        gridList=new ArrayList<>();

        for(int i=0;i<step;i++){
            Double y=minY;
            for(int j=0;j<step;j++){
                PolygonOptions polygonOptions=new PolygonOptions();
                polygonOptions.add(new LatLng(minX,y))
                        .add(new LatLng(minX,y+clusterSize))
                        .add(new LatLng(minX+clusterSize,y+clusterSize))
                        .add(new LatLng(minX+clusterSize,y));
                polygonOptions.strokeWidth(2.0f)
                        .strokeColor(0xff334455);
                Polygon polygon=map.addPolygon(polygonOptions);

                GridNode gridNode=new GridNode();
                gridNode.setPolygon(polygon);
                polygonList.add(polygon);
                gridList.add(gridNode);
                y+=clusterSize;
            }
            minX+=clusterSize;
        }
    }

    public GridNode getStartNode(){
        for(int i=0;i<gridList.size();i++){
            getChildren(gridList.get(i));
        }

        return gridList.get(0);
    }

    public void getChildren(GridNode gridNode){
        for(int i=0;i<gridList.size();i++){
            if(gridList.get(i)==gridNode){
                Log.d("awesome","Oops its me");
            }else{
                int matchCount=getMatchCount(gridList.get(i).getPolygon(),gridNode.getPolygon());
                if(matchCount>0&&matchCount<4){
                    gridNode.getChildren().add(gridList.get(i));
                    Log.d("awesome","Added child: "+gridList.get(i)+" to "+gridNode);
                }else{
                    Log.d("awesome","Oh no! That's not my successor");
                }
            }
        }
        Log.d("awesome","Total children of "+gridNode+" : "+gridNode.getChildren().size());
    }

    public ArrayList<GridNode> getGridList(){
        return this.gridList;
    }

    private int getMatchCount(Polygon a,Polygon b){
        if(a==b){
            return 4;
        }else{
            int count=0;
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
    }

    private void cleanMap(){
        for(Polygon polygon: polygonList){
            polygon.remove();
        }
    }
}
