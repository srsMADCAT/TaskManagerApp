package com.example.taskmanagerapp.weatherForecast;


import com.example.taskmanagerapp.weatherForecast.POJO.WheatherData;

import io.reactivex.Observable;
import retrofit2.Callback;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface OpenWeatherInterface {
    @GET("weather")
    Observable<WheatherData> getWeatherData(
            @Query("lat") String lat,
            @Query("lon") String lon,
            @Query("units") String units,
            @Query("appid") String appid
    );
}
