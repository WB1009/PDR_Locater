package com.example.pdr_locator.sensor;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/10
 * @Time: 17:40
 */
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class MagnetometerSensor implements ISensor, SensorEventListener {
    private SensorManager sensorManager;
    protected Sensor magnetometer;
    private double[] latestData = new double[3]; // 存储最新数据

    public MagnetometerSensor(SensorManager manager) {
        this.sensorManager = manager;
        this.magnetometer = manager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
    }

    @Override
    public int getSensorType() {
        return Sensor.TYPE_MAGNETIC_FIELD;
    }

    @Override
    public String getSensorName() {
        return "Magnetometer";
    }

    @Override
    public double[] getLatestData() {
        return latestData;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            // 更新最新数据
            latestData[0] = event.values[0];
            latestData[1] = event.values[1];
            latestData[2] = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 处理传感器精度变化
    }
}
