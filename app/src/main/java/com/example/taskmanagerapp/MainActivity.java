package com.example.taskmanagerapp;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.taskmanagerapp.db.DatabaseClient;
import com.example.taskmanagerapp.db.Task;
import com.example.taskmanagerapp.weatherForecast.GpsTracker;
import com.example.taskmanagerapp.weatherForecast.WeatherAPI;
import com.example.taskmanagerapp.weatherForecast.WeatherService;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class MainActivity extends AppCompatActivity {

    private boolean isFragmentDisplayed = false;
    private static final String TAG = "WEATHER";
    private RecyclerView recyclerView;
    private ImageView wcIcon;
    private CardView cardView;
    private TextView wcTemp, wcTemFL, wcCity, wcDesc, wcWind, currentDate;
    private WeatherAPI.ApiInterface api;
    private GpsTracker gpsTracker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            if (ContextCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 101);
            }
        } catch (Exception e){
            e.printStackTrace();
        }

        FloatingActionButton addTask = (FloatingActionButton) findViewById(R.id.floating_add_button);

        recyclerView = findViewById(R.id.recyclerview_tasks);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        wcTemp = (TextView) findViewById(R.id.whether_temp);
        wcTemFL = (TextView) findViewById(R.id.whether_temp_feel);
        wcCity = (TextView) findViewById(R.id.city);
        wcDesc = (TextView) findViewById(R.id.description);
        wcWind = (TextView) findViewById(R.id.whether_wind);
        wcIcon = (ImageView) findViewById(R.id.whether_icon);
        currentDate = (TextView) findViewById(R.id.currentDate);


        api = WeatherAPI.getClient().create(WeatherAPI.ApiInterface.class);

        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFragmentDisplayed) {
                    displayFragment();
                } else {
                    closeFragment();
                }
            }
        });

        getTasks();
        getWeather();

    }

    public void displayFragment() {
        AddTaskFrag fragment = AddTaskFrag.newInstance();
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        fragmentTransaction.add(R.id.fragmentContainer, fragment).addToBackStack(null).commit();
        isFragmentDisplayed = true;
    }

    public void closeFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AddTaskFrag fragment = (AddTaskFrag) fragmentManager.findFragmentById(R.id.fragmentContainer);

        if (fragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragment).commit();

        }

        isFragmentDisplayed = false;
    }

    public void setFragmentDisplayedFalse() {
        isFragmentDisplayed = false;
    }

    private void getTasks() {
        class GetTasks extends AsyncTask<Void, Void, List<Task>> {

            @Override
            protected List<Task> doInBackground(Void... voids) {
                List<Task> taskList = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .taskDao()
                        .getAll();
                return taskList;
            }

            @Override
            protected void onPostExecute(List<Task> tasks) {
                super.onPostExecute(tasks);
                TaskAdapter adapter = new TaskAdapter(MainActivity.this, tasks);
                recyclerView.setAdapter(adapter);
            }
        }

        GetTasks gt = new GetTasks();
        gt.execute();
    }

    private void getWeather() {
//        final Double latitude = getLocation()[0];
//        final Double longitude = getLocation()[1];

        Double latitude = 48.9215;
        Double longitude = 24.7097;
        String units = "metric";
        String key = WeatherAPI.KEY;

        Log.d(TAG, "OK");

        // get weather for today
        Call<WeatherService> callToday = api.getToday(longitude, latitude, units, key);
        callToday.enqueue(new Callback<WeatherService>() {
            @Override
            public void onResponse(Call<WeatherService> call, Response<WeatherService> response) {
                Log.e(TAG, "onResponse");
                WeatherService data = response.body();
                //Log.d(TAG,response.toString());
                //Log.d(TAG, String.valueOf(latitude));

                if (response.isSuccessful()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
                    wcCity.setText(data.getCity());
                    wcDesc.setText(data.getDescription());
                    wcTemp.setText(data.getTempWithDegree());
                    wcTemFL.setText("Feels like " + data.getTempFeelsLike() + "\u00b0");
                    wcWind.setText("Wind speed: " + data.getWindSpeed());
                    currentDate.setText((String.valueOf(sdf.format(data.getDate().getTime()))));
                    Picasso.with(MainActivity.this).load(data.getIconUrl()).into(wcIcon);
                }
            }

            @Override
            public void onFailure(Call<WeatherService> call, Throwable t) {
                Log.e(TAG, "onFailure");
                Log.e(TAG, t.toString());
            }
        });

    }

    public double[] getLocation(){
        double latitude = 0, longitude = 0;
        gpsTracker = new GpsTracker(MainActivity.this);
        if(gpsTracker.canGetLocation()){
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
        }else{
            gpsTracker.showSettingsAlert();
        }
        return new double[]{latitude, longitude};
    }

}