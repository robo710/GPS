package com.sonchan.gps.presentation;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.Manifest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.LocationResult;
import com.sonchan.gps.data.GPSHelper;
import com.sonchan.gps.R;

public class MainActivity  extends AppCompatActivity implements SensorEventListener{

    TextView speed_tv;
    ProgressBar speedProgress_pb, longitude_pb, latitude_pb;
    GPSHelper gpsHelper;
    GPSHelper.GPSHelperLister gpsHelperLister;

    SensorManager sensorManager;
    Sensor stepCountSensor;
    TextView stepCountText;
    Button resetBtn;

    // 현재 걸음 수
    int currentSteps = 0;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        speed_tv = findViewById(R.id.speed_tv);
        speedProgress_pb = findViewById(R.id.speedProgress_pb);
        longitude_pb = findViewById(R.id.longitude_pb);
        latitude_pb = findViewById(R.id.latitude_pb);
        stepCountText = findViewById(R.id.stepCountText);
        resetBtn = findViewById(R.id.resetBtn);

        // 활동 퍼미션 체크
        if(ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACTIVITY_RECOGNITION) == PackageManager.PERMISSION_DENIED){

            requestPermissions(new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 0);
        }

        // 걸음 센서 연결
        // * 옵션
        // - TYPE_STEP_DETECTOR:  리턴 값이 무조건 1, 앱이 종료되면 다시 0부터 시작
        // - TYPE_STEP_COUNTER : 앱 종료와 관계없이 계속 기존의 값을 가지고 있다가 1씩 증가한 값을 리턴
        //
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        stepCountSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_DETECTOR);

        // 디바이스에 걸음 센서의 존재 여부 체크
        if (stepCountSensor == null) {
            Toast.makeText(this, "No Step Sensor", Toast.LENGTH_SHORT).show();
        }

        // 리셋 버튼 추가 - 리셋 기능
        resetBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 걸음수 초기화
                currentSteps = 0;
                stepCountText.setText(String.valueOf(currentSteps) + "걸음");
            }
        });
    }

    public void onStart() {
        super.onStart();
        if(stepCountSensor !=null) {
            // 센서 속도 설정
            // * 옵션
            // - SENSOR_DELAY_NORMAL: 20,000 초 딜레이
            // - SENSOR_DELAY_UI: 6,000 초 딜레이
            // - SENSOR_DELAY_GAME: 20,000 초 딜레이
            // - SENSOR_DELAY_FASTEST: 딜레이 없음
            //
            sensorManager.registerListener((SensorEventListener) this,stepCountSensor,SensorManager.SENSOR_DELAY_FASTEST);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // 걸음 센서 이벤트 발생시
        if(event.sensor.getType() == Sensor.TYPE_STEP_DETECTOR){

            if(event.values[0]==1.0f){
                // 센서 이벤트가 발생할때 마다 걸음수 증가
                currentSteps++;
                stepCountText.setText(String.valueOf(currentSteps) + "걸음");
            }

        }

    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

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