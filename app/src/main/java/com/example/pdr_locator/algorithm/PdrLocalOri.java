package com.example.pdr_locator.algorithm;

import android.content.Context;

import com.example.pdr_locator.model.Quat;

import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/13
 * @Time: 17:17
 */

/**
 * PdrLocalOri算法类
 */
public class PdrLocalOri implements IAlgorithm{
    private String modelName;  // 模型名称
    private PDR pdr;  // pdr模型对象
    private boolean firstWin;  // 首个窗口
    private double minVar;
    private Quat oriM;
    private int halfResetWindow;

//    private INDArray cacheNd4j;

    public PdrLocalOri(String modelname) {
        this.modelName = modelName;
        this.pdr = new PDR(modelname);
        this.firstWin = true;
        this.minVar = 1000;
        this.halfResetWindow = 25;
    }
    @Override
    public double[] getCoordinate(double[][] inputs){
        return new double[]{0.0, 0.0};
    }
}

/**
 * PDR类，仅供PdrLocalOri算法使用
 */
class PDR{
    private final Module model;  // pytorch模型

    /**
     * 构造函数
     *
     * @param modelName 选择的模型名称
     */
    public PDR(String modelName) {
        String modelPath = "../assets/" + modelName;  // 模型路径
        this.model = Module.load(modelPath);
    }

    /**
     * 运行算法得出结果
     * @param inputData 输入数据
     * @param seqLength
     * @return 返回结果，
     */
    public double[] run(double[] inputData, int seqLength) {
        // 输入形状为 [1, 6, seqLength]
        long[] inputShape = new long[]{1, 6, seqLength};
        Tensor inputTensor = Tensor.fromBlob(inputData, inputShape);

        // 执行推理
        IValue output = model.forward(IValue.from(inputTensor));
        Tensor outputTensor = output.toTensor();

        // 转换为输出数组
        return outputTensor.getDataAsDoubleArray();
    }
}
