package com.example.pdr_locator.sensor;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/10
 * @Time: 17:29
 */
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.util.Log;

import com.example.pdr_locator.model.SensorType;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Setter;

/**
 * 传感器参数配置
 */
@Setter
public class SensorConfig {
    private Context context;  // 上下文内容
    private List<SensorType> sensorTypes = new ArrayList<>();  // 需要启用的传感器类型
    private Map<SensorType, Integer> sensorClassMap = new HashMap<>();  // SensorType和手机的Sensor类型映射
    private SensorManager sensorManager;  // 手机的传感器管理器，接收得到

    /**
     * 构造函数
     *
     * @param context 上下文内容
     * @param sensorManager 传感器管理器
     */
    public SensorConfig(Context context, SensorManager sensorManager) {
        this.context = context;
        this.sensorManager = sensorManager;
        // 初始化传感器类型到类的映射
        sensorClassMap.put(SensorType.ACCELEROMETER, Sensor.TYPE_ACCELEROMETER);
        sensorClassMap.put(SensorType.GYROSCOPE, Sensor.TYPE_GYROSCOPE);
        sensorClassMap.put(SensorType.MAGNETOMETER, Sensor.TYPE_MAGNETIC_FIELD);
    }

    /**
     * 根据需要启用的传感器类型创建对应的传感器对象
     *
     * @return 返回传感器列表
     */
    public List<ISensor> createSensors() {
        List<ISensor> sensors = new ArrayList<>();
        for (SensorType type : sensorTypes) {
            try {
                if (sensorClassMap.get(type) != null){
                    int sensorType = sensorClassMap.get(type);
                    switch (sensorType) {
                        case Sensor.TYPE_ACCELEROMETER:
                            ISensor accelerometer = new AccelerometerSensor(sensorManager);
                            sensors.add(accelerometer);
                        case Sensor.TYPE_GYROSCOPE:
                            ISensor gyroscope = new GyroscopeSensor(sensorManager);
                            sensors.add(gyroscope);
                        case Sensor.TYPE_MAGNETIC_FIELD:
                            ISensor magnetic = new MagnetometerSensor(sensorManager);
                            sensors.add(magnetic);
                    }
                }
            } catch (NullPointerException e) {
                Log.e("SensorConfig", "Failed to create sensor: " + type, e);
            }
        }
        return sensors;
    }
}
