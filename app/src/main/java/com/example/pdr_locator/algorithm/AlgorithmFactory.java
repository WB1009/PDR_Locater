package com.example.pdr_locator.algorithm;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/14
 * @Time: 15:28
 */


/**
 * 算法工厂类，根据算法名称返回定位算法对象
 */
public class AlgorithmFactory {
    public static IAlgorithm createAlgorithm(String algorithmName) {
        switch (algorithmName) {
            case "PdrLocalOri":
                return new PdrLocalOri("gru.pt"); // 假设 PDRAlgorithm 是你的 PDR 算法类
            default:
                throw new IllegalArgumentException("Unknown algorithm: " + algorithmName);
        }
    }
}
