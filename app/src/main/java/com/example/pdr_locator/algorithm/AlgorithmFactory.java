package com.example.pdr_locator.algorithm;

import android.content.Context;
/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/14
 * @Time: 15:28
 */


import java.net.ContentHandler;

/**
 * 算法工厂类，根据算法名称返回定位算法对象
 */
public class AlgorithmFactory {
    public static IAlgorithm createAlgorithm(Context context,String algorithmName) {
        switch (algorithmName) {
            case "PdrLocalOri":
                return new PdrLocalOri(context, "gru.onnx");
            case "CollectData":
                return new CollectData();
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }
    }
}
