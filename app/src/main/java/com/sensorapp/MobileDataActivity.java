package com.sensorapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;

import es.dmoral.toasty.Toasty;


public class MobileDataActivity extends AppCompatActivity {
    //Private fields
    private static final String TAG = WifiActivity.class.getSimpleName();
    private static final int EXPECTED_SIZE_IN_BYTES = 1048576;//1MB 1024*1024

    private static final double EDGE_THRESHOLD = 176.0;
    private static final double BYTE_TO_KILOBIT = 0.0078125;
    private static final double KILOBIT_TO_MEGABIT = 0.0009765625;

    private FloatingActionButton mBtnStart,btnStop,btnPlayRec;
    private TextView mTxtSpeed;
    private TextView mTxtConnectionSpeed;
    private TextView mTxtProgress;
    private TextView mTxtNetwork;
    CoordinatorLayout coordinatorLayout;
    int status;

    private final int MSG_UPDATE_STATUS=0;
    private final int MSG_UPDATE_CONNECTION_TIME=1;
    private final int MSG_COMPLETE_STATUS=2;

    private final static int UPDATE_THRESHOLD=300;

    private DecimalFormat mDecimalFormater;

    private ProgressBar mRunningBar;

    DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_data);
        coordinatorLayout=(CoordinatorLayout)findViewById(R.id.coordinatorLayout);
        //network connection check..
        ConnectivityManager cm = (ConnectivityManager) getSystemService(MobileDataActivity.this.CONNECTIVITY_SERVICE);
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

        //set action bar..
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Mobile Data");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        databaseHelper=new DatabaseHelper(this); //get instance of database of sqlite..


        mDecimalFormater=new DecimalFormat("##.##");

        //checking mobile data is enable or not..
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();

        if(isConnected)
        {
            if(activeNetwork.getType()==ConnectivityManager.TYPE_MOBILE)
            {
                bindListeners(); //initlisize the views..
            }else{

                showSettingsAlert();
            }

        }

        else

            showSettingsAlert();
    }

    //dilog to redirect the user to mobile data setting..
    public void showSettingsAlert(){

        AlertDialog.Builder builder = new AlertDialog.Builder(MobileDataActivity.this,R.style.CustomDialog);
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
    private void bindListeners() {

        mBtnStart = (FloatingActionButton) findViewById(R.id.btnStart);
        btnStop = (FloatingActionButton) findViewById(R.id.btnStop);
        btnPlayRec = (FloatingActionButton) findViewById(R.id.btnPlayRec);
        mTxtSpeed = (TextView) findViewById(R.id.speed);
        mTxtConnectionSpeed = (TextView) findViewById(R.id.connectionspeeed);
        mTxtProgress = (TextView) findViewById(R.id.progress);
        mTxtNetwork = (TextView) findViewById(R.id.networktype);
        mRunningBar = (ProgressBar)findViewById(R.id.runningBar);

        mBtnStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(final View view) {
                mRunningBar.setVisibility(ProgressBar.VISIBLE);
                mTxtSpeed.setText("Test started");
                mBtnStart.setEnabled(false);
                mTxtNetwork.setText(R.string.network_detecting);
                new Thread(mWorker).start();
            }
        });

        btnPlayRec.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //store the result in database...
                GPSTracker tracker=new GPSTracker(MobileDataActivity.this);
                Location location=tracker.getLocation();
                if(location==null){
                    Toasty.error(MobileDataActivity.this,"Could not get your location. Please check GPS settings",Toast.LENGTH_LONG).show();
                }else{
                    Log.d("awesome","Got location: "+location);

                    MobileData mobileData=new MobileData(
                            mTxtConnectionSpeed.getText().toString(),
                            mTxtSpeed.getText().toString().replace(" ","").replace("kb/s",""),
                            mTxtNetwork.getText().toString(),
                            mTxtProgress.getText().toString(),
                            String.valueOf(location.getLatitude()),
                            String.valueOf(location.getLongitude())
                    );

                    if (databaseHelper.insertMobileData(mobileData)){
                        Toasty.success(getApplicationContext(),"Data Save Successfully", Toast.LENGTH_LONG).show();
                    }else{
                        Toasty.error(getApplicationContext(),"Something went wrong. Please try again.",Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    private final Handler mHandler=new Handler(){
        @Override
        public void handleMessage(final Message msg) {

            //setting result to view..
            switch(msg.what){
                case MSG_UPDATE_STATUS:
                    final SpeedInfo info1=(SpeedInfo) msg.obj;
                    mTxtSpeed.setText(String.format(getResources().getString(R.string.update_speed), mDecimalFormater.format(info1.kilobits)));
                    // Title progress is in range 0..10000
                    mRunningBar.setProgress(100 * msg.arg1);
                    mTxtProgress.setText(String.format(getResources().getString(R.string.update_downloaded), String.valueOf(msg.arg2)));
                    break;
                case MSG_UPDATE_CONNECTION_TIME:
                    mTxtConnectionSpeed.setText(String.format(getResources().getString(R.string.update_connectionspeed), String.valueOf(msg.arg1)));
                    break;
                case MSG_COMPLETE_STATUS:
                    final  SpeedInfo info2=(SpeedInfo) msg.obj;
                    mTxtSpeed.setText(String.format(getResources().getString(R.string.update_downloaded_complete),String.valueOf(info2.kilobits)));

                    mTxtProgress.setText(String.format(getResources().getString(R.string.update_downloaded), String.valueOf(info2.kilobits)));

                    if(networkType(info2.kilobits)==1){
                        mTxtNetwork.setText(R.string.network_3g);
                    }else{
                        mTxtNetwork.setText(R.string.network_edge);
                    }

                    mBtnStart.setEnabled(true);
                    mRunningBar.setVisibility(ProgressBar.GONE);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    };

    // Our Slave worker that does actually all the work

    private final Runnable mWorker=new Runnable(){

        @Override
        public void run() {
            InputStream stream=null;
            try {
                int bytesIn=0;
                String downloadFileUrl="https://www.google.co.in/url?sa=i&rct=j&q=&esrc=s&source=images&cd=&cad=rja&uact=8&ved=0ahUKEwicgpLxk-XSAhVKqY8KHfgiAhgQjRwIBw&url=http%3A%2F%2Ftechmeasy.blogspot.com%2F2013%2F05%2Fhide-data-text-file-without-software.html&psig=AFQjCNEBGr3jsjSIOavGjOW62rx-ENxR1A&ust=1490101778770166";
                long startCon=System.currentTimeMillis();
                URL url=new URL(downloadFileUrl);
                URLConnection con=url.openConnection();
                con.setUseCaches(false);
                long connectionLatency=System.currentTimeMillis()- startCon;
//                stream=new FileInputStream(downloadFileUrl);
                stream=con.getInputStream();
                Message msgUpdateConnection=Message.obtain(mHandler, MSG_UPDATE_CONNECTION_TIME);
                msgUpdateConnection.arg1=(int) connectionLatency;
                mHandler.sendMessage(msgUpdateConnection);
                long start=System.currentTimeMillis();
                int currentByte=0;
                long updateStart=System.currentTimeMillis();
                long updateDelta=0;
                int  bytesInThreshold=0;

                while((currentByte=stream.read())!=-1){
                    bytesIn++;
                    bytesInThreshold++;
                    if(updateDelta>=UPDATE_THRESHOLD){
                        int progress=(int)((bytesIn/(double)EXPECTED_SIZE_IN_BYTES)*100);
                        Message msg=Message.obtain(mHandler, MSG_UPDATE_STATUS, calculate(updateDelta, bytesInThreshold));
                        msg.arg1=progress;
                        msg.arg2=bytesIn;
                        mHandler.sendMessage(msg);
                        //Reset
                        updateStart=System.currentTimeMillis();
                        bytesInThreshold=0;

                    }
                    updateDelta = System.currentTimeMillis()- updateStart;
                    Log.e("updateDelta", String.valueOf(updateDelta));
                }

                long downloadTime=(System.currentTimeMillis()-start);
                //Prevent AritchmeticException
                if(downloadTime==0){
                    downloadTime=1;
                }

                Message msg=Message.obtain(mHandler, MSG_COMPLETE_STATUS, calculate(downloadTime, bytesIn));
                Log.e("Message", String.valueOf(msg));
                msg.arg1=bytesIn;
                mHandler.sendMessage(msg);
            }
            catch (MalformedURLException e) {
                Log.e(TAG, e.getMessage());
            } catch (IOException e) {
                Log.e(TAG, e.getMessage());
            }finally{
                try {
                    if(stream!=null){
                        stream.close();
                    }
                } catch (IOException e) {
                    //Suppressed
                }
            }

        }
    };

    /**
     * Get Network type from download rate
     0 for Edge and 1 for 3G
     */
    private int networkType(final double kbps){
        int type=1;//3G
        //Check if its EDGE
        if(kbps<EDGE_THRESHOLD){
            type=0;
        }
        return type;
    }



//     1 byte = 0.0078125 kilobits
//      1 kilobits = 0.0009765625 megabit
//
//      downloadTime in miliseconds
//      bytesIn number of bytes downloaded
//      SpeedInfo containing current speed

    private SpeedInfo calculate(final long downloadTime, final long bytesIn){
        SpeedInfo info=new SpeedInfo();
        //from mil to sec
        long bytespersecond   =(bytesIn / downloadTime) * 1000;
        double kilobits=bytespersecond * BYTE_TO_KILOBIT;
        double megabits=kilobits  * KILOBIT_TO_MEGABIT;
        info.downspeed=bytespersecond;
        info.kilobits=kilobits;
        info.megabits=megabits;

        return info;
    }

//     Transfer Object

    private static class SpeedInfo{
        public double kilobits=0;
        public double megabits=0;
        public double downspeed=0;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(android.R.id.home==item.getItemId()){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
