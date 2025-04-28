package com.example.pdr_locator.model;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/13
 * @Time: 15:54
 */

import lombok.Getter;
import lombok.Setter;

/**
 * 传感器数据存储结构
 */
@Getter
@Setter
public class SensorData {
    private long timestamp = 0;  // 时间戳
    private float[] accelerometerData = new float[]{0.0f, 0.0f, 0.0f}; // 加速度计3轴数据
    private float[] gyroscopeData = new float[]{0.0f, 0.0f, 0.0f};;     // 陀螺仪3轴数据
    private float[] magnetometerData = new float[]{0.0f, 0.0f, 0.0f};;  // 磁力计3轴数据

    /**
     * 构造函数
     * @param timestamp  // 时间戳
     * @param accelerometerData  // 加速度计数据xyz
     * @param gyroscopeData  // 陀螺仪数据xyz
     * @param magnetometerData  // 磁力计数据xyz
     */
    public SensorData(long timestamp, float[] accelerometerData, float[] gyroscopeData, float[] magnetometerData) {
        this.timestamp = timestamp;
        this.accelerometerData = accelerometerData;
        this.gyroscopeData = gyroscopeData;
        this.magnetometerData = magnetometerData;
    }

    /**
     * 将数据转换为一个包含10个数字的数组
     *
     * @return float[10] 包含十个数据的数组
     */
    public float[] toFlatArray() {
        float[] result = new float[10];
        result[0] = timestamp; // 时间戳放在第一位
        System.arraycopy(accelerometerData, 0, result, 1, 3);
        System.arraycopy(gyroscopeData, 0, result, 4, 3);
        System.arraycopy(magnetometerData, 0, result, 7, 3);
        return result;
    }

    public String buildRowData() {
        StringBuilder rowData = new StringBuilder();
        rowData.append(timestamp);
        for (float value : accelerometerData) rowData.append(",").append(value);
        for (float value : gyroscopeData) rowData.append(",").append(value);
        for (float value : magnetometerData) rowData.append(",").append(value);
        rowData.append(System.lineSeparator());
        return rowData.toString();
    }
}
