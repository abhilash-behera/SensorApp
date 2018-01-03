package com.sensorapp.retrofit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Abhilash on 17-09-2017
 */

public class ApiClient {
    //public static final String BASE_URL = "http://192.168.166.5:3000";
    public static final String BASE_URL="https://maps.googleapis.com";
    private static ApiInterface apiInterface=null;

    public static ApiInterface getClient() {
        if (apiInterface==null) {
            OkHttpClient.Builder client = new OkHttpClient.Builder();
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            client.addInterceptor(loggingInterceptor);
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client.build())
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            apiInterface=retrofit.create(ApiInterface.class);
        }
        return apiInterface;
    }
}
