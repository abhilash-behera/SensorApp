package com.sensorapp;

import android.content.Context;
import android.hardware.GeomagneticField;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.TextView;

import com.sensorapp.views.CompassView;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

public class CompassActivity extends AppCompatActivity implements SensorEventListener,LocationListener {

    public static final String NA = "N/A";
    public static final String FIXED = "FIXED";
    // location min time
    private static final int LOCATION_MIN_TIME = 30 * 1000;
    // location min distance
    private static final int LOCATION_MIN_DISTANCE = 10;
    // Gravity for accelerometer data
    private float[] gravity = new float[3];
    // magnetic data
    private float[] geomagnetic = new float[3];
    // Rotation data
    private float[] rotation = new float[9];
    // orientation (azimuth, pitch, roll)
    private float[] orientation = new float[3];
    // smoothed values
    private float[] smoothed = new float[3];
    // sensor manager
    private SensorManager sensorManager;
    // sensor gravity
    private Sensor sensorGravity;
    private Sensor sensorMagnetic;
    private LocationManager locationManager;
    private Location currentLocation;
    private GeomagneticField geomagneticField;
    private double bearing = 0;
    private TextView textDirection, textLat, textLong;
    private CompassView compassView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compass);

        //setting action bar..
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Compass");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        //find out text view..and compassview..
        textLat = (TextView) findViewById(R.id.latitude);
        textLong = (TextView) findViewById(R.id.longitude);
        textDirection = (TextView) findViewById(R.id.text);
        compassView = (CompassView) findViewById(R.id.compass);


    }

    @Override
    protected void onStart() {
        super.onStart();
        // Create our Sensor Manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        //Accelerometer sensor..
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //Magnatic field sensor..
        sensorMagnetic = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        // listen to these sensors
        sensorManager.registerListener(this, sensorGravity,
                SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, sensorMagnetic,
                SensorManager.SENSOR_DELAY_NORMAL);

        // location manager from system service ...
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // request location data
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                LOCATION_MIN_TIME, LOCATION_MIN_DISTANCE, this);

        // get last known position
        Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (gpsLocation != null) {
            currentLocation = gpsLocation;
        } else {
            // try with network provider to get last location ..
            Location networkLocation = locationManager
                    .getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (networkLocation != null) {
                currentLocation = networkLocation;
            } else {
                // Fix a position
                currentLocation = new Location(FIXED);
                currentLocation.setAltitude(1);
                currentLocation.setLatitude(43.296482);
                currentLocation.setLongitude(5.36978);
            }

            // set current location
            onLocationChanged(currentLocation);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // remove listeners
        sensorManager.unregisterListener(this, sensorGravity);
        sensorManager.unregisterListener(this, sensorMagnetic);
        locationManager.removeUpdates(this);
    }




    @Override
    public void onSensorChanged(SensorEvent event) {

        boolean accelOrMagnetic = false;

        // get accelerometer data
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            // use a low pass filter to make data smoothed
            smoothed = LowPassFilter.filter(event.values, gravity);
            gravity[0] = smoothed[0];
            gravity[1] = smoothed[1];
            gravity[2] = smoothed[2];
            accelOrMagnetic = true;

        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            smoothed = LowPassFilter.filter(event.values, geomagnetic);
            geomagnetic[0] = smoothed[0];
            geomagnetic[1] = smoothed[1];
            geomagnetic[2] = smoothed[2];
            accelOrMagnetic = true;

        }

        // get rotation matrix to get gravity and magnetic data
        SensorManager.getRotationMatrix(rotation, null, gravity, geomagnetic);
        // get bearing to target
        SensorManager.getOrientation(rotation, orientation);
        // east degrees of true North
        bearing = orientation[0];
        // convert from radians to degrees
        bearing = Math.toDegrees(bearing);

        // fix difference between true North and magnetical North
        if (geomagneticField != null) {
            bearing += geomagneticField.getDeclination();
        }

        // bearing must be in 0-360
        if (bearing < 0) {
            bearing += 360;
        }

        // update compass view
        compassView.setBearing((float) bearing);

        if (accelOrMagnetic) {
            compassView.postInvalidate();
        }

        updateTextDirection(bearing); // display text direction on screen


    }

    private void updateTextDirection(double bearing) {
        int range = (int) (bearing / (360f / 16f));
        String dirTxt = "";

        if (range == 15 || range == 0)
            dirTxt = "N";
        if (range == 1 || range == 2)
            dirTxt = "NE";
        if (range == 3 || range == 4)
            dirTxt = "E";
        if (range == 5 || range == 6)
            dirTxt = "SE";
        if (range == 7 || range == 8)
            dirTxt = "S";
        if (range == 9 || range == 10)
            dirTxt = "SW";
        if (range == 11 || range == 12)
            dirTxt = "W";
        if (range == 13 || range == 14)
            dirTxt = "NW";

        textDirection.setText("" + ((int) bearing) + ((char) 176) + " "
                + dirTxt); // char 176 ) = degrees ...
    }



    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

        if (sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD
                && i == SensorManager.SENSOR_STATUS_UNRELIABLE) {

        }

    }

    @Override
    public void onLocationChanged(Location location) {

        currentLocation = location;
        // used to update location info on screen
        updateLocation(location);
        geomagneticField = new GeomagneticField(
                (float) currentLocation.getLatitude(),
                (float) currentLocation.getLongitude(),
                (float) currentLocation.getAltitude(),
                System.currentTimeMillis());

    }

    // update the location..
    private void updateLocation(Location location) {

        if (FIXED.equals(location.getProvider())) {
            textLat.setText(NA);
            textLong.setText(NA);
        }

        // better => make this creation outside method
        DecimalFormatSymbols dfs = new DecimalFormatSymbols();
        dfs.setDecimalSeparator('.');
        DecimalFormat formatter = new DecimalFormat("#0.00", dfs);
        textLat.setText("Lat : " + formatter.format(location.getLatitude()));
        textLong.setText("Long : " + formatter.format(location.getLongitude()));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(android.R.id.home==item.getItemId()){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
