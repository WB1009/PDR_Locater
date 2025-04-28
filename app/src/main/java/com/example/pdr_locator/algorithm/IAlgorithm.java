package com.example.pdr_locator.algorithm;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/13
 * @Time: 17:00
 */

import com.example.pdr_locator.model.SensorData;

import java.util.List;

/**
 * 定位算法接口
 */
public interface IAlgorithm {
    /**
     * 获得一次定位结果
     * @param input 输入IMU数据
     * @return 返回一次定位结果（x,y,z）
     */
    double[] getCoordinate(List<SensorData> input) throws Exception;
}
