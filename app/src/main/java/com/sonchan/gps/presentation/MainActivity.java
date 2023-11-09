package com.sonchan.gps.presentation;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationResult;
import com.sonchan.gps.data.GPSHelper;
import com.sonchan.gps.R;

public class MainActivity  extends AppCompatActivity {

    TextView speed_tv;
    ProgressBar speedProgress_pb, longitude_pb, latitude_pb;
    GPSHelper gpsHelper;
    GPSHelper.GPSHelperLister gpsHelperLister;
    Button weather_btn;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speed_tv = findViewById(R.id.speed_tv);
        speedProgress_pb = findViewById(R.id.speedProgress_pb);
        longitude_pb = findViewById(R.id.longitude_pb);
        latitude_pb = findViewById(R.id.latitude_pb);
        weather_btn = (Button) findViewById(R.id.weather_btn);

        weather_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        gpsHelper = new GPSHelper(this);
        gpsHelperLister = new GPSHelper.GPSHelperLister() {
            @Override
            public void onReady() {
                gpsHelper.start();
            }

            @Override
            public void onLocationResult(LocationResult locationResult) {
                if(locationResult != null){
                    for(Location location : locationResult.getLocations()){
                        try {
                            speed_tv.setText(String.valueOf(floatMpsToFloatKph(location.getSpeed())));
                            speedProgress_pb.setProgress(floatMpsToIntKph(location.getSpeed()));
                            longitude_pb.setProgress(doubleToIntegerX100(location.getLongitude()));
                            latitude_pb.setProgress(doubleToIntegerX100(location.getLatitude()));
                        } catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                }
            }
        };

        prepareGPSHelper(gpsHelperLister);
    }

    private float floatMpsToFloatKph(float mps){
        return Float.valueOf(String.format("%.1f", mps*3600/1000));
    }

    private int floatMpsToIntKph(float mps){
        return Integer.parseInt(String.valueOf(Math.round(mps*3600/1000)));
    }

    private int doubleToIntegerX100(double d){
        return Integer.parseInt(String.valueOf(Math.round(d*100)));
    }

    private void prepareGPSHelper(GPSHelper.GPSHelperLister lister){
        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            if(lister != null){
                gpsHelper.prepareGPS(lister);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            gpsHelper.stop();
            gpsHelper = null;
            gpsHelperLister = null;
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1){
            if(grantResults.length > 0){
                for(int grantResult : grantResults){
                    if(grantResult == PackageManager.PERMISSION_GRANTED){
                        prepareGPSHelper(gpsHelperLister);
                    }
                }
            }
        }
    }
}