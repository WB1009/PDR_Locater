package com.example.pdr_locator.sensor;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/10
 * @Time: 16:36
 */

import android.hardware.Sensor;
import android.hardware.SensorEventListener;

/**
 * 手机传感器接口，定义传感器的通用方法
 */
public interface ISensor {

    /**
     * 获取传感器类型
     *
     * @return 返回传感器的类型（Sensor的Type是用一个int值表示）
     */
    int getSensorType();

    /**
     * 获取传感器名字
     *
     * @return 返回传感器名字
     */
    String getSensorName();

    /**
     * 获取传感器采集的最细数据
     *
     * @return double[] 返回一条最新的该传感器数据
     */
    double[] getLatestData();
}
