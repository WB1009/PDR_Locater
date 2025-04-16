package com.example.pdr_locator.sensor;

import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.example.pdr_locator.R;
import com.example.pdr_locator.model.SensorData;
import com.example.pdr_locator.model.SensorType;
import com.example.pdr_locator.utils.SlidingWindowManager;

import java.util.List;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/10
 * @Time: 17:02
 */

/**
 * 传感器数据采集器类，用于注册、监听传感器，并记录存储传感器数据
 */
public class SensorDataCollector implements SensorEventListener {
    private final List<SensorType> sensorTypes; // 需要启用的传感器的类型
    private final List<ISensor> sensorList; // 启用的传感器列表
    private final SensorManager sensorManager; // 手机自带的传感器管理器
    public final SlidingWindowManager slidingWindowManager; // 滑动窗口管理器工具，用于生成消息队列任务
    private float[] latestAccelerometerData; // 最新的加速度计任务
    private float[] latestGyroscopeData; // 最新的陀螺仪数据
    private float[] latestMagnetometerData; // 最新的磁力计数据

    /**
     * 构造函数
     *
     * @param context 应用上下文信息
     * @param sensorTypes 需要启用的传感器类型
     */
    public SensorDataCollector(Context context, List<SensorType> sensorTypes){
        this.sensorTypes = sensorTypes;
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        SensorConfig config = new SensorConfig(context, sensorManager);
        config.setSensorTypes(sensorTypes);
        this.sensorList = config.createSensors();

        // 读取上下文中的全局配置参数windowSize和slideStep
        SharedPreferences sharedPref = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE);
        String windowSizeKey = context.getResources().getString(R.string.window_size_key);
        int windowSize = sharedPref.getInt(windowSizeKey, 50);
        String slideStepKey = context.getResources().getString(R.string.slide_step_key);
        int slideStep = sharedPref.getInt(slideStepKey, 10);

        slidingWindowManager = new SlidingWindowManager(windowSize, slideStep);
    }

    /**
     * 开始数据采集，注册需要的传感器
     */
    public void startCollecting() {
        for(ISensor s: sensorList){
            int type = s.getSensorType();
            switch (type){
                case Sensor.TYPE_ACCELEROMETER:
                    if(s instanceof AccelerometerSensor) {
                        AccelerometerSensor as = (AccelerometerSensor) s;
                        sensorManager.registerListener(this, as.accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    }
                case Sensor.TYPE_GYROSCOPE:
                    if(s instanceof GyroscopeSensor){
                        GyroscopeSensor gs = (GyroscopeSensor)s;
                        sensorManager.registerListener(this, gs.gyroscope, SensorManager.SENSOR_DELAY_NORMAL);
                    }
                case Sensor.TYPE_MAGNETIC_FIELD:
                    if(s instanceof MagnetometerSensor){
                        MagnetometerSensor ms = (MagnetometerSensor)s;
                        sensorManager.registerListener(this, ms.magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
                    }
            }
        }
    }

    /**
     * 停止采集，注销所有传感器
     */
    public void stopCollecting(){
        sensorManager.unregisterListener(this);
    }

    /**
     * 传感器数据变化的回调函数
     *
     * @param event the {@link android.hardware.SensorEvent SensorEvent}.
     */
    @Override
    public void onSensorChanged(SensorEvent event) {
        long timestamp = System.currentTimeMillis();

        switch (event.sensor.getType()) {
            case Sensor.TYPE_ACCELEROMETER:
                latestAccelerometerData = event.values.clone();
                break;
            case Sensor.TYPE_GYROSCOPE:
                latestGyroscopeData = event.values.clone();
                break;
            case Sensor.TYPE_MAGNETIC_FIELD:
                latestMagnetometerData = event.values.clone();
                break;
        }

        if(!sensorTypes.contains(SensorType.MAGNETOMETER)){
            latestAccelerometerData = new float[]{0.0f, 0.0f, 0.0f};
        }
        // 检查是否所有传感器都有最新数据
        if (latestAccelerometerData != null &&
            latestGyroscopeData != null &&
            latestMagnetometerData != null) {
            // 整合数据
            SensorData sensorData = new SensorData(
                    timestamp,
                    latestAccelerometerData,
                    latestGyroscopeData,
                    latestMagnetometerData
            );

            // 将数据转换为一个包含10个数字的数组
            float[] flatData = sensorData.toFlatArray();

            // 添加到滑动窗口管理器
            slidingWindowManager.addData(flatData);

            // 重置缓存
            latestAccelerometerData = null;
            latestGyroscopeData = null;
            latestMagnetometerData = null;
        }
    }

    /**
     * 传感器精度变化时的回调函数
     *
     * @param sensor 传感器
     * @param accuracy The new accuracy of this sensor, one of
     *         {@code SensorManager.SENSOR_STATUS_*}
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
