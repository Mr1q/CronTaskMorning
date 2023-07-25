package com.example.user.flashopen;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class ClockReceive  extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        //循环启动Service
        Intent i = new Intent(context, Clock.class);

        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

//        context.startService(i);
        context.startActivity(i);
        Toast.makeText(context,"接收到定时信号",Toast.LENGTH_LONG).show();
        Log.d("TAG_",context.toString());


    }
}
