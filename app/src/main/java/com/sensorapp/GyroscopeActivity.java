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

public class GyroscopeActivity extends AppCompatActivity implements SensorEventListener,View.OnClickListener {

    private SensorManager mgr;
    private Sensor gyro;
    private TextView xText, yText, zText;
    private LineChart mChart;
    FloatingActionButton btnStart,btnStop,btnPlayRec;

    DatabaseHelper databaseHelper;

    ArrayList<String> xValue=new ArrayList<String>();
    ArrayList<String> yValue=new ArrayList<String>();
    ArrayList<String> zValue=new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gyroscope);

        //setting action bar..
        ActionBar actionBar=getSupportActionBar();
        actionBar.setTitle("Gyroscope");
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        // Create our Sensor Manager
        mgr = (SensorManager) this.getSystemService(SENSOR_SERVICE);
        //Gyroscope sensor..
        gyro = mgr.getDefaultSensor(Sensor.TYPE_GYROSCOPE);


        // Assign TextView
        xText = (TextView)findViewById(R.id.xText);
        yText = (TextView)findViewById(R.id.yText);
        zText = (TextView)findViewById(R.id.zText);

        //find out button instance..
        btnStart=(FloatingActionButton)findViewById(R.id.btnStart);
        btnStop=(FloatingActionButton)findViewById(R.id.btnStop);
        btnPlayRec=(FloatingActionButton)findViewById(R.id.btnPlayRec);

        //registering listener..
        btnStart.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnPlayRec.setOnClickListener(this);

        databaseHelper=new DatabaseHelper(this); //geting instance of Database helper..

        mChart = (LineChart) findViewById(R.id.chart1);  //find out line chart for displaying graph..


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

        //setting x axis..
        XAxis xl = mChart.getXAxis();
        xl.setTextColor(Color.WHITE);
        xl.setDrawGridLines(false);
        xl.setAvoidFirstLastClipping(true);
        xl.setEnabled(true);

        //setting y axis..
        YAxis leftAxis = mChart.getAxisLeft();
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setAxisMaximum(15f);
        leftAxis.setAxisMinimum(-15f);
        leftAxis.setDrawGridLines(true);

        YAxis rightAxis = mChart.getAxisRight();
        rightAxis.setEnabled(false);


    }

    @Override
    protected void onResume() {

        super.onResume();
    }

    @Override
    protected void onPause() {
        mgr.unregisterListener(this, gyro);
        super.onPause();
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        // value added in list
        xValue.add(String.valueOf(sensorEvent.values[0]));
        yValue.add(String.valueOf(sensorEvent.values[1]));
        zValue.add(String.valueOf(sensorEvent.values[2]));

        //update text value..
        xText.setText("" + sensorEvent.values[0]);
        yText.setText("" + sensorEvent.values[1]);
        zText.setText("" + sensorEvent.values[2]);



        LineData data = mChart.getData();

        if (data != null) {

            ILineDataSet set = data.getDataSetByIndex(0);

            if (set == null) {
                set = createSet();
                data.addDataSet(set);
            }

            //ploting the graph
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

    //modifey the line data ..
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

    @Override
    public void onClick(View view) {

        if (view==btnStart){

            // Register sensor Listener
            mgr.registerListener(this, gyro, SensorManager.SENSOR_DELAY_NORMAL);


        }

        if (view==btnStop){

            //unregister the sensor..
            mgr.unregisterListener(this);
        }

        if (view==btnPlayRec){
            try{

                //storing all value in sqlite data base..
                boolean result=databaseHelper.addGListValue(xValue,yValue,zValue);

                if (result == true){

                    Toasty.success(getApplicationContext(),"Data Save Successfully", Toast.LENGTH_LONG).show();
                }


            }catch (Exception ex){
                ex.printStackTrace();
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
