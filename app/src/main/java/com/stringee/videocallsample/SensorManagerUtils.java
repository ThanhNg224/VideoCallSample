package com.stringee.videocallsample;

import android.annotation.SuppressLint;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.PowerManager;

public class SensorManagerUtils implements SensorEventListener {
    @SuppressLint("StaticFieldLeak")
    private static SensorManagerUtils instance;
    private SensorManager mSensorManager;
    private PowerManager.WakeLock wakeLock;

    private final Context context;

    public static SensorManagerUtils getInstance(Context context) {
        if (instance == null) {
            instance = new SensorManagerUtils(context.getApplicationContext());
        }
        return instance;
    }


    public SensorManagerUtils(Context context) {
        this.context = context;
    }

    public void acquireProximitySensor(String tag) {
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        mSensorManager.registerListener(this, mProximity, SensorManager.SENSOR_DELAY_NORMAL);

        PowerManager powerManager = ((PowerManager) context.getSystemService(Context.POWER_SERVICE));

        int screenLockValue;

        screenLockValue = PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK;
        wakeLock = powerManager.newWakeLock(screenLockValue, tag);

        wakeLock.acquire(10*60*1000L /*10 minutes*/);
    }



    public void releaseSensor() {
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            mSensorManager = null;
        }

        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
            wakeLock = null;
        }



        if (instance != null) {
            instance = null;
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        float value = sensorEvent.values[0];
        if (value == 0) {
            if (wakeLock != null && !wakeLock.isHeld()) {
                wakeLock.acquire(10*60*1000L /*10 minutes*/);
            }
        } else {
            if (wakeLock != null && wakeLock.isHeld()) {
                wakeLock.release();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
