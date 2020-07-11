package com.example.taskmanagerapp.weatherForecast;

import com.google.gson.annotations.SerializedName;

import java.util.Calendar;
import java.util.List;

public class WeatherService {
    public class WeatherTemp{
        Double temp;
        Double temp_min;
        Double temp_max;
        Double feels_like;
    }


    public class WeatherDesc {
        String description;
        String icon;
    }

    public class WeatherWind {
        Double speed;
    }

    @SerializedName("main")
    private WeatherTemp temp;

    @SerializedName("weather")
    private List<WeatherDesc> description;

    @SerializedName("name")
    private String city;

    @SerializedName("wind")
    private WeatherWind wind;

    @SerializedName("dt")
    private long timestamp;

    public void WeatherDay(WeatherTemp temp, List<WeatherDesc> description) {
        this.temp = temp;
        this.description = description;
    }

    public Calendar getDate() {
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(timestamp * 1000);
        return date;
    }

    public String getTemp() { return String.valueOf(temp.temp); }

    public String getTempMin() { return String.valueOf(temp.temp_min); }

    public String getTempMax() { return String.valueOf(temp.temp_max); }

    public String getTempInteger() { return String.valueOf(temp.temp.intValue()); }

    public String getTempWithDegree() { return String.valueOf(temp.temp.intValue()) + "\u00B0"; }

    public String getTempFeelsLike() { return String.valueOf(temp.feels_like); }

    public String getWindSpeed() { return String.valueOf(wind.speed); }

    public String getCity() { return city; }

    public String getDescription() { return description.get(0).description.toUpperCase(); }

    public String getIcon() { return description.get(0).icon; }

    public String getIconUrl() {
        return "http://openweathermap.org/img/wn/" + description.get(0).icon + "@4x.png";
    }
}
