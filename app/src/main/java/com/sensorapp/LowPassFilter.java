package com.sensorapp;



class LowPassFilter {

    private static float ALPHA =0.2f;

    public LowPassFilter() {

    }
//filter the campass value..
    public static float[] filter(float[] input,float[] output){

        if (output==null){
            return input;
        }
        for (int i=0;i<input.length;i++){

            output[i]=output[i]+ALPHA*(input[i]-output[i]);
        }

        return output;
    }
}
