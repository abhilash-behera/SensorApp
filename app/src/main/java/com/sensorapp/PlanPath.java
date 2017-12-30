package com.sensorapp;

import android.Manifest;
import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

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
import java.util.Locale;

import es.dmoral.toasty.Toasty;

public class PlanPath extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener,View.OnClickListener,GoogleMap.OnCameraMoveListener {

    GPSTracker gpsTracker;
    private GoogleMap mMap;
    ArrayList<LatLng> MarkerPoints;
    double lat, lang,latSource,langSource,latDestination,langDestination;
    String source,destination,noiseDb;
    double zoom;
    MarkerOptions markerS,markerG,markerD;
    GoogleApiClient mGoogleApiClient;
    protected LocationManager locationManager;
    Handler handler;
    CoordinatorLayout coordinatorLayout;
    String totalDistance="";
    String distance="";
    String duration="";
    Geocoder geocoder;
    TextView tvDistance,tvAvgNoise;
    FloatingActionButton ivTrack,ivFab;
    DatabaseHelper databaseHelper;
    ArrayList<String> list;
    DebugHelper debugHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.coordinatorLayout);
        // network connection check..
        ConnectivityManager cm = (ConnectivityManager) getSystemService(PlanPath.this.CONNECTIVITY_SERVICE);
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
                        public void onClick(View view)
                        {
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
        //setting action bar..
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Path");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        //getting gpstracker instance

        gpsTracker = new GPSTracker(PlanPath.this);
        geocoder=new Geocoder(this, Locale.getDefault());

        // Initializing
        MarkerPoints = new ArrayList<>();

        if (!gpsTracker.canGetLocation()) {

            gpsTracker.showSettingsAlert();

        }
        // checking google service ic available or not
        if (googleServiceAvailable()) {

            setContentView(R.layout.activity_plan_path);

        }

        handler=new Handler(); //creating handler object..

        // find out views..
        tvDistance=(TextView)findViewById(R.id.tvDistance);
        tvAvgNoise=(TextView)findViewById(R.id.tvAvgNoise);
        ivTrack=(FloatingActionButton) findViewById(R.id.ivTrack);
        ivFab=(FloatingActionButton) findViewById(R.id.ivFab);
        ivTrack.setOnClickListener(this);
        ivFab.setOnClickListener(this);

        //getting lat lang from gps...
        lat = gpsTracker.getLatitude();
        lang = gpsTracker.getLongitude();
        //getting source and distination from before activity or intent..
        source=getIntent().getStringExtra("source");
        destination=getIntent().getStringExtra("destination");

        debugHelper=new DebugHelper(this);

        //creating geocoder object..
        Geocoder gc=new Geocoder(this);
        try {
            //getting lat lang from address..
            List<Address> list = gc.getFromLocationName(source, 1);
            List<Address> list1 = gc.getFromLocationName(destination, 1);

            Address add = list.get(0);
            Address add1 = list1.get(0);
            latSource = add.getLatitude();
            langSource = add.getLongitude();
            latDestination = add1.getLatitude();
            langDestination = add1.getLongitude();

        }catch (Exception ex){
            ex.printStackTrace();
        }

        //getting instance of sqlite database..
        databaseHelper=new DatabaseHelper(this);
        list=new ArrayList<String>();

        //infleating map..
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

    }

    //checking google servie is availavel or not..
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

    @Override
    public void onMapReady(GoogleMap googleMap) {

        mMap=googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        mMap.setOnCameraMoveListener(this);
        setMarker();

        //Initialize Google Play Services
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);

            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        }


    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    //setting marker on map ..
    private void setMarker() {

        markerS = new MarkerOptions()
                .title(source)
                .position(new LatLng(latSource, langSource))
                .snippet("source")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                gotoLocation(latSource,langSource,14);
        mMap.addMarker(markerS);


        markerD = new MarkerOptions()
                .title(destination)
                .position(new LatLng(latDestination, langDestination))
                .snippet("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                gotoLocation(latDestination,langDestination,14);
        mMap.addMarker(markerD);

    }

    //move the camera to marker..
    private void gotoLocation(double lat, double lang, int zoom) {

        LatLng ll = new LatLng(lat, lang);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mMap.moveCamera(update);

    }

    @Override
    public void onConnected(Bundle bundle) {
        //generating url for geting all optimize lat lang between source ande destination..
        String url = getUrl(latSource,langSource,latDestination,langDestination);
        FetchUrl fetchUrl = new FetchUrl();
        // Start downloading json data from Google Directions API
        fetchUrl.execute(url); //execute the class FetchUrl to get all lat lang..
        handler.post(updateLocation); //handler to update location contoinusely..

        String url1 = getUrl(lat,lang,latDestination,langDestination);
        FetchUrl fetchUrl1 = new FetchUrl();
        fetchUrl1.execute(url1);


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
    Runnable updateLocation = new Runnable() {
        @Override
        public void run() {

            if (gpsTracker.canGetLocation()) //checking gps is on or off..
            {
                lat=gpsTracker.getLatitude();       //get latitude and longitude from gps..
                lang=gpsTracker.getLongitude();
                List<Address> addresses;
                String city="";

                try{

                    addresses = geocoder.getFromLocation(lat, lang, 1);
                    city =addresses.get(0).getAddressLine(0);
                }catch (Exception ex){
                    ex.printStackTrace();
                }

                markerG=new MarkerOptions()
                        .title(city).position(new LatLng(lat,lang))
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_CYAN))
                        .draggable(true);
                mMap.addMarker(markerG);
                LatLng latLng=new LatLng(lat,lang);

                handler.postDelayed(this, 6000); //update location on 6000 mili sec..

                //move map camera
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
                mMap.animateCamera(CameraUpdateFactory.zoomTo(14));


            }
        }
    };

    //Get Url From Google server
    private String getUrl(double latSource,double langSource,double latDestination,double langDestination) {
        // Origin of route
        String str_origin = "origin=" + latSource + "," + langSource;
        // Destination of route
        String str_dest = "destination=" + latDestination + "," + langDestination;
        // Sensor enabled
        String sensor = "sensor=false";
        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor;
        // Output format
        String output = "json";
        // Building the url to the web service
        String url = "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;

        return url;
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
            Log.e("url",strUrl);
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
        zoom = Math.round(5.0);
        debugHelper.drawDebugGrid(mMap,zoom);

    }


    // Fetches data from url passed
    private class FetchUrl extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {
            // For storing data from web service
            String data = "";
            try {
                // Fetching the data from web service
                Log.e("download Url","download");
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
                Log.d("ParserTask",e.toString());
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {
            List<LatLng> points=null;
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
                    if(j==0)
                    {
                        distance = (String)point.get("distance");
                        continue;
                    }else if(j==1)
                    {
                        duration = (String)point.get("duration");
                        continue;
                    }

                    double lat = Double.parseDouble(point.get("lat"));
                    double lng = Double.parseDouble(point.get("lng"));
                    LatLng position = new LatLng(lat, lng);

                    points.add(position);
                }


            }

            //dividing list and storing latlang in seperate four list ..
            first =  points.subList(0,points.size()/2);
            third=first.subList(0,first.size()/2);
            fourth=first.subList(first.size()/2-1,first.size());
            second =  points.subList((points.size()/2)-1,points.size());
            fifth=second.subList(0,second.size()/2);
            six=second.subList(second.size()/2-1,second.size());
            lineOptions.addAll(third);
            lineOptions1.addAll(fourth);
            lineOptions3.addAll(fifth);
            lineOptions4.addAll(six);
            lineOptions.width(12);
            lineOptions1.width(12);
            lineOptions3.width(12);
            lineOptions4.width(12);
            lineOptions.color(Color.parseColor("#76FF03"));
            lineOptions1.color(Color.parseColor("#02ff7c"));
            lineOptions3.color(Color.parseColor("#e21b47"));
            lineOptions4.color(Color.parseColor("#e21b1b"));

            tvDistance.setText(distance);           // Drawing polyline in the Google Map for the i-th route
            tvAvgNoise.setText(duration);
            if(lineOptions != null)
            {
                mMap.addPolyline(lineOptions);
                mMap.addPolyline(lineOptions1);
                mMap.addPolyline(lineOptions3);
                mMap.addPolyline(lineOptions4);


            }
            else
            {
                Log.d("onPostExecute","without Polylines drawn");
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
    public void onClick(View view) {

        if (view==ivTrack){

        }
        if (view==ivFab){
            //storing data to sqlite..
            boolean inserted=databaseHelper.insertFabData(source,destination,distance,null);

            if (inserted==true)
            {

                Toasty.success(getApplicationContext(),"Congratulation ! Make Favourit Success.",Toast.LENGTH_LONG).show();

            }

        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(updateLocation);
    }

}
