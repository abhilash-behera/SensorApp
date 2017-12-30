package com.sensorapp;

import android.app.Dialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
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
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
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

public class GpsActivity extends AppCompatActivity implements OnMapReadyCallback,GoogleApiClient.ConnectionCallbacks,GoogleApiClient.OnConnectionFailedListener,LocationListener,View.OnClickListener {

    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    private static final float DEFULTZOOM = 5;
    LocationRequest mLocationRequest;
    double lat, lang,destLat,destLang;
    LatLng latLng;
    GPSTracker gpsTracker;
    Location location;
    private Handler handler;
    String distance = "";
    String duration = "";
    MarkerOptions marker1,marker2;
    Marker mCurrLocationMarker;
    CoordinatorLayout coordinatorLayout;
    ArrayList<LatLng> MarkerPoints;
    Geocoder geocoder;
    List<Address> addressSource;
    List<Address> addressDestination;
    FloatingActionButton ivPlay,ivStop,ivFab;
    DatabaseHelper databaseHelper;
    String noiseDb,source,destination;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.coordinatorLayout);
        // network connection check
        checkNetworkConnection();

    }

    private void checkNetworkConnection(){
        if (Utils.isNetworkAvailable(GpsActivity.this)) {
            //get instance of gpsTracker..
            gpsTracker = new GPSTracker(GpsActivity.this);


            //checking gps is open or close..
            if (!gpsTracker.canGetLocation()) {

                gpsTracker.showSettingsAlert(); //if close alert dilog to open the gps..

            }
            //checking google services is available or not..
            if (googleServiceAvailable()) {
                setContentView(R.layout.activity_gps);
            }else{
                Toasty.error(getApplicationContext(),"Google play services not available in device.",Toast.LENGTH_LONG).show();
                finish();
            }

            //setting action bar..
            ActionBar actionBar=getSupportActionBar();
            actionBar.setTitle("Gps");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);

            //getting instance of geocoder...
            geocoder = new Geocoder(this, Locale.getDefault());

            // Initializing
            MarkerPoints = new ArrayList<>();

            lat = gpsTracker.getLatitude();    //get latitude from gps..
            lang = gpsTracker.getLongitude();  //get longitude from gps

            //infleating google map
            MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
            mapFragment.getMapAsync(this);
            handler = new Handler(); //handler object to geting location continue..in thread

            ivPlay=(FloatingActionButton) findViewById(R.id.ivPlay);
            ivStop=(FloatingActionButton) findViewById(R.id.ivStop);
            ivFab=(FloatingActionButton) findViewById(R.id.ivFab);
            ivPlay.setOnClickListener(this);
            ivStop.setOnClickListener(this);
            ivFab.setOnClickListener(this);

            databaseHelper=new DatabaseHelper(this);
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
    }
    //check googleservice of device
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
        mMap=googleMap;
        String add="";
        gotoLocation(lat,lang,DEFULTZOOM);
        mMap.setMapType(googleMap.MAP_TYPE_HYBRID);

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
            }
        }
        else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
        }

        try {

            addressSource = geocoder.getFromLocation(lat, lang, 1);  //getting location from geocoder
            source = addressSource.get(0).getAddressLine(0);
            String state = addressSource.get(0).getAdminArea();
            String country = addressSource.get(0).getCountryName();


        } catch (Exception e) {
            e.printStackTrace();
        }


        //setting marker on map
        marker1 = new MarkerOptions()
                .title(add)
                .position(new LatLng(lat, lang))
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mCurrLocationMarker=mMap.addMarker(marker1);



    }


    protected synchronized void buildGoogleApiClient() {

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();

    }

    //Get Url From Google server
    private String getUrl(double lat,double lng,double latd,double lngd) {
        // Origin of route
        String str_origin = "origin=" + lat + "," + lng;
        // Destination of route
        String str_dest = "destination=" + latd + "," + lngd;
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


    //set Camera on location
    private void gotoLocation(double lat, double lng, float zoom) {

        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mMap.moveCamera(update);
    }

    // updates the visualizer every 50 milliseconds
    Runnable updateLocation = new Runnable() {
        @Override
        public void run() {
            if (gpsTracker.canGetLocation())
            {
                try{

//                    location = gpsTracker.getLocation();// update the location
                     destLat=gpsTracker.getLatitude();
                    destLang=gpsTracker.getLongitude();
                    String url=getUrl(lat,lang,destLat,destLang);
                    FetchUrl fetchUrl = new FetchUrl();
                    // Start downloading json data from Google Directions API
                    fetchUrl.execute(url);

                    marker2 = new MarkerOptions()
                            .position(new LatLng(destLat, destLang))
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
                    mMap.addMarker(marker2);

                    Toast.makeText(getApplicationContext(),""+ destLat+ ","+destLang, Toast.LENGTH_SHORT).show();

                    handler.postDelayed(this, 6000);


                }catch (Exception ex){

                    ex.printStackTrace();
                }



            }
        }
    };


    //Set Circle arround marker
    private Circle drawCircle(LatLng ll) {

        CircleOptions options=new CircleOptions()
                .center(ll)
                .radius(700)
                .fillColor(0x33BEC0D5)
                .strokeColor(Color.BLUE)
                .strokeWidth(2);

        return mMap.addCircle(options);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        handler.removeCallbacks(updateLocation);
    }

    @Override
    public void onConnected( Bundle bundle) {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
        {
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
    public void onClick(View view) {
        if (view==ivPlay){

            handler.post(updateLocation); //start handler to get location continue..
            Toasty.success(getApplicationContext(),"drawing path Started ",Toast.LENGTH_LONG).show();

        }
        if (view==ivStop){

            handler.removeCallbacks(updateLocation);
            Toasty.success(getApplicationContext(),"drawing path stoped ",Toast.LENGTH_LONG).show();

        }
        if (view==ivFab){

            getdestinationAddress(destLat,destLang);
        }
    }

    private void getdestinationAddress(double destLat, double destLang) {

        try {

            addressDestination = geocoder.getFromLocation(destLat, destLang, 1);  //getting location from geocoder
            destination = addressDestination.get(0).getAddressLine(0);
            String state = addressDestination.get(0).getAdminArea();
            String country = addressDestination.get(0).getCountryName();

            GPSTracker gpsTracker=new GPSTracker(GpsActivity.this);
            Location location=gpsTracker.getLocation();
            Log.d("awesome","Got location: "+location);

            GPSData gpsData=new GPSData(
                    source,
                    destination,
                    distance,
                    duration,
                    String.valueOf(location.getLatitude()),
                    String.valueOf(location.getLongitude())
            );


            if (databaseHelper.insertGpsData(gpsData))
            {
                Toasty.success(getApplicationContext(),"Data Saved in Database",Toast.LENGTH_LONG).show();

            }else{
                Toasty.error(getApplicationContext(),"Something went wrong. Please try again.",Toast.LENGTH_LONG).show();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes
            for (int i = 0; i < result.size(); i++) {
                points = new ArrayList<>();
                lineOptions = new PolylineOptions();

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

                // Adding all the points in the route to LineOptions
                lineOptions.addAll(points);
                lineOptions.width(12);
                lineOptions.color(Color.BLUE);

            }

            // Drawing polyline in the Google Map for the i-th route
            if(lineOptions != null)
            {
                mMap.addPolyline(lineOptions);
            }
            else
            {
                Log.d("onPostExecute","without Polylines drawn");
            }
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


}
