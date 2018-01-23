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
import android.util.Log;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
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

import static com.sensorapp.PlanPathViewActivity.setColorAlpha;

class DebugHelper {
    private List<Polyline> gridLines = new ArrayList<Polyline>();

    void drawDebugGrid(GoogleMap map, double clusterSize) {
        cleanup();
        Projection projection = map.getProjection();
        LatLngBounds bounds = projection.getVisibleRegion().latLngBounds;
        double minY = -180 + clusterSize * (int) (SphericalMercator.scaleLatitude(bounds.southwest.latitude) / clusterSize);
        double minX = -180 + clusterSize * (int) (SphericalMercator.scaleLongitude(bounds.southwest.longitude) / clusterSize);
        double maxY = -180 + clusterSize * (int) (SphericalMercator.scaleLatitude(bounds.northeast.latitude) / clusterSize);
        double maxX = -180 + clusterSize * (int) (SphericalMercator.scaleLongitude(bounds.northeast.longitude) / clusterSize);

        for (double y = minY; y <= maxY; y += clusterSize) {
            Polyline polyline=map.addPolyline(new PolylineOptions().color(Color.RED).width(1.0f).add(new LatLng(SphericalMercator.toLatitude(y), bounds.southwest.longitude),
                    new LatLng(SphericalMercator.toLatitude(y), bounds.northeast.longitude)));
            Log.d("awesome","Polyline: "+polyline);
            gotoLocation(map,polyline.getPoints().get(0).latitude,polyline.getPoints().get(0).longitude);
            gridLines.add(polyline);
        }

        if (minX <= maxX) {
            for (double x = minX; x <= maxX; x += clusterSize) {
                gridLines.add(map.addPolyline(new PolylineOptions().color(Color.RED).width(1.0f).add(new LatLng(bounds.southwest.latitude, x),
                        new LatLng(bounds.northeast.latitude, x))));
            }
        } else {
            for (double x = -180; x <= minX; x += clusterSize) {
                gridLines.add(map.addPolyline(new PolylineOptions().color(Color.RED).width(1.0f).add(new LatLng(bounds.southwest.latitude, x),
                        new LatLng(bounds.northeast.latitude, x))));
            }
            for (double x = maxX; x < 180; x += clusterSize) {
                gridLines.add(map.addPolyline(new PolylineOptions().color(Color.RED).width(1.0f).add(new LatLng(bounds.southwest.latitude, x),
                        new LatLng(bounds.northeast.latitude, x))));
            }
        }
    }

    private void gotoLocation(GoogleMap map,double lat, double lng) {

        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
        map.moveCamera(update);


    }

    void cleanup() {
        for (Polyline polyline : gridLines) {
            polyline.remove();
        }
        gridLines.clear();
    }

}
