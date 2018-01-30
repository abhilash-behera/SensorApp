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
import android.os.CountDownTimer;
import android.os.Handler;
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
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.VisibleRegion;
import com.google.maps.android.PolyUtil;
import com.sensorapp.retrofit.ApiClient;
import com.sensorapp.retrofit.ApiInterface;
import com.sensorapp.retrofit.DirectionApiResponse;
import com.sensorapp.retrofit.Step;

import java.io.IOException;
import java.lang.reflect.Array;
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
    private LinearLayout ll2;
    private Geocoder geocoder;
    private int markerCounter = 0;
    private GeoLocate geoLocate;
    private ArrayList<DataPoint> dataPoints;
    private ArrayList<ArrayList<GridNode>> gridList;
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
    private LatLng source;
    private LatLng destination;
    private final int START_PLACE_REQUEST_CODE=2;
    private final int DESTINATION_PLACE_REQUEST_CODE=3;
    private Polyline aPolyline;
    private Polyline dfsPolyline;
    private Polyline routePolyline;
    private static final Double CLUSTER_SIZE=0.00125762;
    private MapGrid mapGrid;
    private int destination_x;
    private int destination_y;
    private int start_x;
    private int start_y;



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
                DatabaseHelper databaseHelper=new DatabaseHelper(PlanPathActivity.this);
                dataPoints=new ArrayList<>();

                //checking which sensor are you click if wifi then execute..
                if (getIntent().getStringExtra(DashBoardActivity.DATA_TYPE).equalsIgnoreCase(DashBoardActivity.DATA_TYPE_WIFI)) {
                    getSupportActionBar().setSubtitle("Wifi Measurements");
                    type="wifi";

                    ArrayList<WifiData> wifiData=databaseHelper.getWifiData();

                    for(WifiData wifiDatum:wifiData){
                        LatLng latLng=new LatLng(Double.valueOf(wifiDatum.getLatitude()),Double.valueOf(wifiDatum.getLongitude()));
                        dataPoints.add(new DataPoint(latLng,Double.valueOf(wifiDatum.getSpeed())));
                    }
                    Log.d("awesome","Got data points: "+dataPoints.size());
                }

                //if mobile data select..
                else if (getIntent().getStringExtra(DashBoardActivity.DATA_TYPE).equalsIgnoreCase(DashBoardActivity.DATA_TYPE_MOBILE_DATA)) {
                    getSupportActionBar().setSubtitle("Mobile Data measurements");
                    type="mobileData";
                    ArrayList<MobileData> mobileData=databaseHelper.getMobileData();
                    for(MobileData mobileDatum:mobileData){
                        LatLng latLng=new LatLng(Double.valueOf(mobileDatum.getLatitude()),Double.valueOf(mobileDatum.getLongitude()));
                        dataPoints.add(new DataPoint(latLng,Double.valueOf(mobileDatum.getSpeed())));
                    }

                    Log.d("awesome","Got data points: "+dataPoints.size());
                }

                //if noise selected..do nothing
                else if (getIntent().getStringExtra(DashBoardActivity.DATA_TYPE).equalsIgnoreCase(DashBoardActivity.DATA_TYPE_NOISE)) {
                    getSupportActionBar().setSubtitle("Noise measurements");
                    type="noise";
                    ArrayList<NoiseData> noiseData=databaseHelper.getNoiseData();
                    for(NoiseData noiseDatum:noiseData){
                        LatLng latLng=new LatLng(Double.valueOf(noiseDatum.getLatitude()),Double.valueOf(noiseDatum.getLongitude()));
                        dataPoints.add(new DataPoint(latLng,Double.valueOf(noiseDatum.getAvgDb())));
                    }
                    Log.d("awesome","Got data points: "+dataPoints.size());
                }

                if(dataPoints.size()==0){
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
                        if(markerSource!=null){
                            markerSource.remove();
                        }
                        if (options[item].equals("Your Location")) {
                            //get instance of gps tracker..
                            gpsTracker = new GPSTracker(PlanPathActivity.this);
                            if (!gpsTracker.canGetLocation()) {
                                //if gps is off open dilog and redirect to setting..
                                gpsTracker.showSettingsAlert();
                            } else {
                                try {

                                    source = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                                    Log.d("awesome","Source : "+source+" and destination: "+destination);
                                    if(source!=null&&checkSameLocations(source,destination)){
                                        Toasty.warning(PlanPathActivity.this,"Source and destination cannot be same.").show();
                                        return;
                                    }
                                    Geocoder gc = new Geocoder(PlanPathActivity.this);
                                    List<Address> list = null;
                                    list = gc.getFromLocation(source.latitude, source.longitude, 1);
                                    Log.d("awesome", "got click location : " + list.toString());
                                    String state = "";
                                    String city = "";

                                    if (list != null) {
                                        city = list.get(0).getSubLocality();
                                        if (city == null) {
                                            city = list.get(0).getLocality();
                                            if (city == null) {
                                                city = list.get(0).getSubAdminArea();
                                                state = list.get(0).getAdminArea();
                                            } else {
                                                state = list.get(0).getSubAdminArea();
                                            }
                                        } else {
                                            state = list.get(0).getLocality();
                                        }

                                        Log.d("awesome", "state: " + state + " and city: " + city);

                                        MarkerOptions markerOptions = new MarkerOptions()
                                                .title(state)
                                                .position(source)
                                                .snippet(city)
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                                        markerSource =mMap.addMarker(markerOptions);
                                        markerCounter=1;
                                        source=markerOptions.getPosition();
                                        markerSource.showInfoWindow();
                                        String source=(state==null?"":state)+","+(city==null?"":city);
                                        Log.d("awesome","Source: "+source);
                                        txtSource.setText(source);
                                        gotoLocation(markerSource.getPosition().latitude,markerSource.getPosition().longitude);
                                    } else {
                                        Toasty.error(PlanPathActivity.this, "Unable to get your location.").show();
                                    }
                                } catch (Exception e) {
                                    Log.d("awesome", "Exception in getting location :" + e.toString());
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
            public void onClick(final View v) {
                final CharSequence[] options = {"Your Location", "Choose On Map"};

                AlertDialog.Builder builder = new AlertDialog.Builder(PlanPathActivity.this);
                builder.setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        if(markerDestination!=null){
                            markerDestination.remove();
                        }
                        if (options[item].equals("Your Location")) {
                            //get instance of gps tracker..
                            gpsTracker = new GPSTracker(PlanPathActivity.this);
                            if (!gpsTracker.canGetLocation()) {
                                //if gps is off open dilog and redirect to setting..
                                gpsTracker.showSettingsAlert();
                            } else {
                                try {
                                    destination = new LatLng(gpsTracker.getLatitude(), gpsTracker.getLongitude());
                                    Log.d("awesome","Source: "+source+" and destination: "+destination);
                                    if(destination!=null&&checkSameLocations(source,destination)){
                                        Toasty.warning(PlanPathActivity.this,"Source and destination cannot be same.").show();
                                        return;
                                    }
                                    Geocoder gc = new Geocoder(PlanPathActivity.this);
                                    List<Address> list = null;
                                    list = gc.getFromLocation(destination.latitude, destination.longitude, 1);
                                    Log.d("awesome", "got click location : " + list.toString());
                                    String state = "";
                                    String city = "";

                                    if (list != null) {
                                        city = list.get(0).getSubLocality();
                                        if (city == null) {
                                            city = list.get(0).getLocality();
                                            if (city == null) {
                                                city = list.get(0).getSubAdminArea();
                                                state = list.get(0).getAdminArea();
                                            } else {
                                                state = list.get(0).getSubAdminArea();
                                            }
                                        } else {
                                            state = list.get(0).getLocality();
                                        }

                                        Log.d("awesome", "state: " + state + " and city: " + city);

                                        MarkerOptions markerOptions = new MarkerOptions()
                                                .title(state)
                                                .position(destination)
                                                .snippet(city)
                                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                                        markerDestination =mMap.addMarker(markerOptions);
                                        markerCounter=0;
                                        markerDestination.showInfoWindow();
                                        String destination=(state==null?"":state)+","+(city==null?"":city);
                                        Log.d("awesome","Destination: "+destination);
                                        txtDestination.setText(destination);
                                        gotoLocation(markerDestination.getPosition().latitude,markerDestination.getPosition().longitude);
                                    } else {
                                        Toasty.error(PlanPathActivity.this, "Unable to get your location.").show();
                                    }
                                } catch (Exception e) {
                                    Log.d("awesome", "Exception in getting location :" + e.toString());
                                }
                            }
                        } else if (options[item].equals("Choose On Map")) {
                            findDestinationPlace(v);
                        }
                    }
                });
                builder.show();
                //findDestinationPlace(v);
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
                    if(checkSameLocations(source,destination)){
                        Toasty.error(PlanPathActivity.this,"Source and destination cannot be same.").show();
                        return;
                    }

                    progressDialog=new ProgressDialog(PlanPathActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle("Finding Optimal Path");
                    progressDialog.setMessage("Applying A* algorithm. Please wait...");
                    progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                    progressDialog.setIndeterminate(false);
                    progressDialog.show();

                    if(dfsPolyline!=null){
                        dfsPolyline.remove();
                        dfsPolyline=null;
                    }

                    if(aPolyline!=null){
                        aPolyline.remove();
                        aPolyline=null;
                    }

                    if(routePolyline!=null){
                        routePolyline.remove();
                        routePolyline=null;
                    }

                    new AsyncTask<Void,Integer,Integer>(){
                        int progress=0;
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                            for(int a=0;a<gridList.size();a++){
                                for(int b=0;b<gridList.get(a).size();b++){
                                    List<LatLng> points=gridList.get(a).get(b).polygon.getPoints();
                                    LatLngBounds latLngBounds=new LatLngBounds(points.get(0),points.get(2));
                                    GridNode gridNode=gridList.get(a).get(b);
                                    if(latLngBounds.contains(markerSource.getPosition())){
                                        gridNode.polygon.setFillColor(0xff00ff00);
                                        gridNode.type=GridNode.TYPE_SOURCE;
                                        markerSource.remove();
                                        start_x=a;
                                        start_y=b;
                                    }else if(latLngBounds.contains(markerDestination.getPosition())){
                                        gridNode.polygon.setFillColor(0xffff0000);
                                        gridNode.type=GridNode.TYPE_DESTINATION;
                                        markerDestination.remove();
                                        destination_x=a;
                                        destination_y=b;
                                    }
                                }
                            }
                        }

                        @Override
                        protected Integer doInBackground(Void... voids) {
                            Log.d("awesome","Coordinates of destination ("+destination_x+","+destination_y+")");
                            for(int i=0;i<gridList.size();i++){
                                for(int j=0;j<gridList.size();j++){
                                    if(gridList.get(i).get(j).type==GridNode.TYPE_OBSTACLE){
                                        Log.d("awesome","Obstacle at "+i+","+j);
                                    }
                                }
                            }
                            for(int x=0;x<gridList.size();x++){
                                for(int y=0;y<gridList.get(x).size();y++){
                                    GridNode gridNode=gridList.get(x).get(y);
                                    Double h_val=Math.sqrt(Double.valueOf(Math.pow(destination_x-x,2))+Double.valueOf(Math.pow(destination_y-y,2)));
                                    gridNode.h=h_val;
                                    gridNode.a=x;
                                    gridNode.b=y;
                                    Log.d("awesome","H value of "+gridNode+" is: "+h_val);
                                }

                                if(progress<80){
                                    publishProgress(progress++);
                                }
                            }

                            final ArrayList<TraceNode> traceNodes=new ArrayList<>();

                            //Initialize closed list
                            ArrayList<GridNode> closedList=new ArrayList<>();

                            //Initialize open list
                            ArrayList<GridNode> openList=new ArrayList<>();

                            //Put starting node in open list
                            final GridNode startNode=gridList.get(start_x).get(start_y);
                            openList.add(startNode);

                            while (openList.size()!=0){
                                //check for node with minimum f value in openlist
                                final GridNode q=getNodeWithMinFValue(openList);

                                //Pop q off the openlist
                                Boolean removeResult=openList.remove(q);
                                Log.d("awesome","Popping q: "+q+" status: "+removeResult);

                                //Add q to closed list


                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if(!(q.a==start_x&&q.b==start_y)){
                                            q.polygon.setFillColor(0xffffff00);
                                        }

                                    }
                                });

                                //For each successor node of q
                                ArrayList<GridNode> successorList=getSuccessors(q,q.a,q.b);
                                for(final GridNode successor:successorList){
                                    if(successor.a==destination_x&&successor.b==destination_y){
                                        Log.d("awesome","Reached destination node");
                                        TraceNode traceNode=new TraceNode();
                                        traceNode.setNode(successor);
                                        traceNode.setParent(q);
                                        traceNodes.add(traceNode);
                                        //trace path
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                publishProgress(100);
                                                //drawPath(gridList.get(destination_x).get(destination_y));
                                                for(TraceNode traceNode:traceNodes){
                                                    Log.d("awesome","Node: "+traceNode.node+" parent: "+traceNode.parent);
                                                }
                                                Log.d("awesome","-----------------------");
                                                printNode(successor,traceNodes);
                                            }
                                        });

                                        //stop search
                                        return 1;
                                    }

                                    Double newG=q.g+Math.sqrt(Double.valueOf(Math.pow(q.a-successor.a,2)+Math.pow(q.b-successor.b,2)));
                                    Double newF=newG+successor.h;
                                    Log.d("awesome","previous F: "+successor.f+" and new F: "+newF);

                                    //Check openlist for already existing node
                                    if(openList.contains(successor)){
                                        Log.d("awesome","openlist contains successor");
                                        if(openList.get(openList.indexOf(successor)).f<newF){
                                            Log.d("awesome","Openlist already contains this successor and its the best so skipping");
                                            continue;
                                        }
                                        Log.d("awesome","Openlist already contains this successor but its not the best so continuing");
                                    }

                                    //Check closedlist for already existing node
                                    if(closedList.contains(successor)){
                                        Log.d("awesome","Closed list contains this successor");
                                        if(closedList.get(closedList.indexOf(successor)).f<newF){
                                            Log.d("awesome","Closedlist already contains this successor and its the best so skipping");
                                            continue;
                                        }
                                        Log.d("awesome","Closed list already contains this successor but its not the best");
                                    }

                                    Log.d("awesome","Moving this node from closed list to open list");
                                    successor.g=newG;
                                    successor.f=newF;
                                    openList.add(successor);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            successor.polygon.setFillColor(0xffdddd00);
                                        }
                                    });

                                    /*//Removing from closed list and adding to openlist
                                    if(closedList.contains(successor)){
                                        removeResult=closedList.remove(successor);
                                        Log.d("awesome","Removing status from closed list: "+removeResult);
                                    }

                                    if(openList.contains(successor)){
                                        Log.d("awesome","Updating successor data in openlist");
                                        openList.get(openList.indexOf(successor)).g=newG;
                                        openList.get(openList.indexOf(successor)).f=newF;
                                        openList.get(openList.indexOf(successor)).parent=q;
                                    }else{
                                        openList.add(successor);
                                        Log.d("awesome","Adding successor to openlist");
                                    }

*/
                                }
                                TraceNode traceNode=new TraceNode();
                                traceNode.setNode(q);
                                traceNode.setParent(q.parent);
                                traceNodes.add(traceNode);
                                closedList.add(q);
                            }
                            return 1;
                        }

                        @Override
                        protected void onProgressUpdate(final Integer... values) {
                            super.onProgressUpdate(values);
                            if(values[0]==100){
                                progressDialog.setProgress(100);
                                progressDialog.dismiss();
                            }
                            progressDialog.setProgress(values[0]);
                        }

                        @Override
                        protected void onPostExecute(Integer result) {
                            super.onPostExecute(result);
                            progressDialog.setProgress(100);
                            progressDialog.dismiss();
                            if(result==0){
                                Toasty.error(PlanPathActivity.this,"Something went wrong. Please try again. ",Toast.LENGTH_LONG).show();
                            }
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
                    if(checkSameLocations(source,destination)){
                        Toasty.error(PlanPathActivity.this,"Source and destination cannot be same.").show();
                        return;
                    }
                    progressDialog=new ProgressDialog(PlanPathActivity.this);
                    progressDialog.setCancelable(false);
                    progressDialog.setTitle("Finding Optimal Path");
                    progressDialog.setMessage("Applying DFS algorithm. Please wait...");
                    progressDialog.show();

                    if(aPolyline!=null){
                        aPolyline.remove();
                        aPolyline=null;
                    }

                    if(dfsPolyline!=null){
                        dfsPolyline.remove();
                        dfsPolyline=null;
                    }

                    if(routePolyline!=null){
                        routePolyline.remove();
                        routePolyline=null;
                    }

                    new AsyncTask<Void,Integer,Integer>(){
                        @Override
                        protected void onPreExecute() {
                            super.onPreExecute();
                        }

                        @Override
                        protected Integer doInBackground(Void... voids) {
                            Node sourceNode=new Node();
                            sourceNode.setLatLng(new LatLng(source.latitude,source.longitude));
                            Log.d("awesome","Source node: "+sourceNode.toString());

                            Node destinationNode=new Node();
                            destinationNode.setLatLng(new LatLng(destination.latitude,destination.longitude));
                            Log.d("awesome","Destination node: "+destinationNode.toString());

                            ArrayList<Node> nodeList=new ArrayList();
                            for(int i=0;i<dataPoints.size();i++){
                                LatLng latLng=dataPoints.get(i).getPosition();
                                Node node=new Node();
                                node.setLatLng(latLng);
                                nodeList.add(node);
                                Log.d("awesome","nodeList node: "+node);

                                sourceNode.adjacencyList.add(node);
                            }

                            for(Node node:nodeList){
                                for(Node adjacentNode:nodeList){
                                    if(adjacentNode!=node){
                                        node.adjacencyList.add(adjacentNode);
                                    }
                                }
                                node.adjacencyList.add(destinationNode);
                            }

                            algoDFS(sourceNode,destinationNode);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressDialog.setTitle("Drawing Route");
                                    progressDialog.setMessage("please wait...");
                                }
                            });

                            final PolylineOptions polylineOptions=new PolylineOptions();
                            polylineOptions.color(Color.GREEN).width(5);
                            Node n=sourceNode;
                            while (n.getChild()!=null){
                                try{
                                    Call<DirectionApiResponse> call=ApiClient.getClient().getPathCoordinates(
                                            n.getLatLng().latitude+","+n.getLatLng().longitude,
                                            n.getChild().getLatLng().latitude+","+n.getChild().getLatLng().longitude,
                                            Utils.API_KEY
                                    );

                                    DirectionApiResponse directionApiResponse=call.execute().body();
                                    polylineOptions.add(n.getLatLng());
                                    List<Step> steps=directionApiResponse.getRoutes().get(0).getLegs().get(0).getSteps();
                                    for(Step step:steps){
                                        List<LatLng> points=decodePoly(step.getPolyline().getPoints());
                                        for(LatLng latLng:points){
                                            polylineOptions.add(latLng);
                                        }
                                    }

                                    polylineOptions.add(n.getChild().getLatLng());
                                }catch (Exception e){
                                    Log.d("awesome","Exception in drawing optimal path: "+e.toString());
                                    return 0;
                                }
                                Log.d("awesome","printing: "+n.toString());
                                n=n.getChild();
                            }

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    dfsPolyline=mMap.addPolyline(polylineOptions);
                                }
                            });
                            return 1;
                        }

                        @Override
                        protected void onProgressUpdate(Integer... values) {
                            super.onProgressUpdate(values);
                        }

                        @Override
                        protected void onPostExecute(Integer integer) {
                            super.onPostExecute(integer);
                            progressDialog.dismiss();
                            if(integer==0){
                                Toasty.error(PlanPathActivity.this,"Something went wrong. Please try again.",Toast.LENGTH_LONG).show();
                            }else{
                                Toasty.success(PlanPathActivity.this,"Optimal route found successfully.").show();
                            }
                        }
                    }.execute();
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
                    if(checkSameLocations(source,destination)){
                        Toasty.error(PlanPathActivity.this,"Source and destination cannot be same.").show();
                        return;
                    }
                    /*String directionApiPath = Utils.getUrl(String.valueOf(markerSource.getPosition().latitude), String.valueOf(markerSource.getPosition().longitude),
                            String.valueOf(markerDestination.getPosition().latitude), String.valueOf(markerDestination.getPosition().longitude));
                    Log.d("awesome", "Direction api url: " + directionApiPath);*/
                    if(Utils.isNetworkAvailable(PlanPathActivity.this)){
                        progressDialog=new ProgressDialog(PlanPathActivity.this);
                        progressDialog.setCancelable(false);
                        progressDialog.setTitle("Getting route");
                        progressDialog.setMessage("please wait...");
                        progressDialog.show();

                        if(aPolyline!=null){
                            aPolyline.remove();
                            aPolyline=null;
                        }

                        if(dfsPolyline!=null){
                            dfsPolyline.remove();
                            dfsPolyline=null;
                        }

                        if(routePolyline!=null){
                            routePolyline.remove();
                            routePolyline=null;
                        }

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
                                    routePolyline=mMap.addPolyline(polylineOptions.width(5).color(Color.RED));
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
                if(!txtSource.getText().toString().isEmpty()&&!txtDestination.getText().toString().isEmpty()){

                    DatabaseHelper databaseHelper=new DatabaseHelper(PlanPathActivity.this);

                    //Converting datapoints into string
                    String dataPointsString="";

                    for(int i=0;i<dataPoints.size();i++){
                        if(i!=dataPoints.size()-1){
                            String point=dataPoints.get(i).getPosition().latitude+"-"+dataPoints.get(i).getPosition().longitude+",";
                            dataPointsString+=point;
                        }else{
                            String point=dataPoints.get(i).getPosition().latitude+"-"+dataPoints.get(i).getPosition().longitude;
                            dataPointsString+=point;
                        }
                    }

                    //Converting measurements into string
                    String measurementsString="";

                    for(int i=0;i<dataPoints.size();i++){
                        if(i!=dataPoints.size()-1){
                            String point=dataPoints.get(i).getMeasurement()+",";
                            measurementsString+=point;
                        }else{
                            String point=dataPoints.get(i).getMeasurement()+"";
                            measurementsString+=point;
                        }
                    }

                    if(aPolyline!=null){

                        //Converting polypoints into string
                        String polyPointsString="";
                        List<LatLng> points=aPolyline.getPoints();
                        for(int i=0;i<points.size();i++){
                            if(i!=points.size()-1){
                                String point=points.get(i).latitude+"-"+points.get(i).longitude+",";
                                polyPointsString+=point;
                            }else{
                                String point=points.get(i).latitude+"-"+points.get(i).longitude;
                                polyPointsString+=point;
                            }
                        }

                        FavouriteData favouriteData=new FavouriteData(
                                txtSource.getText().toString(),
                                txtDestination.getText().toString(),
                                "A*",
                                polyPointsString,
                                dataPointsString,
                                measurementsString,
                                getIntent().getStringExtra(DashBoardActivity.DATA_TYPE)
                        );

                        if(databaseHelper.insertFavouriteData(favouriteData)){
                            Toasty.success(PlanPathActivity.this,"Favourite data inserted successfully.",Toast.LENGTH_LONG).show();
                        }else{
                            Toasty.error(PlanPathActivity.this,"Something went wrong. Please try again.",Toast.LENGTH_LONG).show();
                        }
                    }else if(dfsPolyline!=null){
                        //Converting polypoints into string
                        String polyPointsString="";
                        List<LatLng> points=dfsPolyline.getPoints();
                        for(int i=0;i<points.size();i++){
                            if(i!=points.size()-1){
                                String point=points.get(i).latitude+"-"+points.get(i).longitude+",";
                                polyPointsString+=point;
                            }else{
                                String point=points.get(i).latitude+"-"+points.get(i).longitude;
                                polyPointsString+=point;
                            }
                        }

                        FavouriteData favouriteData=new FavouriteData(
                                txtSource.getText().toString(),
                                txtDestination.getText().toString(),
                                "DFS",
                                polyPointsString,
                                dataPointsString,
                                measurementsString,
                                getIntent().getStringExtra(DashBoardActivity.DATA_TYPE)
                        );

                        if(databaseHelper.insertFavouriteData(favouriteData)){
                            Toasty.success(PlanPathActivity.this,"Favourite data inserted successfully.",Toast.LENGTH_LONG).show();
                        }else{
                            Toasty.error(PlanPathActivity.this,"Something went wrong. Please try again.",Toast.LENGTH_LONG).show();
                        }

                    }else if(routePolyline!=null){
                        //Converting polypoints into string
                        String polyPointsString="";
                        List<LatLng> points=routePolyline.getPoints();
                        for(int i=0;i<points.size();i++){
                            if(i!=points.size()-1){
                                String point=points.get(i).latitude+"-"+points.get(i).longitude+",";
                                polyPointsString+=point;
                            }else{
                                String point=points.get(i).latitude+"-"+points.get(i).longitude;
                                polyPointsString+=point;
                            }
                        }

                        FavouriteData favouriteData=new FavouriteData(
                                txtSource.getText().toString(),
                                txtDestination.getText().toString(),
                                "None",
                                polyPointsString,
                                dataPointsString,
                                measurementsString,
                                getIntent().getStringExtra(DashBoardActivity.DATA_TYPE)
                        );

                        if(databaseHelper.insertFavouriteData(favouriteData)){
                            Toasty.success(PlanPathActivity.this,"Favourite data inserted successfully.",Toast.LENGTH_LONG).show();
                        }else{
                            Toasty.error(PlanPathActivity.this,"Something went wrong. Please try again.",Toast.LENGTH_LONG).show();
                        }
                    }else{
                        Toasty.warning(PlanPathActivity.this,"Please generate a route first.").show();
                    }
                }else{
                    Toasty.warning(PlanPathActivity.this,"Please select source and destination.").show();
                }

            }
        });
    }

    private void printNode(GridNode destination, ArrayList<TraceNode> traceNodes) {
        for(TraceNode traceNode:traceNodes){
            Log.d("awesome","Node: "+traceNode.node+" parent: "+traceNode.parent);
        }
        Log.d("awesome","node: "+destination);
        GridNode parent=traceNodes.get(traceNodes.indexOf(destination)).parent;
        if(parent==null){
            Log.d("awesome","END");
        }else{
            Log.d("awesome","parent: "+parent);
            printNode(parent,traceNodes);
        }

    }

    private void drawPath(GridNode current) {
        if(current.parent==null){
            Log.d("awesome","I am the start: "+current);
            return;
        }
        Log.d("awesome","current: "+current+" parent: "+current.parent);
        drawPath(current.parent);
        /*final PolylineOptions polylineOptions=new PolylineOptions();
        polylineOptions.color(Color.RED).width(5.0f);
        Log.d("awesome","At start current is: "+current+" and parent is: "+current.parent);
        current.polygon.setFillColor(0x00000000);

        if(current.parent!=null){
            Call<DirectionApiResponse> call=ApiClient.getClient().getPathCoordinates(
                    current.getLocation().latitude+","+
                            current.getLocation().longitude,
                    current.parent.getLocation().latitude+","+
                            current.parent.getLocation().longitude,
                    Utils.API_KEY
            );

            call.enqueue(new Callback<DirectionApiResponse>() {
                @Override
                public void onResponse(Call<DirectionApiResponse> call, Response<DirectionApiResponse> response) {
                    List<Step> steps=response.body().getRoutes().get(0).getLegs().get(0).getSteps();
                    for(Step step:steps){
                        List<LatLng> points=decodePoly(step.getPolyline().getPoints());
                        for(LatLng latLng:points){
                            polylineOptions.add(latLng);
                        }
                    }
                }

                @Override
                public void onFailure(Call<DirectionApiResponse> call, Throwable t) {
                    Toasty.error(PlanPathActivity.this,"Something went wrong while drawing path. Please try again.",Toast.LENGTH_LONG);
                }
            });

            if(dfsPolyline!=null){
                dfsPolyline.remove();
            }
            dfsPolyline=mMap.addPolyline(polylineOptions);

            drawPath(current.parent);
        }else{
            Log.d("awesome","Reached the end of A*");
            Toasty.success(PlanPathActivity.this,"A* algorithm applied successfully");
        }
*/

    }

    private ArrayList<GridNode> getSuccessors(GridNode q,int a, int b) {
        ArrayList<GridNode> successorList=new ArrayList<>();
        for(int x=a-1;x<=a+1;x++){
            for(int y=b-1;y<=b+1;y++){
                if(!(x==a&&y==b)){
                    GridNode successor=getSuccessor(x,y);
                    if(successor!=null){
                        successor.parent=q;
                        successorList.add(successor);
                    }
                }else{
                    Log.d("awesome","It's me");
                }
            }
        }
        Log.d("awesome","Total successors: "+successorList.size());
        return successorList;
    }

    private GridNode getSuccessor(int i, int j) {
        if(i<0||i>=gridList.size()||j<0||j>=gridList.size()){
            Log.d("awesome","Index out of bounds");
            return null;
        }else{
            if(gridList.get(i).get(j).type==GridNode.TYPE_OBSTACLE){
                return null;
            }else{
                return gridList.get(i).get(j);
            }

        }
    }

    private boolean checkSameLocations(LatLng source, LatLng destination) {
        if(source==null||destination==null){
            return false;
        }
        if(source.latitude==destination.latitude&&source.longitude==destination.longitude){
            return true;
        }
        return false;
    }

    private int algoDFS(Node node,Node destinationNode) {
        node.setVisited(true);
        if(node==destinationNode){
            return 0;
        }else{

            int minimumDistance=0;
            Node minimumNode=null;
            for(Node adjacentNode:node.getAdjacencyList()){
                if(adjacentNode.isVisited()){
                    continue;
                }
                try{
                    Call<DirectionApiResponse> call=ApiClient.getClient().getPathCoordinates(
                            node.getLatLng().latitude+","+node.getLatLng().longitude,
                            adjacentNode.getLatLng().latitude+","+adjacentNode.getLatLng().longitude,
                            Utils.API_KEY
                    );

                    DirectionApiResponse directionApiResponse=call.execute().body();
                    int totalDistance=algoDFS(adjacentNode,destinationNode)+directionApiResponse.getRoutes().get(0).getLegs().get(0).getDistance().getValue();
                    if(totalDistance<minimumDistance||minimumDistance==0){
                        minimumDistance=totalDistance;
                        minimumNode=adjacentNode;
                    }
                }catch (Exception e){
                    Log.d("awesome","Exception in finding distance: "+e.toString());
                }
            }
            node.setChild(minimumNode);
            return minimumDistance;
        }
    }

    private boolean betterNodeExists(ArrayList<Node> nodeArrayList, Node n) {
        for(Node node:nodeArrayList){
            if(node.getG()==n.getG()){
                if(node.getF()<n.getF()){
                    return true;
                }
            }
        }
        return false;
    }

    private void removeNodeFromList(ArrayList<Node> nodeArrayList, Node n) {
        for(Node node:nodeArrayList){
            if(node.getLatLng()==n.getLatLng()){
                nodeArrayList.remove(node);
            }
        }
    }

    private boolean listContainsNode(ArrayList<Node> nodeList, Node n) {
        for(Node node:nodeList){
            if(node.getLatLng()==n.getLatLng()){
                return true;
            }
        }
        return false;
    }

    private Node getNodeWithMaxFValue(ArrayList<Node> openList) {
        int max_F_value=openList.get(0).getF();
        for(Node node:openList){
            if(node.getF()>max_F_value){
                max_F_value=node.getF();
            }
        }

        for (Node node:openList){
            if(node.getF()==max_F_value){
                return node;
            }
        }
        return null;
    }

    private GridNode getNodeWithMinFValue(ArrayList<GridNode> nodeList){
        Double minF=nodeList.get(0).g+nodeList.get(0).h;
        int pos=0;
        for(int i=0;i<nodeList.size();i++){
            if(minF>nodeList.get(i).g+nodeList.get(i).h){
                minF=nodeList.get(i).g+nodeList.get(i).h;
                pos=i;
            }
        }
        return nodeList.get(pos);
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

        geoLocate = new GeoLocate(mMap, PlanPathActivity.this, mGoogleApiClient);
        GPSTracker gpsTracker=new GPSTracker(PlanPathActivity.this);
        Location location=gpsTracker.getLocation();
        gotoLocation(location.getLatitude(),location.getLongitude());
        mapGrid=new MapGrid(mMap);

        Handler handler=new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                drawGrid(mMap);
            }
        },3000);



        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                Geocoder gc = new Geocoder(PlanPathActivity.this);
                List<Address> list = null;
                try {
                    list = gc.getFromLocation(latLng.latitude, latLng.longitude, 1);
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
                        source=markerOptions.getPosition();
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
                        destination=markerOptions.getPosition();
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
                        source=null;
                        destination=null;
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

    private void drawGrid(GoogleMap mMap) {LatLng center=mMap.getProjection().getVisibleRegion().latLngBounds.getCenter();
        LatLng start=mMap.getProjection().getVisibleRegion().latLngBounds.southwest;
        LatLng destination=mMap.getProjection().getVisibleRegion().latLngBounds.northeast;
        Log.d("awesome","center: "+center);
        Log.d("awesome","start: "+start);
        Log.d("awesome","destination: "+destination);
        mapGrid.drawGrid(start,destination,CLUSTER_SIZE);
        gridList=mapGrid.getGridList();

        Double minValue;
        Double maxValue;

        minValue=dataPoints.get(0).getMeasurement();
        maxValue=dataPoints.get(0).getMeasurement();

        for(int i=0;i<dataPoints.size();i++){
            Double measurement=dataPoints.get(i).getMeasurement();
            if(minValue>measurement){
                minValue=measurement;
            }

            if(maxValue<measurement){
                maxValue=measurement;
            }
        }

        Log.d("awesome","Got minValue: "+minValue+" and maxValue: "+maxValue);

        for(int i=0;i<dataPoints.size();i++){
            Double x=dataPoints.get(i).getMeasurement();
            int percentage=(int)Math.round((x-minValue)*100/(maxValue-minValue));
            Log.d("awesome","percentage: "+percentage);
            if(percentage==0){
                percentage+=10;
            }
            String alpha=Utils.getAlphaValue(percentage);
            dataPoints.get(i).setAlpha(alpha);

            Boolean dataFound=false;
            for(int a=0;a<gridList.size();a++){
                if(!dataFound){
                    for(int b=0;b<gridList.get(i).size();b++){
                        List<LatLng> points=gridList.get(a).get(b).polygon.getPoints();
                        LatLngBounds latLngBounds=new LatLngBounds(points.get(0),points.get(2));
                        if(latLngBounds.contains(dataPoints.get(i).getPosition())){
                            gridList.get(a).get(b).polygon.setFillColor(Utils.hex2Decimal(alpha+"0000FF"));
                            gridList.get(a).get(b).type=GridNode.TYPE_OBSTACLE;
                            break;

                        }
                    }
                }else{
                    break;
                }

            }
        }
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

            case R.id.clear_map:
                clearMap();
                break;

            default:
                break;
        }
        if (android.R.id.home == item.getItemId()) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void clearMap() {
        if(aPolyline!=null){
            aPolyline.remove();
            aPolyline=null;
        }

        if(dfsPolyline!=null){
            dfsPolyline.remove();
            dfsPolyline=null;
        }

        if(markerSource!=null){
            markerSource.remove();
            markerSource=null;
        }

        if(markerDestination!=null){
            markerDestination.remove();
            markerDestination=null;
        }

        if(routePolyline!=null){
            routePolyline.remove();
            routePolyline=null;
        }
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
                Log.d("awesome", "" + strReturnedAddress.toString());
            } else {
                Log.d("awesome", "No Address returned!");
            }
        } catch (Exception e) {
            Log.d("awesome", "Canont get Address!");
        }
        return strAdd;
    }

    //Find Location From Google place Api
    private void findStartPlace(View view) {
        try {
            Intent intent = new PlaceAutocomplete
                    .IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(this);
            startActivityForResult(intent, START_PLACE_REQUEST_CODE);
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
            startActivityForResult(intent, DESTINATION_PLACE_REQUEST_CODE);
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
        if (requestCode == START_PLACE_REQUEST_CODE) {
            Log.d("awesome","result code: "+resultCode);
            if (resultCode == RESULT_OK) {
                // retrive the data by using getPlace() method.
                place = PlaceAutocomplete.getPlace(this, data);
                source=place.getLatLng();
                if(checkSameLocations(source,destination)){
                    Toasty.warning(PlanPathActivity.this,"Source and destination cannot be same").show();
                    return;
                }
                txtSource.setText(place.getAddress());
                if(markerSource!=null){
                    markerSource.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions()
                        .title(String.valueOf(place.getAddress()))
                        .position(source)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                markerSource =mMap.addMarker(markerOptions);
                markerSource.showInfoWindow();
                gotoLocation(source.latitude,source.longitude);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Log.d("awesome","Result error:"+data);
                Toasty.error(PlanPathActivity.this,"Error in getting location. Try again.").show();
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Log.d("awesome","Start place request cancelled");
            }else{
                Log.d("awesome","Result code: "+resultCode);
                Toasty.error(PlanPathActivity.this,"Something went wrong. Please try again.").show();
            }

        }else if(requestCode==DESTINATION_PLACE_REQUEST_CODE){
            Log.d("awesome","result code: "+resultCode);
            if (resultCode == RESULT_OK) {
                // retrive the data by using getPlace() method.
                place = PlaceAutocomplete.getPlace(this, data);
                destination=place.getLatLng();
                if(checkSameLocations(source,destination)){
                    Toasty.warning(PlanPathActivity.this,"Source and destination cannot be same").show();
                    return;
                }
                txtDestination.setText(place.getAddress());
                if(markerDestination!=null){
                    markerDestination.remove();
                }
                MarkerOptions markerOptions = new MarkerOptions()
                        .title(String.valueOf(place.getAddress()))
                        .position(destination)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                markerDestination =mMap.addMarker(markerOptions);
                markerDestination.showInfoWindow();
                gotoLocation(destination.latitude,destination.longitude);
            } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                Log.d("awesome","Result error:"+data);
                Toasty.error(PlanPathActivity.this,"Error in getting location. Try again.").show();
            } else if (resultCode == RESULT_CANCELED) {
                // The user canceled the operation.
                Log.d("awesome","Destination place request cancelled");
            }else{
                Log.d("awesome","Result code: "+resultCode);
                Toasty.error(PlanPathActivity.this,"Something went wrong. Please try again.").show();
            }
        }

    }

    @Override
    public void onCameraMove() {
        Log.d("Camera zoom", "Zoom " + mMap.getCameraPosition().zoom);
    }

    private double deg2rad(double degrees){
        return Math.PI*degrees/180.0;
    }

    private double rad2deg(double radians){
        return 180.0*radians/Math.PI;
    }

}
