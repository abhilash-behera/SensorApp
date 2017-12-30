package com.sensorapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashMap;

/**
 * Created by Abhilash on 24-11-2017.
 */

public class Utils {
    private static HashMap<Integer,String> alphaValues;
    public static boolean isNetworkAvailable(Context context){
        ConnectivityManager connectivityManager=(ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo=connectivityManager.getActiveNetworkInfo();
        if(networkInfo!=null&&networkInfo.isConnected()){
            return true;
        }else{
            return false;
        }
    }

    public static String getAlphaValue(int percentage){
        if(alphaValues==null){
            alphaValues=new HashMap<>();
            for (double i = 1; i >= 0; i -= 0.01) {
                i = Math.round(i * 100) / 100.0d;
                int alpha = (int) Math.round(i * 255);
                String hex = Integer.toHexString(alpha).toUpperCase();
                if (hex.length() == 1) hex = "0" + hex;
                int percent = (int) (i * 100);
                alphaValues.put(percent,hex);
            }
        }

        return alphaValues.get(percentage);
    }

    public static int hex2Decimal(String s) {
        String digits = "0123456789ABCDEF";
        s = s.toUpperCase();
        int val = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            int d = digits.indexOf(c);
            val = 16*val + d;
        }
        return val;
    }
}
