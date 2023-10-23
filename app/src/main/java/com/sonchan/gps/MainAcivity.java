package com.sonchan.gps

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity

import android.content.pm.PackageManager;
import android.os.Bundle
import com.sonchan.gps.databinding.ActivityMainBinding

public class MainAcivity  MainActivity extends AppCompatActivity{
        @Override
        protected void oncreate (Bundle savedInstanceStatr){
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_main);
        }
        @Override
        public void onRequesPermissionResult(int requestCode,@NonNull String[] permissions,@NonNull int[] grantResults)
                super.onRequestPermissionREsult(requestCode,permissions,grantResult);

        if(request == 1){
                if(grantResult : grantResults){
                        for(int grantResult : grantResult){
                                if (grantResult == PackageManager.PERMISSION_GRANTED){

                                }
                        }
                }
        }
}