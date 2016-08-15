package com.example.iosuser11.postonwall;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Created by iosuser12 on 8/11/16.
 */
public class GRVCoordinates implements SensorEventListener {
    SensorManager mSensorManager;
    private Sensor mSensor;

    //[0] = x , [1] = y [2] = z
    private float[] values = new float[3];

    public GRVCoordinates(Activity activity) {
        // Get an instance of the SensorManager
        mSensorManager = (SensorManager) activity.getSystemService(activity.SENSOR_SERVICE);
        mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        mSensorManager.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_GAME);
    }

    public float[] getValues() {
        return values;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() != Sensor.TYPE_GAME_ROTATION_VECTOR) {
            return;
        }
        else {
            values = sensorEvent.values.clone();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}