package com.sensorapp.retrofit;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

/**
 * Created by Abhilash on 17-09-2017
 */

public interface ApiInterface {
    @Headers("Content-type:application/json")
    @GET("/maps/api/directions/json")
    Call<DirectionApiResponse> getPathCoordinates(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("key") String key);

    @Headers("Content-type:application/json")
    @GET("/maps/api/directions/json")
    Call<DirectionApiResponse> getPathCoordinatesWithWaypoints(
            @Query("origin") String origin,
            @Query("destination") String destination,
            @Query("waypoints") String waypoints,
            @Query("key") String key);
}
