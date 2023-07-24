package com.example.user.flashopen;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;

@SuppressWarnings("all")
public class Lock {
    private PowerManager.WakeLock wakeLock = null;
    private boolean isLock = false;

    /**
     * 给CPU加锁，使CPU不得休眠
     * @param activity activity
     * @param timeMilliseconds 锁的失效时间，毫秒值（强制指定失效时间，不然会造成手机耗电增加）
     * @return 返回true说明加锁成功
     */
    public synchronized boolean lock(Activity activity, long timeMilliseconds) {
        if (activity == null)
            return false;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed()) {
                return false;
            }
        }
        PowerManager pm = (PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ON_AFTER_RELEASE, "CpuWakeLock");
            wakeLock.acquire(timeMilliseconds);
            isLock = true;
            return true;
        }
        return false;
    }

    /**
     * 给CPU解锁，使CPU可以休眠
     */
    public synchronized void unlock() {
        if (wakeLock != null) {
            wakeLock.release();
            wakeLock = null;
            isLock = false;
        }
    }

    public boolean isLock() {
        return isLock;
    }


}

