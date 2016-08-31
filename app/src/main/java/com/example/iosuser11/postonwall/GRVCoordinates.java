package com.example.iosuser11.postonwall;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


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

    public float[] getRotationMatrix() {
        float[] mat = new float[16];
        mSensorManager.getRotationMatrixFromVector(mat, values);
        return mat;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
//        if (sensorEvent.sensor.getType() != Sensor.TYPE_GAME_ROTATION_VECTOR) {
//            return;
//        } else {
//            values = sensorEvent.values.clone();
//        }
        if (sensorEvent.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            values = sensorEvent.values.clone();
        } else if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

        } else {
            return;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}