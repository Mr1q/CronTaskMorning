package com.example.user.flashopen;

import android.app.AlarmManager;
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

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity {


    private CameraManager manager;// 声明CameraManager对象
    private Camera m_Camera = null;// 声明Camera对象
    private int countDownTimer = 0;
    private CheckBox cb_ctl;
    private TimePicker timePicker;
    private Switch switch1;
    private Switch switch2;
    private Button close_light;
    private TextView time_msg;
    private long leftTime = 3600;//一分钟
    Timer timer;
    TimerTask task;

    @Override
    @SuppressWarnings("all")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        requestIgnoreBatteryOptimizations(this);
        Lock lock   = new Lock();
        if(lock.lock(this,1000*60*60*10)){
            Log.e("error","加锁成功");
        }

//        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);


        cb_ctl = findViewById(R.id.cb_ctl);
        timePicker = findViewById(R.id.timePicker);
        switch1 = findViewById(R.id.switch1);
        switch2 = findViewById(R.id.switch2);
        close_light = findViewById(R.id.close_light);
        time_msg = findViewById(R.id.time_msg);

        timePicker.setIs24HourView(true);
        timePicker.setCurrentHour(0);
        timePicker.setCurrentMinute(0);


        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] camerList = manager.getCameraIdList();
            for (String str : camerList) {
            }
        } catch (CameraAccessException e) {
            Log.e("error", e.getMessage());
        }

        close_light.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mHandler.removeCallbacks(runnable);
                Log.e("TAG_ close","close 闪光灯 移除runnable");

            }
        });
        cb_ctl.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    timer = new Timer(false);
                    timePicker.setVisibility(View.INVISIBLE);
                    switch2.setVisibility(View.INVISIBLE);
                    lightSwitch(true);
                    countDownTimer = timePicker.getCurrentHour() * 60 + timePicker.getCurrentMinute();
                    Log.d("TAG_LOG", countDownTimer + " ");
                    if (countDownTimer > 0) {
                        task = new TimerTask() {
                            @Override
                            public void run() {
                                handler.sendEmptyMessage(1);
                            }
                        };
                        int unit = 1;
                        if (switch2.isChecked()) {
                            unit = 60;
                        }
                        Log.d("TAG_LOG", unit + " unit：");
//                        timer.scheduleAtFixedRate(task, 0, 1000 * unit);
                        leftTime = unit * countDownTimer;
                        timer.schedule(task,  1000 * unit * countDownTimer );
//                        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP ,System.currentTimeMillis(),5*1000, task);
                        handler.postDelayed(update_thread,1000);

                    }
                } else {
                    countDownTimer = 0;

                    timePicker.setCurrentHour(0);
                    timePicker.setCurrentMinute(0);
                    timePicker.setVisibility(View.VISIBLE);
                    switch2.setVisibility(View.VISIBLE);

                    lightSwitch(true);
                    if (task != null) {
                        task.cancel();
                    }
                    if (timer != null) {

                        timer.cancel();
                        timer.purge();
                    }

                    if (switch1.isChecked()) {
                        MainActivity.this.finish();
                    }
                }
            }
        });


    }
    @SuppressWarnings("all")
    Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
//                    cb_ctl.setChecked(false);
                    Log.d("TAG_LOG", msg.toString() + "-- handler另一个");
                    lightSwitch(false);
                    break;
                default:

            }

//            if (countDownTimer > 0) {
//                countDownTimer--;
//                Log.d("TAG_LOG", countDownTimer + "-- ");
//            } else {
//
//                lightSwitch(false);
//            }
        }
    };

    /**
     * 手电筒控制方法
     *
     * @param lightStatus
     * @return
     */
    private void lightSwitch(final boolean lightStatus) {
        if (lightStatus) { // 关闭手电筒
            close();
        } else { // 打开手电筒 间隔一秒启动闪光灯
            mHandler.postDelayed(runnable,100);
        }
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

    /**
     * 判断Android系统版本是否 >= M(API23)
     */
    private boolean isM() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return true;
        } else {
            return false;
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
    final Handler handlerStop = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    leftTime = 0;
                    handler.removeCallbacks(update_thread);
                    break;
            }
            super.handleMessage(msg);
        }

    };
    Runnable update_thread = new Runnable() {
        @Override
        public void run() {
            leftTime--;
            Log.d("TAG_ leftTime=",""+leftTime);
            if (leftTime > 0) {
                //倒计时效果展示
                String formatLongToTimeStr = formatLongToTimeStr(leftTime);
                time_msg.setText(formatLongToTimeStr);
                //每一秒执行一次
                handler.postDelayed(this, 1000);
            } else {//倒计时结束
                //处理业务流程

                //发送消息，结束倒计时
                Message message = new Message();
                message.what = 1;
                handlerStop.sendMessage(message);
            }
        }
    };
    public String formatLongToTimeStr(Long l) {
        int hour = 0;
        int minute = 0;
        int second = 0;
        second = l.intValue() ;
        if (second > 60) {
            minute = second / 60;   //取整
            second = second % 60;   //取余
        }
        if (minute > 60) {
            hour = minute / 60;
            minute = minute % 60;
        }
        String strtime = "剩余："+hour+"小时"+minute+"分"+second+"秒";
        return strtime;
    }

    /**
     * 判断我们的应用是否在白名单中
     *
     * @param context
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isIgnoringBatteryOptimizations(Context context) {
        boolean isIgnoring = false;
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            isIgnoring = powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }
        return isIgnoring;
    }

    /**
     * 申请加入白名单
     *
     * @param context
     */
    @RequiresApi(api = Build.VERSION_CODES.M)
    public static void requestIgnoreBatteryOptimizations(Context context) {
        try {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + context.getPackageName()));
            context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
