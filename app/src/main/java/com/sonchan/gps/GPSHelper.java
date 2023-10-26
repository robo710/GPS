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
        checkAndPermissionRequest(); // Activity를 인자로받고 그 Activity의 gps권한을 확인하고 요청하는 코드
    }

    public void prepareGPS(GPSHelperLister listener){
        this.listener = listener;
        locationRequest = com.google.android.gms.location.LocationRequest.create(); // 구글의 gps서비스를 이용하기위해 locationRequest 객체를 생성하고 할당한다
        locationRequest.setInterval(1000); // gps 업데이트 가장 느린간격을 설정해줌 단위는 ms
        locationRequest.setFastestInterval(500); // gps 업데이트 가장 빠른간녁을 설정해줌 단위는 ms
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY); // 정확한 위치 요청
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult  locationResult) { // LocationCallback을 초기화하고 위치 업데이트가 생기면 listener에위치값을 전달해준다                  
                super.onLocationResult(locationResult);
                listener.onLocationResult(locationResult);
            }
        };
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(activity); // 안드로이드 앱에서 google play 서비스의 위치 제공자를 사용하도록 설정하는 부분이다                                  
        listener.onReady(); // onReady 함수를 사용해서 리스너가 준비됬다는걸 알려준다
    }

    public void start(){
        if (checkGrantedPermission(Manifest.permission.ACCESS_FINE_LOCATION) && checkDeniedPermission(Manifest.permission.ACCESS_COARSE_LOCATION)){ // 위치권한이 있는지 확인한다                                          
            return;
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper()); 
        // prepareGPS에서 설정한 옵션을 받아서 fusedLocationProviderClient를 이용해서 requestLocationUpdates메서드를호출하여 위치를 업데이트한다                                                 
    }

    public void stop(){
        fusedLocationProviderClient.removeLocationUpdates(locationCallback); // 앱이 더이상 위치정보를 필요로 하지않을때 위치업데이트를 멈추는메서드                                                           
    }

    private boolean checkGrantedPermission(String permisson){ // ActivityCompat.checkSelfPermission 메서드를 사용하여 권한을 확인한다
        return ActivityCompat.checkSelfPermission(activity, permisson) == PackageManager.PERMISSION_GRANTED;
    }

    private boolean checkDeniedPermission(String permisson){ // ActivityCompat.checkSelfPermission 메서드를 사용하여 권한이 거부당했는지 확인한다
        return ActivityCompat.checkSelfPermission(activity, permisson) == PackageManager.PERMISSION_DENIED;
    }

    private void checkAndPermissionRequest() { // 위치 접근권한을 확인하고 권한이 없으면 사용자에게 권한을 요청하는 메서드
        int permissonCheck = ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION);

        if (permissonCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }
    }
}
