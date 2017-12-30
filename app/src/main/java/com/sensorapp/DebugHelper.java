/*
 * Copyright (C) 2013 Maciej Górski
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sensorapp;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;

import static com.sensorapp.PlanPathViewActivity.setColorAlpha;

class DebugHelper {

    private final DatabaseHelper databaseHelper;
    private List<Polyline> gridLines = new ArrayList<Polyline>();
    private List<DoublePoint> pointsList = new ArrayList<>();
    private List<Polygon> polygonList = new ArrayList<>();
    private List<LatLngBounds> boundsList = new ArrayList<>();
    private List<LatLngBounds> drawableItemsList = new ArrayList<>();
    private List<PolygonOptions> optionsList = new ArrayList<>();

    LatLngBounds myrect = null;
    PolylineOptions lineOptions;
    PolylineOptions lineOptions1;
    PolylineOptions lineOptions3;
    PolylineOptions lineOptions4;

    public DebugHelper(final Context context) {
        databaseHelper = new DatabaseHelper(context);
        bolts.Task.callInBackground(new Callable<ArrayList<FavouritesList>>() {
            @Override
            public ArrayList<FavouritesList> call() throws Exception {
                ArrayList<FavouritesList> favouritesList = databaseHelper.getAllFabList();
                if (!favouritesList.isEmpty()) {
                    for (int i = 0; i < favouritesList.size(); i++) {
                        String source = favouritesList.get(i).getSource();
                        String destination = favouritesList.get(i).getDestination();
                        Geocoder gc = new Geocoder(context);
                        List<Address> list = gc.getFromLocationName(source, 1);
                        List<Address> list1 = gc.getFromLocationName(destination, 1);

                        Address add = list.get(0);
                        Address add1 = list1.get(0);
                        double latd = add1.getLatitude();
                        double lngd = add1.getLongitude();
                        double lat = add.getLatitude();
                        double lng = add.getLongitude();
                        LatLngBounds.Builder builder = new LatLngBounds.Builder();
                        builder.include(new LatLng(latd, lngd));
                        builder.include(new LatLng(lat, lng));
                        myrect = builder.build();
                        drawableItemsList.add(myrect);
                    }

                }
                return favouritesList;
            }
        });


        //  favouritesLists = databaseHelper.getLatLang();
    }

    void drawDebugGrid(GoogleMap map, double clusterSize) {

        Projection projection = map.getProjection();
        LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
        cleanup(map, bounds);
        double minY = -180 + clusterSize * (int) (SphericalMercator.scaleLatitude(bounds.southwest.latitude) / clusterSize);
        double minX = -180 + clusterSize * (int) (SphericalMercator.scaleLongitude(bounds.southwest.longitude) / clusterSize);
        double maxY = -180 + clusterSize * (int) (SphericalMercator.scaleLatitude(bounds.northeast.latitude) / clusterSize);
        double maxX = -180 + clusterSize * (int) (SphericalMercator.scaleLongitude(bounds.northeast.longitude) / clusterSize);
        for (double y = minY, x = minX; y <= maxY; y += clusterSize, x += clusterSize) {
            gridLines.add(map.addPolyline(new PolylineOptions().width(2f).color(Color.BLACK).add(new LatLng(SphericalMercator.toLatitude(y), bounds.southwest.longitude),
                    new LatLng(SphericalMercator.toLatitude(y), bounds.northeast.longitude))));
            gridLines.add(map.addPolyline(new PolylineOptions().width(2f).color(Color.BLACK).add(new LatLng(bounds.southwest.latitude, x),
                    new LatLng(bounds.northeast.latitude, x))));
            pointsList.add(new DoublePoint(x, SphericalMercator.toLatitude(y)));
        }
        //        for (double x = minX; x <= maxX; x += clusterSize) {
//            gridLines.add(map.addPolyline(new PolylineOptions().width(2f).color(Color.BLACK).add(new LatLng(bounds.southwest.latitude, x),
//                    new LatLng(bounds.northeast.latitude, x))));
//        }
        for (int k = 0; k <= pointsList.size(); k++) {
            for (int i = 0; i < pointsList.size() - 1 - k; i++) {
                LatLngBounds rectBound = new LatLngBounds(new LatLng(pointsList.get(i + k).y, pointsList.get(i).x), new LatLng(pointsList.get(i + k + 1).y, pointsList.get(i + 1).x));
//                drawBounds(bounds2, Color.RED, map);
                boundsList.add(rectBound);
            }
        }
        for (int k = 1; k <= pointsList.size(); k++) {
            for (int i = 0; i < pointsList.size() - 1 - k; i++) {
                LatLngBounds rectBounds = new LatLngBounds(new LatLng(pointsList.get(i).y, pointsList.get(i + k).x), new LatLng(pointsList.get(i + 1).y, pointsList.get(i + k + 1).x));
//                drawBounds(bounds2, Color.RED, map);
                boundsList.add(rectBounds);
            }
        }
        for (int i = 0; i < drawableItemsList.size(); i++) {
            if (bounds.contains(drawableItemsList.get(i).northeast) || bounds.contains(drawableItemsList.get(i).southwest)|| bounds.contains(drawableItemsList.get(i).getCenter()))
                for (int k = 0; k < boundsList.size(); k++) {
                    if (drawableItemsList.get(i).contains(boundsList.get(k).getCenter()) || drawableItemsList.get(i).contains(boundsList.get(k).northeast)
                            || drawableItemsList.get(i).contains(boundsList.get(k).southwest)) {
                        drawBounds(boundsList.get(k), Color.RED, map);
                    }
                }
        }


//        if (minX <= maxX) {
//            for (double x = minX; x <= maxX; x += clusterSize) {
//                gridLines.add(map.addPolyline(new PolylineOptions().width(2f).color(Color.BLACK).add(new LatLng(bounds.southwest.latitude, x),
//                        new LatLng(bounds.northeast.latitude, x))));
//            }
//        } else {
//            for (double x = -180; x <= minX; x += clusterSize) {
//                gridLines.add(map.addPolyline(new PolylineOptions().width(2f).color(Color.BLACK).add(new LatLng(bounds.southwest.latitude, x),
//                        new LatLng(bounds.northeast.latitude, x))));
//            }
//            for (double x = maxX; x < 180; x += clusterSize) {
//                gridLines.add(map.addPolyline(new PolylineOptions().width(2f).color(Color.BLACK).add(new LatLng(bounds.southwest.latitude, x),
//                        new LatLng(bounds.northeast.latitude, x))));
//            }
//
//        }

    }

    private void drawBounds(LatLngBounds bounds, int color, GoogleMap mMap) {
        PolygonOptions polygonOptions = new PolygonOptions()
                .add(new LatLng(bounds.northeast.latitude, bounds.northeast.longitude))
                .add(new LatLng(bounds.southwest.latitude, bounds.northeast.longitude))
                .add(new LatLng(bounds.southwest.latitude, bounds.southwest.longitude))
                .add(new LatLng(bounds.northeast.latitude, bounds.southwest.longitude))
                .strokeColor(color).strokeWidth(3.5f).fillColor(Color.parseColor(setColorAlpha(20, "#FF0004")));
        if (!optionsList.contains(polygonOptions)) {
            polygonList.add(mMap.addPolygon(polygonOptions));
            optionsList.add(polygonOptions);
        } else {
            polygonList.get(pointsList.size() - 1).setFillColor(Color.BLUE);
        }
    }

    public List<Polyline> getGridLines() {
        return gridLines;
    }

    void cleanup(GoogleMap map, LatLngBounds bounds) {
        for (Polyline polyline : gridLines) {
            polyline.remove();
        }
        for (Polygon polygon : polygonList) {
            polygon.remove();
        }
        gridLines.clear();
        pointsList.clear();
        polygonList.clear();
        boundsList.clear();
    }

}
