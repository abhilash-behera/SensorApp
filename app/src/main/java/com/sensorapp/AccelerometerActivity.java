package com.sensorapp;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import java.util.ArrayList;

import es.dmoral.toasty.Toasty;

public class AccelerometerActivity extends AppCompatActivity implements SensorEventListener,View.OnClickListener{

    private TextView xText, yText, zText;
    private Sensor mySensor;
    private SensorManager SM;
    private LineChart mChart;
    FloatingActionButton btnStart,btnStop,btnPlayRec;

    DatabaseHelper databaseHelper=new DatabaseHelper(AccelerometerActivity.this);

    ArrayList<String> xValue=new ArrayList<String>();
    ArrayList<String> yValue=new ArrayList<String>();
    ArrayList<String> zValue=new ArrayList<String>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_accelerometer);

        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Accelerometer");
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        // Create our Sensor Manager
        SM = (SensorManager)getSystemService(SENSOR_SERVICE);

        // Accelerometer Sensor
        mySensor = SM.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        // Assign TextView and button
        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);
        btnStart=(FloatingActionButton)findViewById(R.id.btnStart);
        btnStop=(FloatingActionButton)findViewById(R.id.btnStop);
        btnPlayRec=(FloatingActionButton)findViewById(R.id.btnPlayRec);

        // registring the listuner for click

        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnPlayRec.setOnClickListener(this);

        //geting the database instance og sqlite..
        databaseHelper=new DatabaseHelper(this);

        // find out chart view for drawing graph..

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

        // creating an object of Line Data..
        LineData data = new LineData();
        data.setValueTextColor(Color.WHITE);

        // add empty data
        mChart.setData(data);

        // get the legend (only possible after setting data)
        Legend l = mChart.getLegend();

        // modify the legend ...
        l.setForm(Legend.LegendForm.LINE);
        l.setTextColor(Color.WHITE);

        XAxis xl = mChart.getXAxis();   //gating instance of x axis..
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        YAxis leftAxis = mChart.getAxisLeft(); //gating instance of y axis
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(15f);           //setting max value of y axis
        leftAxis.setAxisMinimum(-15f);          //setting min value of y axis
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();    //setting graph right of y axis..
        rightAxis.setEnabled(false);



    }

    //modifiey the graph here..
    private LineDataSet createSet() {

        LineDataSet set = new LineDataSet(null, "Dynamic Data");
        set.setAxisDependency(YAxis.AxisDependency.LEFT);
        set.setColor(ColorTemplate.getHoloBlue());
        set.setCircleColor(Color.WHITE);
        set.setLineWidth(2f);
        set.setCircleRadius(4f);
        set.setFillAlpha(15);
        set.setFillColor(ColorTemplate.getHoloBlue());
        set.setHighLightColor(Color.rgb(244, 117, 117));
        set.setValueTextColor(Color.WHITE);
        set.setValueTextSize(9f);
        set.setDrawValues(true);
        return set;
    }

    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        xValue.add(String.valueOf(sensorEvent.values[0]));// eting the value of x in array list to store in sqlite
        yValue.add(String.valueOf(sensorEvent.values[1]));// eting the value of y in array list to store in sqlite
        zValue.add(String.valueOf(sensorEvent.values[2]));// eting the value of z in array list to store in sqlite

        xText.setText("" +sensorEvent.values[0]);
        yText.setText("" + sensorEvent.values[1]);
        zText.setText("" + sensorEvent.values[2]);

        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0); //seting line data index to 0


            if (set == null) {
                set = createSet();
                data.addDataSet(set); //adding data to graph
            }

            //seting the value on graph and draw line bitween points
            data.addEntry(new Entry(set.getEntryCount(), (float) (Math.random() * sensorEvent.values[0]) + sensorEvent.values[1]), 0);
            data.notifyDataChanged();

            // let the chart know it's data has changed
            mChart.notifyDataSetChanged();

            // limit the number of visible entries
            mChart.setVisibleXRangeMaximum(8);


            // move to the latest entry
            mChart.moveViewToX(data.getEntryCount());

    }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }


    @Override
    public void onClick(View view) {

        if (view==btnStart){

            // Register sensor Listener
            SM.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);


        }

        if (view==btnStop){
            // unregister sensore
            SM.unregisterListener(this);

        }

        if (view==btnPlayRec){

            //storing value in sqlite data base..
            boolean result=databaseHelper.addListValue(xValue,yValue,zValue);
            if (result == true){

                Toasty.success(getApplicationContext(),"Data Save Successfully", Toast.LENGTH_LONG).show();
            }

        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(android.R.id.home==item.getItemId()){
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
