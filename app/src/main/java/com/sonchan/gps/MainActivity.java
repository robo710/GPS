package com.sonchan.gps;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.Manifest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.LocationResult;

public class MainActivity  extends AppCompatActivity {

    TextView speed_tv; // 현재속도를 보여주는 TextView
    ProgressBar speedProgress_pb, longitude_pb, latitude_pb; // 현재속도 speedProgress_pb, 현재위도 longitude_pb, 현재경도latitude_pb
    GPSHelper gpsHelper; 
    GPSHelper.GPSHelperLister gpsHelperLister;

    protected void OnCreate(Bundle savedInstanceState){ // Activity가 생성됐을때 activity_main.xml파일에서 위의 TextView, PrograssBar같은 ui 요소를 변수에 연결해준다
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        speed_tv = findViewById(R.id.speed_tv);
        speedProgress_pb = findViewById(R.id.speedProgress_pb);
        longitude_pb = findViewById(R.id.longitude_pb);
        latitude_pb = findViewById(R.id.latitude_pb);
    }

    @Override
    protected void onResume() { // Activity가 정지후 다시 시작할때 GPSHelper클래스를 초기화하고 GPSHelperListner를 구현한다
        super.onResume();
        gpsHelper = new GPSHelper(this);
        gpsHelperLister = new GPSHelper.GPSHelperLister() {
            @Override
            public void onReady() {
                gpsHelper.start();
            }

            @Override
            public void onLocationResult(LocationResult locationResult) { // 기기의 위치정보들을 activity_main.xml의 ui요소들에 표시한다
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

    private float floatMpsToFloatKph(float mps){ // GooglePlay가 재공한 M/S를 M/h로 바꿔서 return해준다 
        return Float.valueOf(String.format("%.1f", mps*3600/1000));
    }

    private int floatMpsToIntKph(float mps){ // GooglePlay가 재공한 M/S를 M/h 그리고 정수로 바꿔서 return해준다 
        return Integer.parseInt(String.valueOf(Math.round(mps*3600/1000)));
    }

    private int doubleToIntegerX100(double d){ // double 형식의 데이터에 100을 곱하고 정수로 바꿔서 return해준다
        return Integer.parseInt(String.valueOf(Math.round(d*100)));
    }

    private void prepareGPSHelper(GPSHelper.GPSHelperLister lister){ // 
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
