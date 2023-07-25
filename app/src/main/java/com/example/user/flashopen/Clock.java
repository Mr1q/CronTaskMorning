package com.example.user.flashopen;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class Clock extends Activity {

    private CameraManager manager;// 声明CameraManager对象
    private Camera m_Camera = null;// 声明Camera对象
    private Button close_light;

    @Override
    @SuppressWarnings("all")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] camerList = manager.getCameraIdList();
            for (String str : camerList) {
            }
        } catch (CameraAccessException e) {
            Log.e("error", e.getMessage());
        }
        close_light = findViewById(R.id.close_light);
        close_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(runnable);
                Log.e("TAG_ close","close 闪光灯 移除runnable");

            }
        });
        //启动闪光灯
        mHandler.postDelayed(runnable,100);
    }


    private void close(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                manager.setTorchMode("0", false);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (m_Camera != null) {
                m_Camera.stopPreview();
                m_Camera.release();
                m_Camera = null;
            }
        }
    }
    private void open(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                manager.setTorchMode("0", true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            final PackageManager pm = getPackageManager();
            final FeatureInfo[] features = pm.getSystemAvailableFeatures();
            for (final FeatureInfo f : features) {
                if (PackageManager.FEATURE_CAMERA_FLASH.equals(f.name)) { // 判断设备是否支持闪光灯
                    if (null == m_Camera) {
                        m_Camera = Camera.open();
                    }
                    final Camera.Parameters parameters = m_Camera.getParameters();
                    parameters.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                    m_Camera.setParameters(parameters);
                    m_Camera.startPreview();
                }
            }
        }
    }


    final Handler mHandler  = new Handler(Looper.getMainLooper());

    final Random random = new Random();
    Runnable runnable  = new Runnable() {
        @Override
        public void run() {
            open();
            int t = random.nextInt(180) + 50;
            Log.d("TAG_LOG", t + " -");
            try {
                Thread.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            close();
            t = random.nextInt(50) + 50;
            Log.d("TAG_LOG", t + " --");
            try {
                Thread.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("TAG_ = " + Thread.currentThread().getId());
            Log.d("TAG_", "run: 我还在运行");
            mHandler.postDelayed(this, 100);
        }
    };



}
