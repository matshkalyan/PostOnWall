package com.example.iosuser11.postonwall;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class MotionSensors implements SensorEventListener {
    SensorManager mSensorManager;
    private Sensor gameRotationVector;
    private Sensor accelerometer;
    float[] rotationMatrix;
    float[] translationXYZ;

    public MotionSensors(Activity activity) {

        mSensorManager = (SensorManager) activity.getSystemService(activity.SENSOR_SERVICE);

        gameRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        mSensorManager.registerListener(this, gameRotationVector, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);
    }

    public float[] getRotationMatrix() {
        return rotationMatrix;
    }

    public float[] getTranslationXYZ() {
        return translationXYZ;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if (sensorEvent.sensor.getType() == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            float[] values = sensorEvent.values.clone();
            rotationMatrix = new float[16];
            mSensorManager.getRotationMatrixFromVector(rotationMatrix, values);
        } else if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

        } else {
            return;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}