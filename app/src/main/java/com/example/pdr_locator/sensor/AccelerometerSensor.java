package com.example.pdr_locator.sensor;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/10
 * @Time: 16:47
 */

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class AccelerometerSensor implements ISensor{
    private SensorManager sensorManager;
    protected Sensor accelerometer;
    private double[] latestData = new double[3]; // 存储最新数据

    public AccelerometerSensor(SensorManager manager) {
        this.sensorManager = manager;
        this.accelerometer = manager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    @Override
    public int getSensorType() {
        return Sensor.TYPE_ACCELEROMETER;
    }

    @Override
    public String getSensorName() {
        return "Accelerometer";
    }

    @Override
    public double[] getLatestData() {
        return latestData;
    }
}
