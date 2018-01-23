package com.sensorapp;

import android.*;
import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;

public class FavouriteRouteViewActivity extends AppCompatActivity {
    private View view;
    private FavouriteData favouriteData;
    private ArrayList<LatLng> dataPoints;
    private ArrayList<Double> measurements;
    private LatLng source;
    private LatLng destination;
    private MapFragment mapFragment;
    private GoogleMap map;
    private String type;
    private PolylineOptions polylineOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourite_route_view);
        initializeViews();
        checkInternetConnection();
    }

    private void checkInternetConnection() {
        if(Utils.isNetworkAvailable(FavouriteRouteViewActivity.this)){
            continueExecution();
        }else{
            Snackbar.make(view,"No internet connection available",Snackbar.LENGTH_INDEFINITE)
                    .setActionTextColor(Color.RED)
                    .setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkInternetConnection();
                        }
                    }).show();
        }
    }

    private void initializeViews() {
        view=findViewById(R.id.view);
        getSupportActionBar().setTitle("Favourite Route");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void continueExecution(){
        DatabaseHelper databaseHelper=new DatabaseHelper(FavouriteRouteViewActivity.this);
        favouriteData=databaseHelper.getFavouriteRouteById(getIntent().getIntExtra(FavouritesAdapter.FAVOURITE_ROUTE_ID,0));
        type=favouriteData.getType();
        polylineOptions=new PolylineOptions().width(5);
        if(favouriteData.getType().equalsIgnoreCase(DashBoardActivity.DATA_TYPE_NOISE)){
            getSupportActionBar().setSubtitle("with Noise measurements");
            polylineOptions.color(Color.RED);
        }else if(favouriteData.getType().equalsIgnoreCase(DashBoardActivity.DATA_TYPE_WIFI)){
            getSupportActionBar().setSubtitle("with Wifi measurements");
            polylineOptions.color(Color.GREEN);
        }else if(favouriteData.getType().equalsIgnoreCase(DashBoardActivity.DATA_TYPE_MOBILE_DATA)){
            getSupportActionBar().setSubtitle("with Mobile Data measurements");
            polylineOptions.color(Color.BLUE);
        }else if(favouriteData.getType().equalsIgnoreCase(DashBoardActivity.DATA_TYPE_GPS)){
            getSupportActionBar().setSubtitle("with GPS measurements");
            polylineOptions.color(Color.YELLOW);
        }

        //initializing measurements list
        String[] measurementStrings=favouriteData.getMeasurements().split(",");
        measurements=new ArrayList<>();
        for(String measurementString:measurementStrings){
            measurements.add(Double.valueOf(measurementString));
        }


        //initializing datapoints list
        String[] dataPointStrings=favouriteData.getDataPoints().split(",");
        dataPoints=new ArrayList<>();
        for(String dataPointString:dataPointStrings){
            String[] value=dataPointString.split("-");
            LatLng latLng=new LatLng(Double.valueOf(value[0]),Double.valueOf(value[1]));
            dataPoints.add(latLng);
        }


        mapFragment=(MapFragment)getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {
                map=googleMap;
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                map.getUiSettings().setCompassEnabled(true);
                if(ContextCompat.checkSelfPermission(FavouriteRouteViewActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
                    map.setMyLocationEnabled(true);
                }

                //The following variables are used when wifi plan path is selected
                Double minWifiSpeed=0.0;
                Double maxWifiSpeed=0.0;

                Double minDataSpeed=0.0;
                Double maxDataSpeed=0.0;

                if(type.equalsIgnoreCase(DashBoardActivity.DATA_TYPE_WIFI)){
                    minWifiSpeed=measurements.get(0);
                    maxWifiSpeed=measurements.get(0);
                    for(Double measurement:measurements){
                        Log.d("awesome","measurement: "+measurement);
                        if(minWifiSpeed>measurement){
                            minWifiSpeed=measurement;
                        }

                        if(maxWifiSpeed<measurement){
                            maxWifiSpeed=measurement;
                        }
                    }

                    Log.d("awesome","Got minWifiSpeed= "+minWifiSpeed+" and maxWifiSpeed= "+maxWifiSpeed);
                }else if(type.equalsIgnoreCase(DashBoardActivity.DATA_TYPE_MOBILE_DATA)){
                    minDataSpeed=measurements.get(0);
                    maxDataSpeed=measurements.get(0);
                    for(Double measurement:measurements){
                        if(minDataSpeed>measurement){
                            minDataSpeed=measurement;
                        }

                        if(maxDataSpeed<measurement){
                            maxDataSpeed=measurement;
                        }
                    }
                }

                for(LatLng point:dataPoints){
                    double maxLatitude=point.latitude+0.00062881;
                    double minLatitude=point.latitude-0.00062881;
                    double lngBuffer=0.07*(350/(Math.cos(deg2rad(point.latitude))*40075));
                    double maxLongitude=point.longitude+lngBuffer;
                    double minLongitude=point.longitude-lngBuffer;
                    gotoLocation(point.latitude,point.longitude);
                    Log.d("awesome","Drawing polygon for latitude: "+point.latitude+" longitude: "+point.longitude+" with maxLatitude: "+maxLatitude+" minLatitude: "+minLatitude+" maxLongitude: "+maxLongitude+" minLongitude: "+minLongitude);

                    Polygon polygon=map.addPolygon(new PolygonOptions().add(
                            new LatLng(minLatitude,minLongitude),
                            new LatLng(maxLatitude,minLongitude),
                            new LatLng(maxLatitude,maxLongitude),
                            new LatLng(minLatitude,maxLongitude)
                    ));

                    if(type.equalsIgnoreCase(DashBoardActivity.DATA_TYPE_NOISE)){
                        String alpha=Utils.getAlphaValue((int)Math.round(measurements.get(dataPoints.indexOf(point))));
                        Log.d("awesome","Got alpha value: "+alpha);
                        polygon.setFillColor(Utils.hex2Decimal(alpha+"FF0000"));
                        polygon.setStrokeColor(Utils.hex2Decimal("00000000"));
                    }else if(type.equalsIgnoreCase("wifi")){
                        Double x=measurements.get(dataPoints.indexOf(point));
                        if(x==minWifiSpeed){
                            x+=10.0;
                        }
                        Log.d("awesome","x: "+x);
                        int percentage=(int)Math.round((x-minWifiSpeed)*100/(maxWifiSpeed-minWifiSpeed));
                        Log.d("awesome","percentage: "+percentage);

                        String alpha=Utils.getAlphaValue(percentage);
                        Log.d("awesome","alpha: "+alpha);
                        polygon.setFillColor(Utils.hex2Decimal(alpha+"00FF00"));
                        polygon.setStrokeColor(Utils.hex2Decimal("00000000"));
                    }else if(type.equalsIgnoreCase("mobileData")){
                        Double x=measurements.get(dataPoints.indexOf(point));
                        if(x==minDataSpeed){
                            x+=10.0;
                        }
                        Log.d("awesome","x: "+x);
                        int percentage=(int)Math.round((x-minDataSpeed)*100/(maxDataSpeed-minDataSpeed));
                        Log.d("awesome","Percentage: "+percentage);

                        String alpha=Utils.getAlphaValue(percentage);
                        Log.d("awesome","alpha: "+alpha);
                        polygon.setFillColor(Utils.hex2Decimal(alpha+"0000FF"));
                        polygon.setStrokeColor(Utils.hex2Decimal("00000000"));
                    }
                }


                //initialising polyline options
                String[] polylinePointStrings=favouriteData.getPolyPoints().split(",");
                for(String polylinePointString:polylinePointStrings){
                    String[] value=polylinePointString.split("-");
                    LatLng latLng=new LatLng(Double.valueOf(value[0]),Double.valueOf(value[1]));
                    polylineOptions.add(latLng);
                }
                map.addPolyline(polylineOptions);

                List<LatLng> points=polylineOptions.getPoints();
                source=points.get(0);
                destination=points.get(points.size()-1);

                MarkerOptions sourceMarkerOptions = new MarkerOptions()
                        .title(favouriteData.getSource().substring(0,favouriteData.getSource().indexOf(",")))
                        .position(source)
                        .snippet(favouriteData.getSource().substring(favouriteData.getSource().indexOf(",")+1))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                map.addMarker(sourceMarkerOptions);

                MarkerOptions destinationMarkerOptions = new MarkerOptions()
                        .title(favouriteData.getDestination().substring(0,favouriteData.getDestination().indexOf(",")))
                        .position(destination)
                        .snippet(favouriteData.getDestination().substring(favouriteData.getDestination().indexOf(",")+1))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                map.addMarker(destinationMarkerOptions);
            }
        });
    }

    private double deg2rad(double degrees){
        return Math.PI*degrees/180.0;
    }

    private double rad2deg(double radians){
        return 180.0*radians/Math.PI;
    }

    private void gotoLocation(double lat, double lng) {

        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, 15);
        map.moveCamera(update);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}