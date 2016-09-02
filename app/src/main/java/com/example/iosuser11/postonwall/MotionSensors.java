package com.example.iosuser11.postonwall;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;


public class MotionSensors implements SensorEventListener {
    SensorManager mSensorManager;
    private Sensor gameRotationVector;
    private Sensor rotationVector;
    private Sensor accelerometer;
    private Sensor geoRotationVector;
    float[] rotationMatrix;
    float[] translationXYZ;

    public MotionSensors(Activity activity) {

        mSensorManager = (SensorManager) activity.getSystemService(activity.SENSOR_SERVICE);

        gameRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        rotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        geoRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        mSensorManager.registerListener(this, gameRotationVector, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_GAME);
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
        } else if(sensorEvent.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
//            float[] values = sensorEvent.values.clone();
//            rotationMatrix = new float[16];
//            mSensorManager.getRotationMatrixFromVector(rotationMatrix, values);
        } else if(sensorEvent.sensor.getType() == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
//            float[] values = sensorEvent.values.clone();
//            rotationMatrix = new float[16];
//            mSensorManager.getRotationMatrixFromVector(rotationMatrix, values);
        } else if(sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER && rotationMatrix != null) {
            translationXYZ = new float[3];
//            float[] gravity = new float[3];
//            float alpha = 0.8f;
//            gravity[0] = alpha * gravity[0] + (1 - alpha) * sensorEvent.values[0];
//            gravity[1] = alpha * gravity[1] + (1 - alpha) * sensorEvent.values[1];
//            gravity[2] = alpha * gravity[2] + (1 - alpha) * sensorEvent.values[2];
            double yaw = Math.atan2(rotationMatrix[4], rotationMatrix[0]);
            double pitch = Math.atan2(-rotationMatrix[8], Math.sqrt(rotationMatrix[9]*rotationMatrix[9] + rotationMatrix[10]*rotationMatrix[10]));
            double roll = Math.atan2(rotationMatrix[9], rotationMatrix[10]);

//            Log.d(TAG, "translation with gravity is: " + );

            translationXYZ[0] = (float) (sensorEvent.values[0] - 9.8f * Math.sin(roll));
            translationXYZ[1] = (float) (sensorEvent.values[1] - 9.8f * Math.sin(pitch));
            translationXYZ[2] = (float) (sensorEvent.values[2] - 9.8f * Math.cos(roll) * Math.cos(pitch));

            Log.d("", "translation is: " + translationXYZ[0] + " " + translationXYZ[1] + " " + translationXYZ[2]);
        } else {
            return;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}