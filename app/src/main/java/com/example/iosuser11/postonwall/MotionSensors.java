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
    float[] speedXYZ;

    public MotionSensors(Activity activity) {

        mSensorManager = (SensorManager) activity.getSystemService(activity.SENSOR_SERVICE);

        gameRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);
        rotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        geoRotationVector = mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        accelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        mSensorManager.registerListener(this, gameRotationVector, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, rotationVector, SensorManager.SENSOR_DELAY_GAME);
        mSensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME);

        speedXYZ = new float[3];
    }

    public float[] getRotationMatrix() {
        return rotationMatrix;
    }

    public float[] getTranslationXYZ() {
        return speedXYZ;
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
            double yaw = Math.atan2(rotationMatrix[4], rotationMatrix[0]);
            double pitch = Math.atan2(-rotationMatrix[8], Math.sqrt(rotationMatrix[9]*rotationMatrix[9] + rotationMatrix[10]*rotationMatrix[10]));
            double roll = Math.atan2(rotationMatrix[9], rotationMatrix[10]);

            speedXYZ[0] += (float) (sensorEvent.values[0] - 9.8f * Math.sin(roll));
            speedXYZ[1] += (float) (sensorEvent.values[1] - 9.8f * Math.sin(pitch));
            speedXYZ[2] += (float) (sensorEvent.values[2] - 9.8f * Math.cos(roll) * Math.cos(pitch));
//            anglex = Math.atan2((double)result[7], (double)result[8]);
//            angley = Math.atan2((double) - result[6], Math.sqrt(result[7] * result[7] + result[8] * result[8]));
//            anglez = Math.atan2((double)result[3], (double)result[0]);

        } else {
            return;
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {}
}