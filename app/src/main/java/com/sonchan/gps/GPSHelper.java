package com.sonchan.gps;

import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.location.Location;
import android.os.Looper;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

public class GPSHelper {
    private Activity activity; // 권한 요청과 확인
    private LocationRequest locationRequest; // 위치 확인 요청을 보낼 때
    private LocationCallback locationCallback; // 위치 확인되고 데이터를 받을 때
    private FusedLocationProviderClient fusedLocationProviderClient; // 위치 확인 실행
    private GPSHelperLister listener;

    interface GPSHelperLister{
        void onReady();

        void onLocationResult(LocationResult locationResult);
    }

    public GPSHelper(Activity activity){
        this.activity = activity;
        checkAndPermissionRequest();
    }

    public void prepareGPS(GPSHelperLister listener){
        this.listener = listener;
        locationRequest = com.google.android.gms.location.LocationRequest.create();
        locationRequest.setInterval(1000);
        locationRequest.setFastestInterval(500);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // 정확한 위치 요청
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult  locationResult) {
                super.onLocationResult(locationResult);
                listener.onLocationResult(locationResult);
            }
        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity);
        listener.onReady();
    }

    public void start(){
        if (checkGrantedPermission(Manifest.permission.ACCESS_FINE_LOCATION) &&
        checkDeniedPermission(Manifest.permission.ACCESS_COARSE_LOCATION)){
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public void stop(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    private boolean checkGrantedPermission(String permisson){
        return ActivityCompat.checkSelfPermission(activity, permisson) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkDeniedPermission(String permisson){
        return ActivityCompat.checkSelfPermission(activity, permisson) == PackageManager.PERMISSION_DENIED;
    }

    private void checkAndPermissionRequest() {
        int permissonCheck = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissonCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }
}
