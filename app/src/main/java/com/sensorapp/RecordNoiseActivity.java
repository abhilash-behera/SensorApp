package com.sensorapp;


import android.graphics.Color;
import android.location.Location;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ColorTemplate;

import java.io.File;
import java.io.IOException;

import es.dmoral.toasty.Toasty;


public class RecordNoiseActivity extends AppCompatActivity implements View.OnClickListener{

    public static final int REPEAT_INTERVAL = 40;
    FloatingActionButton record,stop,play;
    private MediaRecorder recorder = null;
    private boolean isRecording = false;
    TextView tvSoundStatus;
    String newFolder = "MySensorAudio";
    String audioPath;
    DatabaseHelper dbDatabaseHelper;
    double db;
    int avgDb=0;
    long start,end;
    long totalRecordingTime;
    private LineChart mChart;
    private Handler handler;
    private int count;// Handler for updating the visualizer


    //get the output file where you want the recording to be stored
    String filepath = Environment.getExternalStorageDirectory().getPath();



    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            Log.d("awesome","Creating Record noise activity");
            setContentView(R.layout.activity_record_noise);
            setVolumeControlStream(AudioManager.STREAM_MUSIC);

            //setting action bar..
            ActionBar actionBar = getSupportActionBar();
            actionBar.setTitle("Record Noise");
            actionBar.setDisplayHomeAsUpEnabled(true);

            //find out all views here..
            record =(FloatingActionButton)findViewById(R.id.btntoggelRec);
            stop=(FloatingActionButton)findViewById(R.id.btnStop);
            play=(FloatingActionButton)findViewById(R.id.btnPlayRec);
            tvSoundStatus=(TextView)findViewById(R.id.tvSoundStatus);
            record.setOnClickListener(this);
            stop.setOnClickListener(this);
            play.setOnClickListener(this);

            //creating file in phone..
            File file = new File(filepath,newFolder);

            if(!file.exists()){
            file.mkdirs();
            }
            //getting path of audio..
            audioPath = file.getAbsolutePath() + "/" + System.currentTimeMillis() + ".mp3";

            //geting instance of sqlite database ..
            dbDatabaseHelper=new DatabaseHelper(this);

            // create the Handler for visualizer update
            handler = new Handler();

        mChart = (LineChart) findViewById(R.id.chart1);



        // enable description text
        mChart.getDescription().setEnabled(true);

        // enable touch gestures
        mChart.setTouchEnabled(true);

        // enable scaling and dragging
        mChart.setDragEnabled(true);
        mChart.setScaleEnabled(true);
        mChart.setDrawGridBackground(false);





        // if disabled, scaling can be done on x- and y-axis separately
        mChart.setPinchZoom(true);

        // set an alternative background color
        mChart.setBackgroundColor(Color.LTGRAY);

        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        //seting x axis..
        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        //setting y axis..
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(130f);
        leftAxis.setAxisMinimum(-10f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);

    }

    @Override
    public void onClick(View view) {
        if(view==record){
            start= System.currentTimeMillis();
            Log.d("awesome","Start time: "+start);
            startRecording();
        }else if(view==stop){
            end=System.currentTimeMillis();
            Log.d("awesome","End time: "+end);
            totalRecordingTime=end-start;
            Log.d("awesome","Total recording time: "+totalRecordingTime);
            if (isRecording) {
                //calculate average sound db here..
                    /*double db21 = 20 * Math.log10((db / totalRecordingTime));
                    Log.d("awesome","db21: "+db21);
                    avgDb = (int) (Math.round(db21));
                    avgDb=(int)Math.round(db);
                    Log.d("awesome","avgDb: "+avgDb);*/
                tvSoundStatus.setText("" + avgDb + "db");
            }
            releaseRecorder();
        }else if(view==play) {
            Location myLocation = new GPSTracker(RecordNoiseActivity.this).getLocation();
            if (myLocation == null) {
                Toasty.error(RecordNoiseActivity.this, "Unable to get your location. Please check GPS settings.", Toast.LENGTH_LONG).show();
            } else {
                Log.d("awesome", "My Location: " + myLocation);
                NoiseData noiseData = new NoiseData(
                        audioPath,
                        String.valueOf(avgDb),
                        String.valueOf(myLocation.getLatitude()),
                        String.valueOf(myLocation.getLongitude())
                );
                boolean result = dbDatabaseHelper.insertAudio(noiseData);
                if (result) {
                    Toasty.success(getApplicationContext(), "Recording Successfully saved", Toast.LENGTH_LONG).show();
                } else {
                    Log.d("abhilash", "Noise Data not saved to database");
                    Toasty.error(RecordNoiseActivity.this, "Something went wrong. Please try again.").show();
                }
            }
        }
    }

    //stop recording and relese the recoder..
    private void releaseRecorder() {
        if (recorder != null) {

            handler.removeCallbacks(updateVisualizer);
            recorder.stop();
            recorder.reset();
            recorder.release();
            isRecording = false; // stop recording
            recorder = null;

        }
    }
    //start recording..
    public void startRecording(){
        if(!isRecording) {
            count=0;
            db=0.0;
            avgDb=0;
            Log.d("awesome","db initiating with: "+db);
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(audioPath);

            MediaRecorder.OnErrorListener errorListener = null;
            recorder.setOnErrorListener(errorListener);
            MediaRecorder.OnInfoListener infoListener = null;
            recorder.setOnInfoListener(infoListener);

            try {
                recorder.prepare();
                recorder.start();
                isRecording = true; // we are currently recording

            } catch (IllegalStateException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

            handler.post(updateVisualizer); //send to handler for generating the graph..

        }
    }


    @Override
    protected void onDestroy() {

        super.onDestroy();
        releaseRecorder();
    }

    //modify the graph..
    private LineDataSet createSet() {
        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(65);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(true);
        return set;
    }


    // updates the visualizer every 50 milliseconds
    Runnable updateVisualizer = new Runnable() {
        @Override
        public void run() {
            int x;
            if (isRecording)
            {
                // get the current amplitude
                x = recorder.getMaxAmplitude();
                double decibel=20*Math.log10(x);
                Log.d("awesome","decibel: "+decibel);
                    //db=db+x;
                if(decibel>=-100){
                    db+=decibel;
                }
                count++;
                Log.d("awesome","db: "+db);

                avgDb=(int)Math.round(db/count);
                Log.d("awesome","avgDb: "+avgDb);

                LineData data = mChart.getData();

                if (data != null) {

                    ILineDataSet set = data.getDataSetByIndex(0);
                    // set.addEntry(...); // can be called as well

                    if (set == null) {
                        set = createSet();
                        data.addDataSet(set);
                    }

                    /*double db21 = 20 * Math.log10((x/3276));
                    avgDb = (int) (Math.round(db21));*/
                    //drawing graph here..
                    data.addEntry(new Entry(set.getEntryCount(), (float) /*(Math.random() * avgDb) +*/ decibel), 0);
                    data.notifyDataChanged();

                    // let the chart know it's data has changed
                    mChart.notifyDataSetChanged();

                    // limit the number of visible entries
                    mChart.setVisibleXRangeMaximum(8);


                    // move to the latest entry
                    mChart.moveViewToX(data.getEntryCount());

                }


                }

                // update in 40 milliseconds
                handler.postDelayed(this, REPEAT_INTERVAL);
            }


    };

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(android.R.id.home==item.getItemId()){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }



}

