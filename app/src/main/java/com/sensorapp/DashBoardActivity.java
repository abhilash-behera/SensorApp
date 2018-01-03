package com.sensorapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.Arrays;

import es.dmoral.toasty.Toasty;

public class DashBoardActivity extends AppCompatActivity implements View.OnClickListener {

    CardView cardView1, cardView2, cardView3;
    CoordinatorLayout coordinatorLayout;

    public static final int PERMISSION_CODE = 100;
    String permission[] = new String[]{android.Manifest.permission.RECORD_AUDIO,
            android.Manifest.permission.READ_EXTERNAL_STORAGE,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dash_board);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        checkPermissions();
    }

    private void checkPermissions() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.RECORD_AUDIO)
                + ActivityCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
                + ActivityCompat.checkSelfPermission(this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                + ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                + ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PermissionChecker.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permission, PERMISSION_CODE);
        }else{
            checkInternetConnection();
        }
    }

    private void checkInternetConnection() {
        if(Utils.isNetworkAvailable(DashBoardActivity.this)){
            //find out card view..
            cardView1 = (CardView) findViewById(R.id.noise);
            cardView2 = (CardView) findViewById(R.id.plan);
            cardView3 = (CardView) findViewById(R.id.fabe);

            //register listuner for click
            cardView1.setOnClickListener(this);
            cardView2.setOnClickListener(this);
            cardView3.setOnClickListener(this);
        }else{
            Snackbar.make(coordinatorLayout,"No internet connection",Snackbar.LENGTH_INDEFINITE)
                    .setActionTextColor(Color.RED)
                    .setAction("Retry", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            checkInternetConnection();
                        }
                    })
                    .show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toasty.success(getApplicationContext(), "Permission granted", Toast.LENGTH_LONG).show();
                checkInternetConnection();
            } else {
                Toasty.error(getApplicationContext(), "Permission not granted", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }

    @Override
    public void onClick(View view) {

        if (view == cardView1) {

            // string array to to display on dilog..
            final String[] value = new String[]{
                    "Noise",
                    "Wi-Fi Signal",
                    "Mobile Data",
                    /*"GPS",*/
                    "Gyroscope",
                    "Accelerometer",
                    "Compass"
            };

            // dilog open on click and chose sensor..
            AlertDialog.Builder alBuilder = new AlertDialog.Builder(DashBoardActivity.this, R.style.CustomDialog);
            alBuilder.setTitle("Select the sensor");
            alBuilder.setItems(value, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

                    String selectedText = Arrays.asList(value).get(i);
                    Intent intent = null;
                    if (selectedText.equals("Noise")) {
                        Log.d("awesome","Selected noise");
                        intent = new Intent(getApplicationContext(), RecordNoiseActivity.class);
                        intent.putExtra("Noise", "Noise");
                        startActivity(intent);
                    }
                    else if (selectedText.equals("Wi-Fi Signal")) {
                        intent = new Intent(getApplicationContext(), WifiActivity.class);
                        intent.putExtra("Wifi", "Wi-Fi Signal");
                    }
                    else if (selectedText.equals("Mobile Data")) {
                        intent = new Intent(getApplicationContext(), MobileDataActivity.class);
                        intent.putExtra("Mobile Data", "Mobile Data");
                    }
                    else if (selectedText.equals("GPS")) {
                        intent = new Intent(getApplicationContext(), GpsActivity.class);
                        intent.putExtra("GPS", "GPS");
                    }
                    else if (selectedText.equals("Gyroscope")) {
                        intent = new Intent(getApplicationContext(), GyroscopeActivity.class);
                        intent.putExtra("Gyroscope", "Gyroscope");
                    }
                    else if (selectedText.equals("Accelerometer")) {
                        intent = new Intent(getApplicationContext(), AccelerometerActivity.class);
                        intent.putExtra("Accelerometer", "Accelerometer");
                    }
                    else if (selectedText.equals("Compass")) {
                        intent = new Intent(getApplicationContext(), CompassActivity.class);
                        intent.putExtra("Compass", "Compass");
                    }
                    Log.d("awesome","Starting intent");
                    startActivity(intent);
                }
            });
            alBuilder.show();


        }
        if (view == cardView2) {

            final String[] value = new String[]{
                    "Noise",
                    "Wi-Fi Signal",
                    "Mobile Data"/*,
                    "GPS",*/
            };


            AlertDialog.Builder alBuilder = new AlertDialog.Builder(DashBoardActivity.this, R.style.CustomDialog);
            alBuilder.setTitle("Select the sensor");
            alBuilder.setItems(value, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    ConnectivityManager cm = (ConnectivityManager) getSystemService(DashBoardActivity.this.CONNECTIVITY_SERVICE);
                    String selectedText = Arrays.asList(value).get(i);
                    Intent intent = null;
                    if (selectedText.equals("Noise")) {
                        //redirect to noise recording ....
                        intent = new Intent(getApplicationContext(), PlanPathActivity.class);
                        intent.putExtra("Noise", "Noise");
                        if(Utils.isNetworkAvailable(DashBoardActivity.this)){
                            startActivity(intent);
                        }else{
                            Toasty.info(getApplicationContext(),"Turn ON internet first.",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if (selectedText.equals("Wi-Fi Signal")) {
                        //redirect to wifi signal ....
                        intent = new Intent(getApplicationContext(), PlanPathActivity.class);
                        intent.putExtra("Wifi", "Wi-Fi Signal");
                        if(Utils.isNetworkAvailable(DashBoardActivity.this)){
                            startActivity(intent);
                        }else{
                            Toasty.info(DashBoardActivity.this,"Turn ON internet first.",Toast.LENGTH_SHORT).show();
                        }
                    }
                    else if (selectedText.equals("Mobile Data")) {
                        //redirect to mobile data ....
                        intent = new Intent(getApplicationContext(), PlanPathActivity.class);
                        intent.putExtra("Mobile Data", "Mobile Data");
                        if(Utils.isNetworkAvailable(DashBoardActivity.this)){
                            startActivity(intent);
                        }else{
                            Toasty.info(DashBoardActivity.this,"Turn ON internet first.",Toast.LENGTH_SHORT).show();
                        }

                    }
                    else if (selectedText.equals("GPS")) {
                        //redirect to gps ....
                        intent = new Intent(getApplicationContext(), PlanPathActivity.class);
                        intent.putExtra("GPS", "GPS");
                        if(Utils.isNetworkAvailable(DashBoardActivity.this)){
                            startActivity(intent);
                        }else{
                            Toasty.info(DashBoardActivity.this,"Turn ON internet first.",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            alBuilder.show();
        }
        if (view == cardView3) {
            //sending to favouritesActivity
            startActivity(new Intent(this, FavouritesActivity.class));
        }
    }
}
