package com.example.pdr_locator.sensor;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/10
 * @Time: 17:36
 */

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class GyroscopeSensor implements ISensor{
    private SensorManager sensorManager;
    protected Sensor gyroscope;
    private double[] latestData = new double[3]; // 存储最新数据

    public GyroscopeSensor(SensorManager manager) {
        this.sensorManager = manager;
        this.gyroscope = manager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
    }

    @Override
    public int getSensorType() {
        return Sensor.TYPE_GYROSCOPE;
    }

    @Override
    public String getSensorName() {
        return "Gyroscope";
    }

    @Override
    public double[] getLatestData() {
        return latestData;
    }
}