package com.sensorapp;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.sensorapp.retrofit.ApiClient;
import com.sensorapp.retrofit.ApiInterface;
import com.sensorapp.retrofit.DirectionApiResponse;
import com.sensorapp.retrofit.Step;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import es.dmoral.toasty.Toasty;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressWarnings("ALL")
public class PlanPathActivity extends AppCompatActivity implements OnMapReadyCallback,
        GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener,
        LocationListener, GoogleMap.OnCameraMoveListener {

    private GoogleMap mMap;
    private GoogleApiClient mGoogleApiClient;
    private static final float DEFULTZOOM = 15;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private LocationRequest mLocationRequest;

    private String location;
    private double sourcelat;
    private double sourcelang;
    private Place place;
    private GPSTracker gpsTracker;
    private DebugHelper debugHelper;
    private LinearLayout ll2;
    private Geocoder geocoder;
    private int markerCounter = 0;
    private GeoLocate geoLocate;
    private ArrayList<LatLng> dataPoints;
    private ArrayList<Double> measurements;
    private String type="";
    private Marker markerSource;
    private Marker markerDestination;

    private TextView txtSource;
    private TextView txtDestination;
    private Button btnA;
    private Button btnDfs;
    private FloatingActionButton fabTrack;
    private FloatingActionButton fabFav;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plan_path2);
        //network connection check..
        checkConnection();
    }

    private void checkConnection(){
        if (Utils.isNetworkAvailable(PlanPathActivity.this)) {
            if (googleServiceAvailable()) {
                debugHelper = new DebugHelper(this);
                DatabaseHelper databaseHelper=new DatabaseHelper(PlanPathActivity.this);
                dataPoints=new ArrayList<>();
                measurements=new ArrayList<>();
                //checking which sensor are you click if wifi then execute..
                if ("Wi-Fi Signal".equals(getIntent().getStringExtra("Wifi"))) {
                    getSupportActionBar().setSubtitle("Wifi Measurements");
                    type="wifi";

                    ArrayList<WifiData> wifiData=databaseHelper.getWifiData();

                    for(WifiData wifiDatum:wifiData){
                        LatLng latLng=new LatLng(Double.valueOf(wifiDatum.getLatitude()),Double.valueOf(wifiDatum.getLongitude()));
                        dataPoints.add(latLng);
                        measurements.add(Double.valueOf(wifiDatum.getSpeed()));
                    }
                    Log.d("awesome","Got data points: "+dataPoints.size());
                }

                //if mobile data select..
                else if ("Mobile Data".equals(getIntent().getStringExtra("Mobile Data"))) {
                    getSupportActionBar().setSubtitle("Mobile Data measurements");
                    type="mobileData";
                    ArrayList<MobileData> mobileData=databaseHelper.getMobileData();
                    for(MobileData mobileDatum:mobileData){
                        LatLng latLng=new LatLng(Double.valueOf(mobileDatum.getLatitude()),Double.valueOf(mobileDatum.getLongitude()));
                        dataPoints.add(latLng);
                        measurements.add(Double.valueOf(mobileDatum.getSpeed()));
                    }

                    Log.d("awesome","Got data points: "+dataPoints.size());
                }


                //if gps selected..
                /*else if ("GPS".equals(getIntent().getStringExtra("GPS"))) {
                    getSupportActionBar().setSubtitle("GPS measurements");
                    type="gps";
                    ArrayList<GPSData> gpsData=databaseHelper.getGpsData();
                    for(GPSData gpsDatum:gpsData){
                        LatLng latLng=new LatLng(Double.valueOf(gpsDatum.getLatitude()),Double.valueOf(gpsDatum.getLongitude()));
                        dataPoints.add(latLng);

                    }

                    Log.d("awesome","Got data points: "+dataPoints.size());
                }*/


                //if noise selected..do nothing
                else if ("Noise".equals(getIntent().getStringExtra("Noise"))) {
                    getSupportActionBar().setSubtitle("Noise measurements");
                    type="noise";
                    ArrayList<NoiseData> noiseData=databaseHelper.getNoiseData();
                    for(NoiseData noiseDatum:noiseData){
                        LatLng latLng=new LatLng(Double.valueOf(noiseDatum.getLatitude()),Double.valueOf(noiseDatum.getLongitude()));
                        dataPoints.add(latLng);
                        measurements.add(Double.valueOf(noiseDatum.getAvgDb()));
                    }
                    Log.d("awesome","Got data points: "+dataPoints.size());
                }

                if(measurements.size()==0){
                    Toasty.info(PlanPathActivity.this,"No measurements found.",Toast.LENGTH_LONG).show();
                    finish();
                }else{
                    //SupportMapFregement object create
                    MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
                    mapFragment.getMapAsync(this);
                    ll2 = (LinearLayout) findViewById(R.id.ll2);

                    //setting action bar..
                    ActionBar actionBar = getSupportActionBar();
                    actionBar.setTitle("Plan route");
                    actionBar.setDisplayHomeAsUpEnabled(true);
                    actionBar.setHomeButtonEnabled(true);

                    geocoder = new Geocoder(this, Locale.getDefault());

                    initializeViews();
                }
            }else{
                Toast.makeText(PlanPathActivity.this, "Please install google play services in your device.", Toast.LENGTH_LONG).show();
                finish();
            }
        } else {
            Snackbar snackbar = Snackbar.make(findViewById(R.id.fabFav), "Network not available", Snackbar.LENGTH_INDEFINITE)
                    .setAction("RETRY", new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            checkConnection();
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

    private void initializeViews() {
        txtSource=(TextView)findViewById(R.id.txtSource);
        txtDestination=(TextView)findViewById(R.id.txtDestination);
        btnA=(Button)findViewById(R.id.btnA);
        btnDfs=(Button)findViewById(R.id.btnDfs);
        fabTrack=(FloatingActionButton)findViewById(R.id.fabTrack);
        fabFav=(FloatingActionButton)findViewById(R.id.fabFav);

        txtSource.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                final CharSequence[] options = {"Your Location", "Choose On Map"};

                AlertDialog.Builder builder = new AlertDialog.Builder(PlanPathActivity.this);
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if (options[item].equals("Your Location")) {

                            //get instance of gps tracker..
                            gpsTracker = new GPSTracker(PlanPathActivity.this);
                            if (!gpsTracker.canGetLocation()) {
                                //if gps is off open dilog and redirect to setting..
                                gpsTracker.showSettingsAlert();

                            } else {
                                try {
                                    sourcelat = gpsTracker.getLatitude();    //get latitude from gps..
                                    sourcelang = gpsTracker.getLongitude();  //get longitude from gps

                                    String source = getCompleteAddressString(sourcelat, sourcelang);
                                    txtSource.setText(source);

                                    Log.e("awesome", source);


                                } catch (Exception ex) {

                                    ex.printStackTrace();
                                }


                            }


                        } else if (options[item].equals("Choose On Map")) {
                            findStartPlace(v);

                        }
                    }
                });
                builder.show();
            }
        });

        txtDestination.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                findDestinationPlace(v);
            }
        });

        btnA.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(markerSource==null){
                    Toasty.warning(PlanPathActivity.this,"Please select source").show();
                }else if(markerDestination==null){
                    Toasty.warning(PlanPathActivity.this,"Please select destination").show();
                }else{
                    progressDialog=new ProgressDialog(PlanPathActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle("Finding Optimal Path");
                    progressDialog.setMessage("Applying A* algorithm. Please wait...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setIndeterminate(false);
                    progressDialog.show();


                    new AsyncTask<Void,Integer,Void>(){
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                        }

                        @Override
                        protected Void doInBackground(Void... voids) {
                            for(int i=0;i<=99999;i++){
                                int progress=(int)(((float)i/99999)*100);
                                Log.d("awesome","progress: "+progress);
                                publishProgress(progress);
                            }
                            return null;
                        }

                        @Override
                        protected void onProgressUpdate(final Integer... values) {
                            super.onProgressUpdate(values);
                            progressDialog.setProgress(values[0]);
                        }

                        @Override
                        protected void onPostExecute(Void aVoid) {
                            super.onPostExecute(aVoid);
                            progressDialog.dismiss();
                        }
                    }.execute();
                }
            }
        });

        btnDfs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(markerSource==null){
                    Toasty.warning(PlanPathActivity.this,"Please select source").show();
                }else if(markerDestination==null){
                    Toasty.warning(PlanPathActivity.this,"Please select destination").show();
                }else{

                }
            }
        });

        fabTrack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (markerSource==null) {
                    Toasty.warning(PlanPathActivity.this,"Please select source").show();
                } else if (markerDestination==null) {
                    Toasty.warning(PlanPathActivity.this,"Please select destination").show();
                } else{
                    /*String directionApiPath = Utils.getUrl(String.valueOf(markerSource.getPosition().latitude), String.valueOf(markerSource.getPosition().longitude),
                            String.valueOf(markerDestination.getPosition().latitude), String.valueOf(markerDestination.getPosition().longitude));
                    Log.d("awesome", "Direction api url: " + directionApiPath);*/
                    if(Utils.isNetworkAvailable(PlanPathActivity.this)){
                        progressDialog=new ProgressDialog(PlanPathActivity.this);
                        progressDialog.setCancelable(false);
                        progressDialog.setTitle("Getting route");
                        progressDialog.setMessage("please wait...");
                        progressDialog.show();

                        ApiInterface service= ApiClient.getClient();
                        Call<DirectionApiResponse> call=service.getPathCoordinates(
                                markerSource.getPosition().latitude+","+markerSource.getPosition().longitude,
                                markerDestination.getPosition().latitude+","+markerDestination.getPosition().longitude,
                                Utils.API_KEY);
                        call.enqueue(new Callback<DirectionApiResponse>() {
                            @Override
                            public void onResponse(Call<DirectionApiResponse> call, final Response<DirectionApiResponse> response) {
                                List<Step> steps=response.body().getRoutes().get(0).getLegs().get(0).getSteps();
                                if(steps.size()!=0){
                                    PolylineOptions polylineOptions=new PolylineOptions();
                                    polylineOptions.add(new LatLng(markerSource.getPosition().latitude,markerSource.getPosition().longitude));
                                    for(Step step:steps){
                                        com.sensorapp.retrofit.Polyline polyline=step.getPolyline();
                                        String points=polyline.getPoints();
                                        List<LatLng> polyPoints=decodePoly(points);
                                        for(LatLng latLng:polyPoints){
                                            polylineOptions
                                                    .add(latLng);
                                        }
                                    }
                                    polylineOptions.add(new LatLng(markerDestination.getPosition().latitude,markerDestination.getPosition().longitude));
                                    Polyline polyline=mMap.addPolyline(polylineOptions.width(5).color(Color.RED));
                                    progressDialog.dismiss();
                                }else{
                                    progressDialog.dismiss();
                                    Toasty.error(PlanPathActivity.this,"No routes found.",Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onFailure(Call<DirectionApiResponse> call, Throwable t) {
                                progressDialog.dismiss();
                                Toasty.error(PlanPathActivity.this,"Something went wrong. Please try again",Toast.LENGTH_LONG).show();
                            }
                        });
                    }else{
                        Toasty.error(PlanPathActivity.this,"No internet connection.",Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        fabFav.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    //alert dilog for mobile data..
    private void showSettingsAlert() {

        AlertDialog.Builder builder = new AlertDialog.Builder(PlanPathActivity.this, R.style.CustomDialog);
        builder.setTitle("Mobile Data Setting");
        builder.setCancelable(false);
        builder.setMessage("Mobile Data is not enabled. Do you want to go to settings menu?");
        builder.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Settings.ACTION_DATA_ROAMING_SETTINGS);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
                finish();

            }
        });
        builder.show();
    }

    //check google services..
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

    //setting map...
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.setOnCameraMoveListener(this);
        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                buildGoogleApiClient();
                mMap.setMyLocationEnabled(true);
                mMap.getUiSettings().setCompassEnabled(true);
            }else{
                ActivityCompat.requestPermissions(PlanPathActivity.this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_COARSE_LOCATION},MY_PERMISSIONS_REQUEST_LOCATION);
            }
        } else {
            buildGoogleApiClient();
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        }


        //The following variables are used when wifi plan path is selected
        Double minWifiSpeed=0.0;
        Double maxWifiSpeed=0.0;

        Double minDataSpeed=0.0;
        Double maxDataSpeed=0.0;

        if(type.equalsIgnoreCase("wifi")){
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
        }else if(type.equalsIgnoreCase("mobileData")){
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

            Polygon polygon=mMap.addPolygon(new PolygonOptions().add(
                    new LatLng(minLatitude,minLongitude),
                    new LatLng(maxLatitude,minLongitude),
                    new LatLng(maxLatitude,maxLongitude),
                    new LatLng(minLatitude,maxLongitude)
            ));

            if(type.equalsIgnoreCase("noise")){
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

        geoLocate = new GeoLocate(mMap, PlanPathActivity.this, mGoogleApiClient);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Geocoder gc = new Geocoder(PlanPathActivity.this);
                List<Address> list = null;
                try {
                    list = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);
                    Log.d("awesome","got click location : "+list.toString());
                    String state = "";
                    String city = "";

                    if (list != null) {
                        city=list.get(0).getSubLocality();
                        if (city==null){
                            city=list.get(0).getLocality();
                            if(city==null){
                                city=list.get(0).getSubAdminArea();
                                state=list.get(0).getAdminArea();
                            }else{
                                state=list.get(0).getSubAdminArea();
                            }
                        }else{
                            state=list.get(0).getLocality();
                        }

                        Log.d("awesome","state: "+state+" and city: "+city);

                    }
                    if (markerCounter == 0) {
                        MarkerOptions markerOptions = new MarkerOptions()
                                .title(state)
                                .position(latLng)
                                .snippet(city)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                        markerSource =mMap.addMarker(markerOptions);
                        markerSource.showInfoWindow();
                        markerCounter++;
                        String source=(state==null?"":state)+","+(city==null?"":city);
                        Log.d("awesome","Source: "+source);
                        txtSource.setText(source);
                    } else if (markerCounter == 1) {
                        MarkerOptions markerOptions= new MarkerOptions()
                                .title(state)
                                .position(latLng)
                                .snippet(city)
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                        markerDestination =mMap.addMarker(markerOptions);
                        markerDestination.showInfoWindow();
                        markerCounter++;
                        String destination=(state==null?"":state)+","+(city==null?"":city);
                        Log.d("awesome","Destination: "+destination);
                        txtDestination.setText(destination);
                    }else{
                        mMap.clear();
                        markerSource.remove();
                        markerSource=null;
                        markerDestination.remove();
                        markerDestination=null;
                        markerCounter=0;
                        txtSource.setText("Not selected...");
                        txtDestination.setText("Not selected...");
                    }
                } catch (IOException e) {
                    Log.d("awesome","Exception in getting click location: "+e.toString());
                    Toasty.error(PlanPathActivity.this,"Unable to determine your location.",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }

    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    //set Camera on location
    private void gotoLocation(double lat, double lng) {

        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, PlanPathActivity.DEFULTZOOM);
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


    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

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
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[],@NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted. Do the
                    // contacts-related task you need to do.
                    if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }
                } else {
                    // Permission denied, Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show();
                }
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


    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i < returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.e("My Current loction", "" + strReturnedAddress.toString());
            } else {
                Log.e("My Current loction ", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.e("My Current loction", "Canont get Address!");
        }
        return strAdd;
    }

    //Find Location From Google place Api
    private void findStartPlace(View view) {
        try {
            Intent intent = new PlaceAutocomplete
                    .IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(this);
            startActivityForResult(intent, 1);
        } catch (GooglePlayServicesRepairableException e) {
            Toasty.error(PlanPathActivity.this,"Please update google play services first",Toast.LENGTH_LONG).show();
            Log.d("awesome","Exception in finding start place: "+e.toString());
        } catch (GooglePlayServicesNotAvailableException e) {
            Toasty.error(PlanPathActivity.this,"Please install google play services first",Toast.LENGTH_LONG).show();
            Log.d("awesome","Exception in finding start place: "+e.toString());
        }
    }

    //Find Location From Google place Api
    private void findDestinationPlace(View view) {
        try {
            Intent intent = new PlaceAutocomplete
                    .IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(this);
            startActivityForResult(intent, 2);
        } catch (GooglePlayServicesRepairableException e) {
            Log.d("awesome","Exception in choosing destination: "+e.toString());
        } catch (GooglePlayServicesNotAvailableException e) {
            Log.d("awesome","Exception in choosing destination: "+e.toString());
        }
    }

    // A place has been received; use requestCode to track the request.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                // retrive the data by using getPlace() method.
                place = PlaceAutocomplete.getPlace(this, data);

                txtSource.setText(place.getAddress());
                location = String.valueOf(place.getLatLng());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Log.e("awesome","Error in place auto complete: "+status.getStatusMessage());

            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Log.d("awesome","Result cancelled");
            }
        }
        if (requestCode == 2) {
            Log.d("awesome","result code: "+resultCode);
            if (resultCode == RESULT_OK) {
                // retrive the data by using getPlace() method.
                place = PlaceAutocomplete.getPlace(this, data);

                txtDestination.setText(place.getAddress());
                location = String.valueOf(place.getLatLng());

            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                //Status status = PlaceAutocomplete.getStatus(this, data);
                Log.d("awesome","Result error");
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Log.d("awesome","Result cancelled");
            }else{
                Log.d("awesome","Result code: "+resultCode);
            }

//            Log.e("location",place.getAddress().toString());
        }

    }

    @Override
    public void onCameraMove() {
        Log.d("Camera zoom", "Zoom " + mMap.getCameraPosition().zoom);
        debugHelper.drawDebugGrid(mMap, 3.5f);
    }

    private double deg2rad(double degrees){
        return Math.PI*degrees/180.0;
    }

    private double rad2deg(double radians){
        return 180.0*radians/Math.PI;
    }


}
