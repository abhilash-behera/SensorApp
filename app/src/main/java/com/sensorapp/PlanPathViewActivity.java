package com.sensorapp;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
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
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import es.dmoral.toasty.Toasty;


public class PlanPathViewActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener, View.OnClickListener, GoogleMap.OnCameraMoveListener {
    ArrayList<LatLng> MarkerPoints;
    FloatingActionButton ivTrack, ivFab;
    MarkerOptions marker1, marker2;
    LatLng mLatLng;
    LocationRequest mLocationRequest;
    private static final double LATTITUDE = 35.039045;
    private static final double LONGITUDE = 33.136621;
    double zoom;
    private static final float DEFULTZOOM = 6;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    String source, destination, noiseDb;
    String distance = "";
    String duration = "";
    Marker mCurrentLocationMarker;
    TextView tvDistance, noise, avgNoise;
    DatabaseHelper databaseHelper;
    ArrayList<FavouriteData> favouritesList;
    CoordinatorLayout coordinatorLayout;
    ArrayList<String> list;
    DebugHelper debugHelper;
    LatLngBounds.Builder builder;
    LatLngBounds initialBounds = null;
    TextView tvBfs, tvA;
    String id;
    int i = 1;
    Intent intent;
    ArrayList<HashMap<Double, Double>> latlang;
    private boolean isFirstTime = false;
    private Marker mark1;
    private Marker mark2;
    private ArrayList<Polyline> polyLines;
    private ArrayList<PolylineOptions> lines;
    private static int lineIndex = 0;
    private final int PATTERN_GAP_LENGTH_PX = 0;
    private final PatternItem DOT = new Dot();
    private final int PATTERN_DASH_LENGTH_PX = 20;
    private final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private final int POLYGON_STROKE_WIDTH_PX = 8;
    private final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    private String color = "#FF00CCFF";
    private ArrayList<NoiseData> noiseList;
    //
// Create a stroke pattern of a gap followed by a dot.
    private final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    private ArrayList<String> positionsList = null;
    String ALGORITHM = "BFS";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        polyLines = new ArrayList<>();
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        checkInternetConnection();
    }

    private void checkInternetConnection(){
        if (Utils.isNetworkAvailable(PlanPathViewActivity.this)) {
            //checking google service is available or not..
            if (googleServiceAvailable()) {
                setContentView(R.layout.activity_plan_path_view);

                //setting action bar..
                ActionBar actionBar = getSupportActionBar();
                actionBar.setTitle("Path");
                actionBar.setDisplayHomeAsUpEnabled(true);
                actionBar.setHomeButtonEnabled(true);

                builder = new LatLngBounds.Builder();
                intent = getIntent();
                source = intent.getStringExtra("source");
                destination = intent.getStringExtra("destination");
                positionsList = intent.getStringArrayListExtra("positions_list");
                id = intent.getStringExtra("id");

                Log.d("awesome","PlanPathViewActivity: source: "+source+" destination: "+destination+" id: "+id);

                initializeViews();
                continueExecution();

            }else{
                Toasty.error(PlanPathViewActivity.this,"Google Play Services not found on your device",Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Snackbar snackbar = Snackbar.make(coordinatorLayout, "No internet connection", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkInternetConnection();
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
    }

    private void continueExecution(){
        //initilizing
        MarkerPoints = new ArrayList<>();
        favouritesList = new ArrayList<FavouriteData>();
        /*debugHelper = new DebugHelper(this);*/
        databaseHelper = new DatabaseHelper(this);
        list = new ArrayList<String>();
        //list = databaseHelper.getAudioDb();
        noiseList=databaseHelper.getNoiseData();
        /*Log.e("List Audio", list.toString());
        if (list != null) {
            try {
                noiseDb = list.get(list.size() - 1);
                noise.setText(noiseDb + " db");
            } catch (Exception ex) {
                ex.printStackTrace();
            }

        }
        Log.e("List Db=", list.toString());*/

        //SupportMapFregement object create
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);
    }

    private void initializeViews(){
        //find out views..
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        avgNoise = (TextView) findViewById(R.id.AvgNoise);
        noise = (TextView) findViewById(R.id.tvAvgNoise);
        tvBfs = (TextView) findViewById(R.id.tvBfs);
        tvA = (TextView) findViewById(R.id.tvA);

        ivTrack = (FloatingActionButton) findViewById(R.id.ivTrack);
        ivFab = (FloatingActionButton) findViewById(R.id.ivFab);
        ivTrack.setOnClickListener(this);
        ivFab.setOnClickListener(this);
        tvBfs.setOnClickListener(this);
        tvA.setOnClickListener(this);
    }

    public boolean googleServiceAvailable() {
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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap = googleMap;
        // gotoLocation(LATTITUDE, KEY_LONGITUDE, DEFULTZOOM);
        mMap.setMapType(googleMap.MAP_TYPE_HYBRID);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnCameraMoveListener(this);

        debugHelper.drawDebugGrid(mMap, 3.5f);
     //}

        /*geoLocate(source, destination);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        }*/
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }


    public void geoLocate(String source, String destination) {

        Geocoder gc = new Geocoder(this);
        try {
            if (mCurrentLocationMarker != null) {
                mCurrentLocationMarker.remove();
            }
            List<Address> list;
            List<Address> list1;
            //getting lat lang from address..
            if (positionsList == null) {
                list = gc.getFromLocationName(source, 1);
                list1 = gc.getFromLocationName(destination, 1);
            } else {
                double latitude = Double.parseDouble(positionsList.get(0));
                double longitude = Double.parseDouble(positionsList.get(1));
                double latitude1 = Double.parseDouble(positionsList.get(2));
                double longitude1 = Double.parseDouble(positionsList.get(3));
                list = gc.getFromLocation(latitude, longitude, 1);
                list1 = gc.getFromLocation(latitude1, longitude1, 1);
            }
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


            marker1 = new MarkerOptions()
                    .title("Source")
                    .position(new LatLng(lat, lng))
                    .snippet(source)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            mark1 = mMap.addMarker(marker1);
            mark1.showInfoWindow();
            mLatLng = new LatLng(lat, lng);


            // Getting URL to the Google Directions API
            String url = getUrl(lat, lng, latd, lngd);
            FetchUrl fetchUrl = new FetchUrl();
            // Start downloading json data from Google Directions API
            fetchUrl.execute(url);

            marker2 = new MarkerOptions()
                    .title("Destination")
                    .position(new LatLng(latd, lngd))
                    .snippet(destination)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
            mark2 = mMap.addMarker(marker2);
            mark2.showInfoWindow();
            gotoLocation(latd, lngd, 5);
            goToLocate(5);

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

    @Override
    public void onConnected(Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }

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
            lines = new ArrayList<>();
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
                options.width(10);
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


            tvDistance.setText(distance); //setting distance.
            if (!"Noise".equals(getIntent().getStringExtra("Noise"))) {
                if ("GPS".equals(getIntent().getStringExtra("GPS"))) {

                    avgNoise.setText("Duration");
                    noise.setText(duration);
                } else {

                    WifiManager wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                    //checking mobile data is enable or not..
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(PlanPathViewActivity.this.CONNECTIVITY_SERVICE);
                    NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
                    myPhoneStateListener psListener = new myPhoneStateListener();

                    if (wifiManager!=null&&wifiManager.isWifiEnabled()) {

                        avgNoise.setText("Signal");
                        // Level of current connection
                        int rssi = wifiManager.getConnectionInfo().getRssi();
                        int status = WifiManager.calculateSignalLevel(rssi, 5);
                        noise.setText("" + status);

                    } else if (activeNetwork.getType() == ConnectivityManager.TYPE_MOBILE) {
                        avgNoise.setText("Signal");
                        tm.listen(psListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                    }

                }

            }

//            ContentValues values = new ContentValues();
//            values.put("data", new JSONObject(latlang).toString());
//            Log.e("json",values.toString());

            // Drawing polyline in the Google Map for the i-th route   https://www.facebook.com/elena.esenina.9/videos/1757874740907272/
            if (lineOptions != null) {
                drawBounds(initialBounds, Color.parseColor(setColorAlpha(15, "#A2F75D")));
//                mMap.addPolygon(polygonOptions);
//                for (int i = 0; i < lineIndex; i++) {
                if (lineIndex == 0) {
                    polyLines.add(mMap.addPolyline(lines.get(lineIndex)));
                    polyLines.get(0).setPattern(PATTERN_POLYLINE_DOTTED);
                    polyLines.get(0).setColor(Color.parseColor(color));
                    polyLines.get(0).setWidth(20);
                } else {
                    for (int i = 0; i < lines.size(); i++) {
                        polyLines.add(mMap.addPolyline(lines.get(i)));
                    }
                }

//                }
//                polyLines.get(0).setPattern(PATTERN_POLYLINE_DOTTED);
//                polyLines.get(0).setColor(Color.parseColor("#FF6B8B9F"));
//                polyLines.get(0).setWidth(20);

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
        mMap.addPolygon(polygonOptions);
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
                        Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        }
    }

    @Override
    public void onClick(View view) {

        if (view == ivTrack) {

        }
        if (view == ivFab) {
            final ProgressDialog dilog;
            //inserting record in sqlite..database.
            dilog = new ProgressDialog(PlanPathViewActivity.this);
            dilog.setMessage("processing...");
            dilog.setCanceledOnTouchOutside(false);
            //   dilog.show();
            Task.callInBackground(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    boolean inserted = databaseHelper.insertFabData(source, destination, distance, noiseDb, ALGORITHM);
                    if (inserted) {
                        //    boolean check = databaseHelper.saveLatLang(latlang);
                        dilog.dismiss();

                    }
                    return false;
                }
            }).continueWith(new Continuation<Boolean, Object>() {
                @Override
                public Object then(Task<Boolean> task) throws Exception {
                    Toast.makeText(PlanPathViewActivity.this, "Congratulation ! Make Favourite Success.", Toast.LENGTH_SHORT).show();
                    return null;
                }
            }, Task.UI_THREAD_EXECUTOR);

        }
        if (view == tvA) {
            ALGORITHM = "A";
//            favouritesList=databaseHelper.getAllFabList();
//            Log.e("List",favouritesList.toString());
//            for (int i=0;i<favouritesList.size();i++){
//                String sId=favouritesList.get(i).getSource();
//                String dId=favouritesList.get(i).getDestination();
//                Log.e("S,D",""+sId+","+dId);
//                geoLocate(sId,dId);
//            }
//            ArrayList<LatLng> mylist = new ArrayList<>();
//            mylist = databaseHelper.getLatLang();
//
//            Log.e("MyList", mylist.toString());
//            PolylineOptions linPolylineOptions = new PolylineOptions();
//            linPolylineOptions.addAll(mylist);
//            linPolylineOptions.width(8);
//            linPolylineOptions.color(Color.parseColor("#76FF03"));
//            mMap.clear();
//            mMap.addPolyline(linPolylineOptions);
//            goToLocate(10);
            //   if (isFirstTime) {
//                lineIndex = 0;
//                isFirstTime = false;
//                geoLocate(source, destination);
            //  } else {
            if (lineIndex != 0) {
                lineIndex = 0;
                for (int j = 0; j < polyLines.size(); j++) {
                    polyLines.get(j).remove();
                }
                polyLines.clear();
                polyLines.add(mMap.addPolyline(lines.get(0)));
                polyLines.get(0).setPattern(PATTERN_POLYLINE_DOTTED);
                polyLines.get(0).setColor(Color.parseColor(color));
                polyLines.get(0).setWidth(20);
            }
            //  }
        }

        if (view == tvBfs) {
            ALGORITHM = "BFS";

            //   favouritesList = databaseHelper.getAllFabList();
            //   Log.e("List", favouritesList.toString());
//            for (int i=0;i<favouritesList.size();i++) {
//                String sId = favouritesList.get(i).getSource();
//                String dId = favouritesList.get(i).getDestination();
//                Log.e("S,D", "" + sId + "," + dId);
//
//            }
//            if (isFirstTime) {
//                lineIndex = 1;
//                isFirstTime = false;
//                geoLocate(source, destination);
//            } else {
            if (lineIndex != 1) {
                lineIndex = 1;
                for (int j = 0; j < polyLines.size(); j++) {
                    polyLines.get(j).remove();
                }
                polyLines.clear();
                for (int j = 0; j < lines.size(); j++) {
                    polyLines.add(mMap.addPolyline(lines.get(j)));
                }
            }
            //  }
        }
    }

    @Override
    public void onCameraMove() {
        debugHelper.drawDebugGrid(mMap, 3.5f);
    }

    private void goToLocate(int zoom) {

        // Gets screen size
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        // Calls moveCamera passing screen size as parameters
        try {
            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(initialBounds, width, height, zoom));
            i = 0;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public class myPhoneStateListener extends PhoneStateListener {
        public int signalSupport = 0;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            signalSupport = signalStrength.getGsmSignalStrength();
            Log.d(getClass().getCanonicalName(), "------ gsm signal --> " + signalSupport);

            if (signalSupport > 30) {
                Log.e(getClass().getCanonicalName(), "Signal GSM : Good");
                noise.setText("5");

            } else if (signalSupport > 20 && signalSupport < 30) {
                Log.e(getClass().getCanonicalName(), "Signal GSM : Avarage");
                noise.setText("4");

            } else if (signalSupport < 20 && signalSupport > 3) {
                Log.e(getClass().getCanonicalName(), "Signal GSM : Week");
                noise.setText("3");

            } else if (signalSupport < 3) {
                Log.e(getClass().getCanonicalName(), "Signal GSM : Very week");
                noise.setText("2");

            }
        }
    }
    //dividing list in given no of parts...
//    public static <T>List<List<T>> chopIntoParts( final List<T> ls, final int iParts )
//    {
//        final List<List<T>> lsParts = new ArrayList<List<T>>();
//        final int iChunkSize = ls.size() / iParts;
//        int iLeftOver = ls.size() % iParts;
//        int iTake = iChunkSize;
//
//        for( int i = 0, iT = ls.size(); i < iT; i += iTake )
//        {
//            if( iLeftOver > 0 )
//            {
//                iLeftOver--;
//
//                iTake = iChunkSize + 1;
//            }
//            else
//            {
//                iTake = iChunkSize;
//            }
//
//            lsParts.add( new ArrayList<T>( ls.subList( i, Math.min( iT, i + iTake ) ) ) );
//        }
//
//        return lsParts;
//    }


}
