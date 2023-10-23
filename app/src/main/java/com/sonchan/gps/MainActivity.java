package com.sonchan.gps;

import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.location.LocationResult;

public class MainActivity  extends AppCompatActivity {

    TextView speed_tv;
    ProgressBar speedProgress_pb, longitude_pb, latitude_pb;
    GPSHelper gpsHelper;
    GPSHelper.GPSHelperLister gpsHelperLister;

    protected void OnCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speed_tv = findViewById(R.id.speed_tv);
        speedProgress_pb = findViewById(R.id.speedProgress_pb);
        longitude_pb = findViewById(R.id.longitude_pb);
        latitude_pb = findViewById(R.id.latitude_pb);
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
    }

    private float floatMpsToFloatKph(float mps){
        return Float.valueOf(String.format("%.1f", mps*3600/1000));
    }

    private int floatMpsToIntKph(float mps){
        return Integer.
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if(requestCode == 1){
            if(grantResults.length > 0){
                for(int grantResult : grantResults){
                    if(grantResults == PackageManager.PERMISSION_GRANTED){

                    }
                }
            }
        }
    }
}
