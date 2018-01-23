package com.sensorapp;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class FavouritesViewActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener, GoogleMap.OnCameraMoveListener {

    ArrayList<LatLng> MarkerPoints;
    ImageView ivTrack, ivFab;
    MarkerOptions marker1, marker2;
    LatLng mLatLng;
    LocationRequest mLocationRequest;
    private static final double LATTITUDE = 20.5937;
    private static final double LONGITUDE = 78.9629;
    private static final float DEFULTZOOM = 5;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    String source, destination, avgNoise;
    String distance = "";
    String duration = "";
    Marker mCurrentLocationMarker;
    TextView tvDistance, noise;
    DatabaseHelper databaseHelper;
    CoordinatorLayout coordinatorLayout;
    ArrayList<String> list;
    DebugHelper debugHelper;
    private String algorithm;
    private ArrayList<PolylineOptions> lines = new ArrayList<>();
    private final int PATTERN_GAP_LENGTH_PX = 0;
    private final PatternItem DOT = new Dot();
    private final int PATTERN_DASH_LENGTH_PX = 20;
    private final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private final int POLYGON_STROKE_WIDTH_PX = 8;
    private final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private String color = "#FF00CCFF";
    private final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //connection check..
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        ConnectivityManager cm = (ConnectivityManager) getSystemService(FavouritesViewActivity.this.CONNECTIVITY_SERVICE);
        android.net.NetworkInfo wifi = cm
                .getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        android.net.NetworkInfo datac = cm
                .getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if ((wifi != null & datac != null)
                && (wifi.isConnected() | datac.isConnected())) {
        } else {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, "Check Your Internet Connection!!", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            finish();
                        }
                    });

            // Changing message text color
            snackbar.setActionTextColor(Color.RED);

            // Changing action button text color
            View sbView = snackbar.getView();
            TextView textView = (TextView) sbView.findViewById(android.support.design.R.id.snackbar_text);
            textView.setTextColor(Color.WHITE);
            snackbar.show();
        }

        if (googleServiceAvailable()) {

            setContentView(R.layout.activity_favourites_view);

        }

        //setting action bar
        ActionBar actionBar = getSupportActionBar();
        actionBar.setTitle("Favourites");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        Intent intent = getIntent();
        source = intent.getStringExtra("source");
        destination = intent.getStringExtra("destination");
        avgNoise = intent.getStringExtra("avgNoise");
        algorithm = intent.getStringExtra("algorithm");

        //   Log.e("source", source);
        //  Log.e("destination", destination);

        tvDistance = (TextView) findViewById(R.id.tvDistance);
        noise = (TextView) findViewById(R.id.tvAvgNoise);

        //initilizing
        MarkerPoints = new ArrayList<>();

        databaseHelper = new DatabaseHelper(this);
        debugHelper = new DebugHelper();

        noise.setText(avgNoise + " db");

        //SupportMapFregement object create
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);


    }

    //checking device is supported google services or not..
    private boolean googleServiceAvailable() {

        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(getApplicationContext(), "Cant connect to play service", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    //setting the defult map

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        geoLocate();
        mMap.setMapType(googleMap.MAP_TYPE_HYBRID);
        mMap.setOnCameraMoveListener(this);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }
    }

    //connecting the google api client..
    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    //geting location and ploat the path..
    private void geoLocate() {

        Geocoder gc = new Geocoder(this);// creating geocoder object..
        try {
            if (mCurrentLocationMarker != null) {
                mCurrentLocationMarker.remove();
            }
            List<Address> list = gc.getFromLocationName(source, 1);//geting source address
            List<Address> list1 = gc.getFromLocationName(destination, 1);// getting destination address

            Address add = list.get(0);            //initilizing source address
            Address add1 = list1.get(0);         //initilizing destination address.
            double latd = add1.getLatitude();   //get latttitued from address
            double lngd = add1.getLongitude();  //get longitude from address
            double lat = add.getLatitude();    //get latttitued from address
            double lng = add.getLongitude();   //get longitude from address


            //stop location updates
            if (mGoogleApiClient != null) {
                LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this);
            }

            gotoLocation(lat, lng, 14);

            marker1 = new MarkerOptions()
                    .title(source)
                    .position(new LatLng(lat, lng))
                    .snippet("source")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mMap.addMarker(marker1);
            mLatLng = new LatLng(lat, lng);


            // Getting URL to the Google Directions API
            String url = getUrl(lat, lng, latd, lngd);
            FetchUrl fetchUrl = new FetchUrl();
            // Start downloading json data from Google Directions API
            fetchUrl.execute(url);
            //setting marker..
            marker2 = new MarkerOptions()
                    .title(destination)
                    .position(new LatLng(latd, lngd))
                    .snippet("Destination")
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
            mMap.addMarker(marker2);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

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

    /**
     * A method to download json data from url
     */
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
    public void onCameraMove() {

        debugHelper.drawDebugGrid(mMap, 3.5f);

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
            List<LatLng> first;
            List<LatLng> second;
            List<LatLng> third;
            List<LatLng> fourth;
            List<LatLng> fifth;
            List<LatLng> six;
            PolylineOptions lineOptions = null;
            PolylineOptions lineOptions1 = null;
            PolylineOptions lineOptions3 = null;
            PolylineOptions lineOptions4 = null;
            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();
                lineOptions1 = new PolylineOptions();
                lineOptions3 = new PolylineOptions();
                lineOptions4 = new PolylineOptions();


                // Fetching i-th route
                List<HashMap<String, String>> path = result.get(i);

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
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }

                PolylineOptions options = new PolylineOptions();
                options.width(10);
                options.color(Color.parseColor("#4333FF"));
                options.addAll(points);

                lines.add(options);
            }
            first = points.subList(0, points.size() / 2);   //brek the array list of point in two parts
            second = points.subList((points.size() / 2) - 1, points.size());

            third = first.subList(0, first.size() / 2);      //breaking the array list of first in two parts. third and fourth.
            fourth = first.subList(first.size() / 2 - 1, first.size());
            fifth = second.subList(0, second.size() / 2);       //breaking the array list of second in two parts. fifth and six.
            six = second.subList(second.size() / 2 - 1, second.size());

