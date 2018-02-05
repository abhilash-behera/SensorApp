package com.sensorapp;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.HashMap;

import es.dmoral.toasty.Toasty;


public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "SENSORDATA";

    // Table and their columns
    private static final String TABLE_FAVOURITE = "FAVORITE";
    private static final String TABLE_NOISE = "NOISE";
    private static final String TABLE_ACCELEROMETER = "ACCELEROMETER";
    private static final String TABLE_GYROSCOPE = "GYROSCOPE";
    private static final String TABLE_WIFI = "WIFI";
    private static final String TABLE_MOBILE = "MOBILE";
    private static final String TABLE_LATLANG = "KEY_LATLANG";
    private static final String TABLE_GPS = "GPSDATA";

    private static final String KEY_SOURCE = "KEY_SOURCE";
    private static final String KEY_ALGORITHM = "KEY_ALGORITHM";
    private static final String KEY_DESTINATION = "KEY_DESTINATION";
    private static final String KEY_DISTANCE = "KEY_DISTANCE";
    private static final String KEY_AVERAGE_NOISE = "KEY_AVERAGE_NOISE";
    private static final String KEY_AUDIO_PATH = "KEY_AUDIO_PATH";
    private static final String KEY_X_VALUE = "KEY_X_VALUE";
    private static final String KEY_Y_VALUE = "KEY_Y_VALUE";
    private static final String KEY_Z_VALUE = "KEY_Z_VALUE";
    private static final String KEY_CONNECTION = "KEY_CONNECTION";
    private static final String KEY_SPEED = "KEY_SPEED";
    private static final String KEY_NETWORK_TYPE = "KEY_NETWORK_TYPE";
    private static final String KEY_PROGRESS = "KEY_PROGRESS";
    private static final String KEY_RSSI = "KEY_RSSI";
    private static final String KEY_LATLANG = "KEY_LATLANG";
    private static final String KEY_DURATION = "KEY_DURATION";
    private static final String KEY_LATITUDE ="KEY_LATITUDE";
    private static final String KEY_LONGITUDE ="KEY_LONGITUDE";
    private static final String KEY_DATA_COUNT="KEY_DATA_COUNT";
    private static final String KEY_POLYPOINTS ="KEY_POLYPOINTS";
    private static final String KEY_DATAPOINTS="KEY_DATAPOINTS";
    private static final String KEY_MEASUREMENTS="KEY_MEASUREMENTS";
    private static final String KEY_TYPE="KEY_TYPE";
    private Context context;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        this.context=context;
    }

    //Create table here
    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE "
                + TABLE_FAVOURITE
                + "(id INTEGER PRIMARY KEY AUTOINCREMENT,"
                +KEY_SOURCE+" VARCHAR(50), "
                +KEY_DESTINATION+" VARCHAR(50), "
                +KEY_ALGORITHM+" VARCHAR(50), "
                + KEY_POLYPOINTS +" TEXT, "
                +KEY_DATAPOINTS+" TEXT, "
                +KEY_MEASUREMENTS+" TEXT, "
                +KEY_TYPE+" VARCHAR(50))");

        db.execSQL("CREATE TABLE "
                + TABLE_NOISE
                + "(id INTEGER PRIMARY KEY AUTOINCREMENT, "+KEY_AUDIO_PATH+" VARCHAR(100), "+KEY_AVERAGE_NOISE+" VARCHAR(50),"+KEY_LATITUDE+" VARCHAR(15),"+KEY_LONGITUDE+" VARCHAR(15),"+KEY_DATA_COUNT+" INTEGER)");

        db.execSQL("CREATE TABLE "
                + TABLE_ACCELEROMETER
                + "(id INTEGER PRIMARY KEY AUTOINCREMENT, "+KEY_X_VALUE+" VARCHAR(50), "+KEY_Y_VALUE+" VARCHAR(50), "+KEY_Z_VALUE+" VARCHAR(50))");

        db.execSQL("CREATE TABLE "
                + TABLE_GYROSCOPE
                + "(id INTEGER PRIMARY KEY AUTOINCREMENT, "+KEY_X_VALUE+" VARCHAR(50), "+KEY_Y_VALUE+" VARCHAR(50), "+KEY_Z_VALUE+" VARCHAR(50))");

        db.execSQL("CREATE TABLE "
                + TABLE_WIFI
                + "(id INTEGER PRIMARY KEY AUTOINCREMENT, "+KEY_CONNECTION+" VARCHAR(50), "+KEY_SPEED+" VARCHAR(50), "+KEY_NETWORK_TYPE+" VARCHAR(50),"+KEY_PROGRESS+" VARCHAR(50), "+KEY_RSSI+" VARCHAR(50),"+KEY_LATITUDE+" VARCHAR(50),"+KEY_LONGITUDE+" VARCHAR(50),"+KEY_DATA_COUNT+" INTEGER)");

        db.execSQL("CREATE TABLE "
                + TABLE_MOBILE
                + "(id INTEGER PRIMARY KEY AUTOINCREMENT, "+KEY_CONNECTION+" VARCHAR(50), "+KEY_SPEED+" VARCHAR(50), "+KEY_NETWORK_TYPE+" VARCHAR(50),"+KEY_PROGRESS+" VARCHAR(50),"+KEY_LATITUDE+" VARCHAR(50),"+KEY_LONGITUDE+" VARCHAR(50),"+KEY_DATA_COUNT+" INTEGER)");

        db.execSQL("CREATE TABLE "
                + TABLE_LATLANG
                + "(id INTEGER PRIMARY KEY AUTOINCREMENT, "+KEY_LATLANG+" VARCHAR(5000))");

        db.execSQL("CREATE TABLE "
                + TABLE_GPS
                + "(id INTEGER PRIMARY KEY AUTOINCREMENT, "+KEY_SOURCE+" VARCHAR(50), "+KEY_DESTINATION+" VARCHAR(50), "+KEY_DISTANCE+" VARCHAR(50), "+KEY_DURATION+" VARCHAR(50),"+KEY_LATITUDE+" VARCHAR(50),"+KEY_LONGITUDE+" VARCHAR(50),"+KEY_DATA_COUNT+" INTEGER)");

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {


        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_FAVOURITE);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_NOISE);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCELEROMETER);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_GYROSCOPE);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_WIFI);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MOBILE);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_LATLANG);

        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_GPS);

        onCreate(sqLiteDatabase);
    }


    //Get all noise data from database
    public ArrayList<NoiseData> getNoiseData(){
        SQLiteDatabase db=this.getReadableDatabase();
        ArrayList<NoiseData> list=new ArrayList<>();
        Cursor c=db.rawQuery("SELECT * FROM "+TABLE_NOISE,null);
        while (c.moveToNext()){
            NoiseData data=new NoiseData(c.getString(1),c.getString(2),c.getString(3),c.getString(4));
            list.add(data);
        }
        return list;
    }

    //Get all wifi data from database
    public ArrayList<WifiData> getWifiData(){
        ArrayList<WifiData> wifiDataList=new ArrayList<>();
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+ TABLE_WIFI,null);
        while(c.moveToNext()){
            WifiData wifiData=new WifiData(
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5),
                    c.getString(6),
                    c.getString(7)
            );
            wifiDataList.add(wifiData);
        }
        return wifiDataList;
    }

    //Get all mobile data from database
    public ArrayList<MobileData> getMobileData(){
        ArrayList<MobileData> mobileDataList=new ArrayList<>();
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+ TABLE_MOBILE,null);
        while(c.moveToNext()){
            MobileData mobileData=new MobileData(
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5),
                    c.getString(6)
            );
            mobileDataList.add(mobileData);
        }

        return mobileDataList;
    }

    //Get all favourite data from database
    public ArrayList<FavouriteData> getFavouriteData(){
        ArrayList<FavouriteData> favouriteDataList=new ArrayList<>();
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+TABLE_FAVOURITE,null);
        while(c.moveToNext()){
            FavouriteData favouriteData=new FavouriteData(
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5),
                    c.getString(6),
                    c.getString(7)
            );

            favouriteData.setId(c.getInt(0));

            favouriteDataList.add(favouriteData);
        }

        c.close();
        return favouriteDataList;
    }

    public FavouriteData getFavouriteRouteById(int id){
        SQLiteDatabase db=this.getReadableDatabase();
        Log.d("awesome","Finding favourite data with id: "+id);
        String sql="SELECT * FROM "+TABLE_FAVOURITE+" WHERE id=?";

        Cursor c=db.rawQuery(sql,new String[]{String.valueOf(id)});
        if(c.moveToFirst()){
            c.moveToFirst();
            FavouriteData favouriteData=new FavouriteData(
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5),
                    c.getString(6),
                    c.getString(7)
            );

            return favouriteData;
        }else{
            Toasty.error(context,"Unable to get route with id: "+id, Toast.LENGTH_LONG).show();
            return null;
        }
    }

    public ArrayList<GPSData> getGpsData(){
        ArrayList<GPSData> gpsDataList=new ArrayList<>();
        SQLiteDatabase db=this.getReadableDatabase();
        Cursor c=db.rawQuery("SELECT * FROM "+ TABLE_GPS,null);
        while(c.moveToNext()){
            GPSData gpsData=new GPSData(
                    c.getString(1),
                    c.getString(2),
                    c.getString(3),
                    c.getString(4),
                    c.getString(5),
                    c.getString(6));
            gpsDataList.add(gpsData);
        }
        return gpsDataList;
    }

    private double deg2rad(double degrees){
        return Math.PI*degrees/180.0;
    }


    //Insert data AudioTable
    public boolean insertAudio(NoiseData noiseData) {
        SQLiteDatabase db = this.getWritableDatabase();

        Double newLatitude=Double.valueOf(noiseData.getLatitude());
        Double newLongitude=Double.valueOf(noiseData.getLongitude());


        Cursor c=db.rawQuery("SELECT * FROM "+TABLE_NOISE,null);
        while(c.moveToNext()){
            Double latitude=Double.valueOf(c.getString(3));
            Double longitude=Double.valueOf(c.getString(4));

            //Getting 1 km bound with this point as center
            double maxLatitude=latitude+0.00062881;
            double minLatitude=latitude-0.00062881;
            double lngBuffer=0.07*(350/(Math.cos(deg2rad(latitude))*40075));
            double maxLongitude=longitude+lngBuffer;
            double minLongitude=longitude-lngBuffer;
            if((newLatitude>=minLatitude&&newLatitude<=maxLatitude)&&(newLongitude>=minLongitude&&newLongitude<=maxLongitude)){
                Log.d("awesome","Point exists in bounds so updating data");
                int count=c.getInt(5);
                Log.d("awesome","previous count:"+count);

                Double totalValue=Double.valueOf(c.getString(2))*count;
                Log.d("awesome","total value:"+totalValue);

                Double newValue=totalValue+Double.valueOf(noiseData.getAvgDb());
                Log.d("awesome","new value: "+newValue);

                Double newAvg=newValue/(count+1);
                Log.d("awesome","new avg: "+newAvg);

                ContentValues cv=new ContentValues();
                cv.put(KEY_AUDIO_PATH,noiseData.getAudioPath());
                cv.put(KEY_AVERAGE_NOISE,String.valueOf(newAvg));
                cv.put(KEY_DATA_COUNT,count+1);
                Log.d("awesome","Updating values: "+cv);
                int result=db.update(TABLE_NOISE,cv,KEY_LATITUDE+"=? AND "+KEY_LONGITUDE+"=?",new String[]{c.getString(3),c.getString(4)});
                if(result>0){
                    Log.d("awesome","Noise data updated");
                    return true;
                }else{
                    Log.d("awesome","Error in updating noise data.");
                    return false;
                }
            }
        }

        c.close();

        ContentValues cv = new ContentValues();
        cv.put(KEY_AUDIO_PATH, noiseData.getAudioPath());
        cv.put(KEY_AVERAGE_NOISE, noiseData.getAvgDb());
        cv.put(KEY_LATITUDE,String.valueOf(noiseData.getLatitude()));
        cv.put(KEY_LONGITUDE,String.valueOf(noiseData.getLongitude()));
        cv.put(KEY_DATA_COUNT,1);

        Log.d("awesome","Inserting values: "+cv);

        if(db.insert(TABLE_NOISE,null,cv)==-1){
            Log.d("awesome","Error in inserting noise data.");
            return false;
        }else{
            Log.d("awesome","Noise data inserted successfully.");
            return true;
        }
    }

    //Insert data WifiTable
    public boolean insertWifiData(WifiData wifiData) {
        SQLiteDatabase db = this.getWritableDatabase();

        Double newLatitude=Double.valueOf(wifiData.getLatitude());
        Double newLongitude=Double.valueOf(wifiData.getLongitude());

        Cursor c=db.rawQuery("SELECT * FROM "+TABLE_WIFI,null);
        while(c.moveToNext()){
            Double latitude=Double.valueOf(c.getString(6));
            Double longitude=Double.valueOf(c.getString(7));

            double maxLatitude=latitude+0.00062881;
            double minLatitude=latitude-0.00062881;
            double lngBuffer=0.07*(350/(Math.cos(deg2rad(latitude))*40075));
            double maxLongitude=longitude+lngBuffer;
            double minLongitude=longitude-lngBuffer;
            if((newLatitude>=minLatitude&&newLatitude<=maxLatitude)&&(newLongitude>=minLongitude&&newLongitude<=maxLongitude)){
                Log.d("awesome","Point exists in bounds so updating data");
                int count=c.getInt(8);
                Log.d("awesome","previous count:"+count);

                Double totalValue=Double.valueOf(c.getString(2))*count;
                Log.d("awesome","total value:"+totalValue);

                Double newValue=totalValue+Double.valueOf(wifiData.getSpeed());
                Log.d("awesome","new value: "+newValue);

                Double newAvg=newValue/(count+1);
                Log.d("awesome","new avg: "+newAvg);

                ContentValues cv=new ContentValues();
                cv.put(KEY_SPEED,String.valueOf(newAvg));
                cv.put(KEY_DATA_COUNT,count+1);
                Log.d("awesome","Updating values: "+cv);
                int result=db.update(TABLE_WIFI,cv,KEY_LATITUDE+"=? AND "+KEY_LONGITUDE+"=?",new String[]{c.getString(6),c.getString(7)});
                if(result>0){
                    Log.d("awesome","Wifi data updated");
                    return true;
                }else{
                    Log.d("awesome","Error in updating wifi data.");
                    return false;
                }
            }
        }

        c.close();

        ContentValues cv = new ContentValues();
        cv.put(KEY_CONNECTION,wifiData.getConnection());
        cv.put(KEY_SPEED, wifiData.getSpeed());
        cv.put(KEY_NETWORK_TYPE, wifiData.getNetwork());
        cv.put(KEY_PROGRESS, wifiData.getProgress());
        cv.put(KEY_RSSI, wifiData.getRssi());
        cv.put(KEY_LATITUDE,wifiData.getLatitude());
        cv.put(KEY_LONGITUDE,wifiData.getLongitude());
        cv.put(KEY_DATA_COUNT,1);

        Log.d("awesome","Inserting values: "+cv);

        if(db.insert(TABLE_WIFI,null,cv)==-1){
            Log.d("awesome","Failed to insert wifi data");
            return false;
        }else{
            Log.d("awesome","Wifi data inserted successfully.");
            return true;
        }
    }

    //Insert data MobileTable
    public boolean insertMobileData(MobileData mobileData) {
        SQLiteDatabase db = this.getWritableDatabase();

        Double newLatitude=Double.valueOf(mobileData.getLatitude());
        Double newLongitude=Double.valueOf(mobileData.getLongitude());

        Cursor c=db.rawQuery("SELECT * FROM "+TABLE_MOBILE,null);
        while(c.moveToNext()){
            Double latitude=Double.valueOf(c.getString(5));
            Double longitude=Double.valueOf(c.getString(6));

            double maxLatitude=latitude+0.00062881;
            double minLatitude=latitude-0.00062881;
            double lngBuffer=0.07*(350/(Math.cos(deg2rad(latitude))*40075));
            double maxLongitude=longitude+lngBuffer;
            double minLongitude=longitude-lngBuffer;
            if((newLatitude>=minLatitude&&newLatitude<=maxLatitude)&&(newLongitude>=minLongitude&&newLongitude<=maxLongitude)){
                Log.d("awesome","Point exists in bounds so updating data");
                int count=c.getInt(7);
                Log.d("awesome","previous count:"+count);

                Double totalValue=Double.valueOf(c.getString(2).replace(" ","").replace("kb/s",""))*count;
                Log.d("awesome","total value:"+totalValue);

                Double newValue=totalValue+Double.valueOf(mobileData.getSpeed().replace(" ","").replace("kb/s",""));
                Log.d("awesome","new value: "+newValue);

                Double newAvg=newValue/(count+1);
                Log.d("awesome","new avg: "+newAvg);

                ContentValues cv=new ContentValues();
                cv.put(KEY_SPEED,String.valueOf(newAvg));
                cv.put(KEY_DATA_COUNT,count+1);
                Log.d("awesome","Updating values: "+cv);
                int result=db.update(TABLE_MOBILE,cv,KEY_LATITUDE+"=? AND "+KEY_LONGITUDE+"=?",new String[]{c.getString(5),c.getString(6)});
                if(result>0){
                    Log.d("awesome","Mobile data updated");
                    return true;
                }else{
                    Log.d("awesome","Error in updating mobile data.");
                    return false;
                }
            }
        }

        c.close();

        ContentValues cv = new ContentValues();
        cv.put(KEY_CONNECTION,mobileData.getConnection());
        cv.put(KEY_SPEED, mobileData.getSpeed());
        cv.put(KEY_NETWORK_TYPE, mobileData.getNetwork());
        cv.put(KEY_PROGRESS, mobileData.getProgress());
        cv.put(KEY_LATITUDE,mobileData.getLatitude());
        cv.put(KEY_LONGITUDE,mobileData.getLongitude());
        cv.put(KEY_DATA_COUNT,1);

        Log.d("awesome","Inserting values: "+cv);

        if(db.insert(TABLE_MOBILE,null,cv)==-1){
            Log.d("awesome","Failed to insert mobile data");
            return false;
        }else{
            Log.d("awesome","mobile data inserted successfully.");
            return true;
        }
    }

    //Insert data FavouriteData
    public boolean insertFavouriteData(FavouriteData favouriteData){
        SQLiteDatabase db=this.getWritableDatabase();

        ContentValues cv=new ContentValues();
        cv.put(KEY_SOURCE,favouriteData.getSource());
        cv.put(KEY_DESTINATION,favouriteData.getDestination());
        cv.put(KEY_ALGORITHM,favouriteData.getAlgorithm());
        cv.put(KEY_POLYPOINTS,favouriteData.getPolyPoints());
        cv.put(KEY_DATAPOINTS,favouriteData.getDataPoints());
        cv.put(KEY_MEASUREMENTS,favouriteData.getMeasurements());
        cv.put(KEY_TYPE,favouriteData.getType());

        Log.d("awesome","Inserting values: "+cv);

        if(db.insert(TABLE_FAVOURITE,null,cv)==-1){
            Log.d("awesome","Failed to insert favourite data");
            return false;
        }else{
            Log.d("awesome","Favourite data inserted successfully");
            return true;
        }
    }

    //Insert data GPSDATA
    public boolean insertGpsData(GPSData gpsData) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(KEY_SOURCE, gpsData.getSource());
        cv.put(KEY_DESTINATION, gpsData.getDestination());
        cv.put(KEY_DISTANCE, gpsData.getDistance());
        cv.put(KEY_DURATION, gpsData.getDuration());
        cv.put(KEY_LATITUDE,gpsData.getLatitude());
        cv.put(KEY_LONGITUDE,gpsData.getLongitude());


        long result = db.insert(TABLE_GPS, null, cv);

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }


    public void removeSingleContact(String id) {
        //Open the database
        SQLiteDatabase database = this.getWritableDatabase();

        //Execute sql query to remove from database
        //NOTE: When removing by String in SQL, value must be enclosed with ''
        database.execSQL("DELETE FROM " + TABLE_FAVOURITE + " WHERE id = '" + id + "'");

        //Close the database
        database.close();
    }

    //Insert data FavouriteData
    public boolean insertFabData(String source, String destination, String distance, String averageNoise, String algorithm) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(KEY_SOURCE, source);
        cv.put(KEY_DESTINATION, destination);
        cv.put(KEY_DISTANCE, distance);
        cv.put(KEY_AVERAGE_NOISE, averageNoise);
        cv.put(KEY_ALGORITHM, algorithm);

        long result = db.insert(TABLE_FAVOURITE, null, cv);

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    public boolean insertFabData(String source, String destination, String distance, String averageNoise) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put(KEY_SOURCE, source);
        cv.put(KEY_DESTINATION, destination);
        cv.put(KEY_DISTANCE, distance);
        cv.put(KEY_AVERAGE_NOISE, averageNoise);

        long result = db.insert(TABLE_FAVOURITE, null, cv);

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    // insert list data to Accelero
    public boolean addListValue(ArrayList<String> xValue, ArrayList<String> yValue, ArrayList<String> zValue) {
        Log.e("list", xValue.toString());
        Log.e("list", yValue.toString());
        Log.e("list", zValue.toString());

        int size = xValue.size();
        long result = -1;

        SQLiteDatabase db = getWritableDatabase();
        try {
            for (int i = 0; i < size; i++) {
                ContentValues cv = new ContentValues();
                cv.put(KEY_X_VALUE, xValue.get(i));
                cv.put(KEY_Y_VALUE, yValue.get(i));
                cv.put(KEY_Z_VALUE, zValue.get(i));
                Log.e("Added ", "" + cv);
                result = db.insert(TABLE_ACCELEROMETER, null, cv);

            }
            db.close();
        } catch (Exception e) {
            Log.e("Problem", e + " ");
        }

        if (result == -1) {
            return false;
        } else {
            return true;
        }
    }

    // insert list data to Gyroscope
    public boolean addGListValue(ArrayList<String> xValue, ArrayList<String> yValue, ArrayList<String> zValue) {
        Log.e("list", xValue.toString());
        Log.e("list", yValue.toString());
        Log.e("list", zValue.toString());

        int size = xValue.size();
        long result = -1;

        SQLiteDatabase db = getWritableDatabase();
        try {
            for (int i = 0; i < size; i++) {
                ContentValues cv = new ContentValues();
                cv.put(KEY_X_VALUE, xValue.get(i));
                cv.put(KEY_Y_VALUE, yValue.get(i));
                cv.put(KEY_Z_VALUE, zValue.get(i));
                Log.e("Added ", "" + cv);
                result = db.insert(TABLE_GYROSCOPE, null, cv);
            }
            db.close();
        } catch (Exception e) {
            Log.e("Problem", e + " ");
        }

        if (result == -1) {
            return false;
        } else {
            return true;
        }

    }

    public boolean deleteFavValue(int id){
        SQLiteDatabase db=this.getWritableDatabase();
        if(db.delete(TABLE_FAVOURITE,"id=?",new String[]{String.valueOf(id)})>0){
            Log.d("awesome","Favourite route deleted successfully");
            return true;
        }else{
            Log.d("awesome","Error in deleting Favourite route");
            return false;
        }
    }

}
