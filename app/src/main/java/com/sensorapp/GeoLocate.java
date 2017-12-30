package com.sensorapp;

import android.content.Context;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.maps.android.SphericalUtil;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by ak on 8/27/17.
 */

public class GeoLocate implements LocationListener {
    private GoogleMap mMap;
    private Context context;
    private GoogleApiClient mGoogleApiClient;
    private LatLngBounds initialBounds;
    private String distance;
    private String duration;
    private ArrayList<HashMap<Double, Double>> latlang;

    public GeoLocate(GoogleMap mMap, Context context, GoogleApiClient mGoogleApiClient) {
        this.mMap = mMap;
        this.context = context;
        this.mGoogleApiClient = mGoogleApiClient;
    }

    public void geoLocate(String source, String destination, List<Address> listTEmp, List<Address> list1Temp) {

        Geocoder gc = new Geocoder(context);
        try {
//            if (mCurrentLocationMarker != null) {
//                mCurrentLocationMarker.remove();
//            }

            //getting lat lang from address..
            List<Address> list = listTEmp;
            List<Address> list1 = list1Temp;

            Address add = list.get(0);
            Address add1 = list1.get(0);
            double latd = add1.getLatitude();
            double lngd = add1.getLongitude();
            double lat = add.getLatitude();
            double lng = add.getLongitude();

            //stop location updates
            if (mGoogleApiClient != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }


            MarkerOptions marker1 = new MarkerOptions()
                    .title(source)
                    .position(new LatLng(lat, lng))
                    .snippet("source")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMap.addMarker(marker1).showInfoWindow();
            LatLng mLatLng = new LatLng(lat, lng);


            // Getting URL to the Google Directions API
            String url = getUrl(lat, lng, latd, lngd);
            FetchUrl fetchUrl = new FetchUrl();
            // Start downloading json data from Google Directions API
            fetchUrl.execute(url);

            MarkerOptions marker2 = new MarkerOptions()
                    .title(destination)
                    .position(new LatLng(latd, lngd))
                    .snippet("Destination")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mMap.addMarker(marker2).showInfoWindow();
            gotoLocation(latd, lngd, 5);
//            goToLocate(5);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void geoLocate(String source, String destination) {

        Geocoder gc = new Geocoder(context);
        try {
//            if (mCurrentLocationMarker != null) {
//                mCurrentLocationMarker.remove();
//            }

            //getting lat lang from address..
            List<Address> list = gc.getFromLocationName(source, 1);
            List<Address> list1 = gc.getFromLocationName(destination, 1);

            Address add = list.get(0);
            Address add1 = list1.get(0);
            double latd = add1.getLatitude();
            double lngd = add1.getLongitude();
            double lat = add.getLatitude();
            double lng = add.getLongitude();

            //stop location updates
            if (mGoogleApiClient != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }


            MarkerOptions marker1 = new MarkerOptions()
                    .title(source)
                    .position(new LatLng(lat, lng))
                    .snippet("source")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMap.addMarker(marker1).showInfoWindow();
            LatLng mLatLng = new LatLng(lat, lng);


            // Getting URL to the Google Directions API
            String url = getUrl(lat, lng, latd, lngd);
            FetchUrl fetchUrl = new FetchUrl();
            // Start downloading json data from Google Directions API
            fetchUrl.execute(url);

            MarkerOptions marker2 = new MarkerOptions()
                    .title(destination)
                    .position(new LatLng(latd, lngd))
                    .snippet("Destination")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mMap.addMarker(marker2).showInfoWindow();
            gotoLocation(latd, lngd, 5);
//            goToLocate(5);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    //Get Url From Google server
    private String getUrl(double lat, double lng, double latd, double lngd) {
        // Origin of route
        String alternatives = "&alternatives=true";

        String str_origin = "origin=" + lat + "," + lng;
        // Destination of route
        String str_dest = "destination=" + latd + "," + lngd;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + alternatives;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
    }

    //set Camera on location
    private void gotoLocation(double lat, double lng, float zoom) {

        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mMap.moveCamera(update);
    }

    private String downloadUrl(String strUrl) throws IOException {
        String data = "";
        InputStream iStream = null;
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(strUrl);
            Log.e("url", strUrl);
            // Creating an http connection to communicate with url
            urlConnection = (HttpURLConnection) url.openConnection();
            // Connecting to url
            urlConnection.connect();
            // Reading data from url
            iStream = urlConnection.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuffer sb = new StringBuffer();

            String line = "";
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            Log.e("downloadUrl", data.toString());
            br.close();

        } catch (Exception e) {
            Log.e("Exception", e.toString());
        } finally {
            iStream.close();
            urlConnection.disconnect();
        }
        return data;
    }

    @Override
    public void onLocationChanged(Location location) {

    }


    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try {
                // Fetching the data from web service
                Log.e("download Url", "download");
                data = downloadUrl(url[0]);
                Log.d("Background Task data", data.toString());
            } catch (Exception e) {
                Log.d("Background Task", e.toString());
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            ParserTask parserTask = new ParserTask();
            // Invokes the thread for parsing the JSON data
            parserTask.execute(result);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {
        // Parsing the data in non-ui thread

        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = null;

            try {
                jObject = new JSONObject(jsonData[0]);

                //Call DataParser Here
                DataParser parser = new DataParser();
                // Starts parsing data
                routes = parser.parse(jObject);

            } catch (Exception e) {
                Log.d("ParserTask", e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            List<LatLng> points = null;
            List<LatLng> first = null;
            List<LatLng> second = null;
            List<LatLng> third = null;
            List<LatLng> fourth = null;
            List<LatLng> fifth = null;
            List<LatLng> six = null;
            ArrayList<PolylineOptions> lines = new ArrayList<>();
            PolylineOptions lineOptions = null;
            PolylineOptions lineOptions1 = null;
            PolylineOptions lineOptions3 = null;
            PolylineOptions lineOptions4 = null;
            LatLng position;
//            Polygon polygon;
            PolygonOptions polygonOptions = null;
            // Create a LatLngBounds.Builder and include your positions
            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            // Traversing through all the routes

            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                lineOptions1 = new PolylineOptions();
                lineOptions3 = new PolylineOptions();
                lineOptions4 = new PolylineOptions();
                polygonOptions = new PolygonOptions();
                latlang = new ArrayList<HashMap<Double, Double>>();
                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);
                double dist = 10.52;

                // Fetching all the points in i-th route
                for (int j = 0; j < path.size(); j++) {
                    HashMap<String, String> point = path.get(j);

                    //Distance & Duration
                    if (j == 0) {
                        distance = (String) point.get("distance");
                        continue;
                    } else if (j == 1) {
                        duration = (String) point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    // tmp hashmap for single contact
                    HashMap<Double, Double> allItem = new HashMap<Double, Double>();
                    allItem.put(lat, lng);
                    latlang.add(allItem);
                    position = new LatLng(lat, lng);

                    points.add(position);
                    builder.include(position);
                    polygonOptions = new PolygonOptions()
                            .add(position);
                }

                // Calculate the bounds of the initial positions
                initialBounds = builder.build();

// Increase the bounds by the given distance
// Notice the distance * Math.sqrt(2) to increase the bounds in the directions of northeast and southwest (45 and 225 degrees respectively)
                LatLng targetNorteast = SphericalUtil.computeOffset(initialBounds.northeast, dist * Math.sqrt(2), 45);
                LatLng targetSouthwest = SphericalUtil.computeOffset(initialBounds.southwest, dist * Math.sqrt(2), 225);

// Add the new positions to the bounds
                builder.include(targetNorteast);
                builder.include(targetSouthwest);
                PolylineOptions options = new PolylineOptions();
                options.width(8);
                options.color(Color.parseColor("#4333FF"));
                options.addAll(points);
                lines.add(options);
            }

            // Log.e("points", points.toString());

            //dividing latlang list in four equal parts and add all in lineoption..
            try {
                first = points.subList(0, points.size() / 2);
                third = first.subList(0, first.size() / 2);
                fourth = first.subList((first.size() / 2) - 1, first.size());
                second = points.subList((points.size() / 2) - 1, points.size());
                fifth = second.subList(0, second.size() / 2);
                six = second.subList((second.size() / 2) - 1, second.size());

                polygonOptions.addAll(points);
                polygonOptions.strokeColor(Color.BLUE);
                polygonOptions.strokeWidth(7);
                polygonOptions.fillColor(Color.CYAN);

                lineOptions.addAll(points);
//                lineOptions.addAll(third);
//                lineOptions1.addAll(fourth);
//                lineOptions3.addAll(fifth);
//                lineOptions4.addAll(six);
                lineOptions.width(8);
                lineOptions1.width(8);
                lineOptions3.width(8);
                lineOptions4.width(8);
                lineOptions.color(Color.parseColor("#4333FF"));
                lineOptions1.color(Color.parseColor("#4333FF"));
                lineOptions3.color(Color.parseColor("#4333FF"));
                lineOptions4.color(Color.parseColor("#4333FF"));

            } catch (Exception ex) {
                ex.printStackTrace();
            }


//            tvDistance.setText(distance); //setting distance.
//            if (!"Noise".equals(getIntent().getStringExtra("Noise"))) {
//                if ("GPS".equals(getIntent().getStringExtra("GPS"))) {
//
//                    avgNoise.setText("Duration");
//                    noise.setText(duration);
//                } else {
//
//                    WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
//                    TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
//                    //checking mobile data is enable or not..
//                    ConnectivityManager cm = (ConnectivityManager) getSystemService(PlanPathViewActivity.this.CONNECTIVITY_SERVICE);
//                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//                    PlanPathViewActivity.myPhoneStateListener psListener = new PlanPathViewActivity.myPhoneStateListener();
//
//                    if (wifiManager.isWifiEnabled()) {
//
//                        avgNoise.setText("Signal");
//                        // Level of current connection
//                        int RSSI = wifiManager.getConnectionInfo().getRssi();
//                        int status = WifiManager.calculateSignalLevel(RSSI, 5);
//                        noise.setText("" + status);
//
//                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
//
//                        avgNoise.setText("Signal");
//                        tm.listen(psListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
//                    }
//
//                }
//
//            }

//            ContentValues values = new ContentValues();
//            values.put("data", new JSONObject(latlang).toString());
//            Log.e("json",values.toString());

            // Drawing polyline in the Google Map for the i-th route   https://www.facebook.com/elena.esenina.9/videos/1757874740907272/
            if (lineOptions != null) {
//                drawBounds(initialBounds, Color.parseColor(setColorAlpha(15, "#A2F75D")));
//                mMap.addPolygon(polygonOptions);
                for (int i = 0; i < lines.size(); i++) {
                    mMap.addPolyline(lines.get(i));
                }
//                mMap.addPolyline(lineOptions1);
//                mMap.addPolyline(lineOptions3);
//                mMap.addPolyline(lineOptions4);
                //debugHelper.drawArea(initialBounds, mMap, lineOptions, lineOptions1, lineOptions3, lineOptions4);
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }

//            goToLocate(15);
        }
    }

    // draw a rectangle view..
    private void drawBounds(LatLngBounds bounds, int color) {

        PolygonOptions polygonOptions = new PolygonOptions()
                .add(new LatLng(bounds.northeast.latitude, bounds.northeast.longitude))
                .add(new LatLng(bounds.southwest.latitude, bounds.northeast.longitude))
                .add(new LatLng(bounds.southwest.latitude, bounds.southwest.longitude))
                .add(new LatLng(bounds.northeast.latitude, bounds.southwest.longitude))
                .strokeColor(color).strokeWidth(3.5f).fillColor(Color.parseColor(setColorAlpha(15, "#A2F75D")));

//        mMap.addPolygon(polygonOptions);

    }

    // setting provided color tranceperancy ..
    public static String setColorAlpha(int percentage, String colorCode) {
        double decValue = ((double) percentage / 100) * 255;
        String rawHexColor = colorCode.replace("#", "");
        StringBuilder str = new StringBuilder(rawHexColor);

        if (Integer.toHexString((int) decValue).length() == 1)
            str.insert(0, "#0" + Integer.toHexString((int) decValue));
        else
            str.insert(0, "#" + Integer.toHexString((int) decValue));
        return str.toString();
    }

}
