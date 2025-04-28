package com.example.pdr_locator.algorithm;

import com.example.pdr_locator.model.SensorData;

import java.util.List;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/23
 * @Time: 22:05
 */

/**
 * 采集数据的算法类，返回原点位置，即不返回定位结果，只采集数据
 */
public class CollectData implements IAlgorithm{
    @Override
    public double[] getCoordinate(List<SensorData> input) throws Exception{
        return new double[]{0.0, 0.0, 0.0};
    }
    @Override
    public void reset(){
    }
}
