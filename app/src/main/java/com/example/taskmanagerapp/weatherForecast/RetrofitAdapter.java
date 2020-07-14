package com.example.taskmanagerapp.weatherForecast;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitAdapter {
    public static String KEY = "6c99302134793d74e1d812832ae6b160";
    public static final String BASE_URL = "http://api.openweathermap.org/data/2.5/";
    private static Retrofit retrofit = null;

    public Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build();
        }
        return retrofit;
    }

}