//            lineOptions.addAll(third);      //adding all latlong of third list in line option
//            lineOptions1.addAll(fourth);     //adding all latlong of fourth list in line option
//            lineOptions3.addAll(fifth);     //adding all latlong of fifth list in line option
//            lineOptions4.addAll(six);       //adding all latlong of six list in line option
//
//            lineOptions.width(12);          //setting line  width
//            lineOptions1.width(12);
//            lineOptions3.width(12);
//            lineOptions4.width(12);
//
//            lineOptions.color(Color.parseColor("#76FF03"));     //setting line color..
//            lineOptions1.color(Color.parseColor("#02ff7c"));
//            lineOptions3.color(Color.parseColor("#e21b47"));
//            lineOptions4.color(Color.parseColor("#e21b1b"));

            tvDistance.setText(distance);                   //setting distance

            // Drawing polyline in the Google Map for the i-th route
            if (lineOptions != null) {
//                mMap.addPolyline(lineOptions);
//                mMap.addPolyline(lineOptions1);
//                mMap.addPolyline(lineOptions3);
//                mMap.addPolyline(lineOptions4);


                if (algorithm == "A") {
                    Polyline polyline = mMap.addPolyline(lines.get(0));
                    polyline.setPattern(PATTERN_POLYLINE_DOTTED);
                    polyline.setColor(Color.parseColor(color));
                    polyline.setWidth(20);
                } else {
                    for (int i = 0; i < lines.size(); i++) {
                        mMap.addPolyline(lines.get(i));
                    }
                }
            } else {
                Log.d("onPostExecute", "without Polylines drawn");
            }
        }
    }


    //Set Multiple types of map
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.locationmap, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.mapTypeNormal:
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.mapTypeSatellite:
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;
            case R.id.mapTypeTerrain:
                mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                break;
            case R.id.mapTypeHybrid:
                mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;

            default:
                break;
        }
        if (android.R.id.home == item.getItemId()) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mGoogleApiClient != null &&
                ContextCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }


}
