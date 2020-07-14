package com.example.taskmanagerapp;

import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.taskmanagerapp.db.DatabaseClient;
import com.example.taskmanagerapp.db.Task;
import com.example.taskmanagerapp.weatherForecast.GpsTracker;
import com.example.taskmanagerapp.weatherForecast.OpenWeatherInterface;
import com.example.taskmanagerapp.weatherForecast.POJO.WheatherData;
import com.example.taskmanagerapp.weatherForecast.RetrofitAdapter;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    private boolean isFragmentDisplayed = false;
    private static final String TAG = "WEATHER";
    private RecyclerView recyclerView;
    private ImageView wcIcon;
    private CardView cardView;
    private TextView wcTemp, wcTemFL, wcCity, wcDesc, wcWind, currentDate;
    private GpsTracker gpsTracker;
    Retrofit retrofit;

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

        retrofit = new RetrofitAdapter().getClient();

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
        final String latitude = Double.toString(getLocation()[0]);
        final String longitude = Double.toString(getLocation()[1]);

        String units = "metric";
        String key = RetrofitAdapter.KEY;

        Log.d(TAG, "OK");

        OpenWeatherInterface openWeatherInterface = retrofit.create(OpenWeatherInterface.class);

        Observable<WheatherData> weatherObservable = openWeatherInterface.getWeatherData(latitude, longitude, units, key);

        weatherObservable.subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(this::handleResults, this::handleErorrs);


    }


    private void handleResults(WheatherData wheatherData) {
        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm a");
        NumberFormat numberFormat = DecimalFormat.getInstance();
        numberFormat.setMaximumFractionDigits(0);

        String url = "http://openweathermap.org/img/wn/" + wheatherData.getWeather().get(0).getIcon() + "@4x.png";

        wcCity.setText(wheatherData.getName());
        wcDesc.setText(wheatherData.getWeather().get(0).getDescription().toUpperCase());
        wcTemp.setText(numberFormat.format(wheatherData.getMain().getTemp()) + "\u00b0");
        wcTemFL.setText("Feels like " + numberFormat.format(wheatherData.getMain().getFeels_like()) + "\u00b0");
        wcWind.setText("Wind speed: " + wheatherData.getWind().getSpeed().toString() + " mps");
        currentDate.setText(String.valueOf(sdf.format(wheatherData.getDate().getTime())));
        Picasso.with(MainActivity.this).load(url).into(wcIcon);

        Log.d(TAG, url);
        Log.d(TAG,  wheatherData.getWeather().get(0).getIcon());

    }

    private void handleErorrs(Throwable throwable) {
        Toast.makeText(this, "ERROR IN FETCHING API RESPONSE. Try again",
                Toast.LENGTH_LONG).show();
        Log.e(TAG, throwable.toString());
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