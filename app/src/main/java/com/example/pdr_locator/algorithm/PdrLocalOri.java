package com.example.pdr_locator.algorithm;

import android.content.Context;

import com.example.pdr_locator.model.Quat;
import com.example.pdr_locator.model.QuatModel;
import com.example.pdr_locator.utils.QuaternionUtil;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.pytorch.IValue;
import org.pytorch.Module;
import org.pytorch.Tensor;
import org.nd4j.linalg.ops.transforms.Transforms;

import java.util.List;

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
    private QuatModel oriM;
    private int halfResetWindow;
    private double[] pxy;  // 此时的位置[x,y]

    public PdrLocalOri(String modelname) {
        this.modelName = modelName;
        this.pdr = new PDR(modelname);
        this.firstWin = true;
        this.minVar = 1000;
        this.halfResetWindow = 25;
        this.pxy = new double[]{0.0, 0.0};
    }

    /**
     * 计算定位结果[x,y]
     * @param inputs 输入IMU数据
     * @return 返回定位结果double[] (x,y)
     */
    @Override
    public double[] getCoordinate(double[][] inputs){
        double[][] timestamp = getTimestamp(inputs);  // 时间戳
        double[][] accelerometerData = getAccelerometer(inputs);  //加速度计三轴数据
        double[][] gyroscopeData = getGyroscope(inputs);  // 陀螺仪三轴数据
//        double[][] magnetometerData = getMagnetometer(inputs);  // 磁力计三轴数据

        double[] vxy = getVxy(timestamp, accelerometerData, gyroscopeData);  // 测得此时的x、y轴方向的速度
        double timeDiff = (timestamp[timestamp.length-1][0] - timestamp[0][0])/1000.0;  // 窗口时间
        pxy[0] = pxy[0] + vxy[0]*timeDiff;  // 计算当前坐标x
        pxy[1] = pxy[1] + vxy[1]*timeDiff;  // 计算当前坐标y
        return pxy;
    }

    /**
     * 根据IMU数据预测速度vx和vy
     * @param time double[][] 二维数组，windowSize行，时间戳
     * @param acce double[][] 二维数组，windowSize行，加速度计三轴数据
     * @param gyro double[][] 二维数组，windowSize行，陀螺仪三轴数据
     * @return [vx, vy] x轴预测的速度、y轴预测的速度
     */
    public double[] getVxy(double[][] time, double[][] acce, double[][] gyro) {
        // 将输入转换为 INDArray
        INDArray timeNd4j = Nd4j.create(time);
        INDArray acceNd4j = Nd4j.create(acce);
        INDArray gyroNd4j = Nd4j.create(gyro);

        // 初始化四元数
        INDArray cacheNd4j;
        if (firstWin) {
            INDArray initGrav = acceNd4j.getColumns(0, 2).mean(0).div(acceNd4j.getColumns(0, 2).mean(0).norm2());
            Quat oriQ = QuaternionUtil.getQFromGrav(initGrav.toDoubleVector(), 0);
            this.oriM = new QuatModel(0.02, oriQ);
            this.halfResetWindow = 25;

            // 计算时间间隔
            INDArray deltaTime = timeNd4j.getColumns(1, timeNd4j.columns() - 1).sub(timeNd4j.getColumns(0, timeNd4j.columns() - 2));
            INDArray zeroCol = Nd4j.zeros(deltaTime.rows(), 1);
            zeroCol.putScalar(0, 20.0);
            cacheNd4j = Nd4j.hstack(zeroCol, deltaTime, gyroNd4j, acceNd4j);
        } else {
            // 计算时间间隔
            INDArray deltaTime = timeNd4j.getColumns(1, timeNd4j.columns() - 1).sub(timeNd4j.getColumns(0, timeNd4j.columns() - 2));
            INDArray zeroCol = Nd4j.zeros(deltaTime.rows(), 1);
            zeroCol.putScalar(0, time[40][0] - time[39][0]);
            cacheNd4j = Nd4j.hstack(zeroCol, deltaTime, gyroNd4j.getColumns(0, 9), acceNd4j.getColumns(0, 9));
        }

        // 计算加速度的方差
        INDArray acceNorm = Transforms.sqrt(acceNd4j.mul(acceNd4j).sum(1));
        double var = acceNorm.varNumber().doubleValue();

        if (var < minVar) {
            minVar = var;
            oriM.reset_q(acce);
        }
        if (var < 0.005) {
            oriM.reset_q(acce);
        }

        // 更新四元数
        for (int t = 0; t < cacheNd4j.rows(); t++) {
            INDArray raw = cacheNd4j.getRow(t);
            oriM.update(raw.getColumns(0, 4).toDoubleVector());
        }

        // 获取最新的50个四元数
        List<double[]> qList = oriM.getQArrayList();
        double[][] latestQ = new double[50][4];
        for (int i = qList.size() - 50; i < qList.size(); i++) {
            latestQ[i - (qList.size() - 50)] = qList.get(i);
        }
        INDArray oriQ = Nd4j.create(latestQ);

        // 拼接四元数数组
        INDArray gyroWithZero = Nd4j.concat(1, Nd4j.zeros(gyroNd4j.rows(), 1), gyroNd4j);
        INDArray acceWithZero = Nd4j.concat(1, Nd4j.zeros(acceNd4j.rows(), 1), acceNd4j);

        // 四元数旋转
        INDArray globGyro = QuaternionUtil.rotateQuaternion(oriQ, gyroWithZero);
        INDArray globAcce = QuaternionUtil.rotateQuaternion(oriQ, acceWithZero);

        // 组合IMU数据
        INDArray imu = Nd4j.hstack(globGyro.getColumns(1, 4), globAcce.getColumns(1, 4));

        double[] inputs = imu.transpose().reshape(1, 6, imu.rows()).toDoubleVector();

        // 调用PDR模型
        double[] vxy = pdr.run(inputs, imu.columns());

        firstWin = false;

        // 将结果转换为 double[]
        return vxy;
    }

    /**
     * 从输入double[数据行数][10]中提取第一列timestamp
     *
     * @param inputs 输入的IMU数据（包括时间戳、加速度计三轴数据、陀螺仪三轴数据、磁力计三轴数据）
     */
    private double[][] getTimestamp(double[][] inputs){
        // 检查输入是否为空
        if (inputs == null || inputs.length == 0) {
            return new double[0][0];
        }

        int rows = inputs.length;  // 获取结果数组的行数
        double[][] result = new double[rows][1]; // 每一行只有一个元素

        // 遍历每一行，取第一列的值
        for (int i = 0; i < rows; i++) {
            // 检查当前行是否为空
            if (inputs[i] != null && inputs[i].length > 0) {
                result[i][0] = inputs[i][0]; // 将第1列的值存储到结果数组中
            } else {
                // 如果某一行为空或长度为0，可以抛出异常或设置默认值
                result[i][0] = 0.0; // 示例中直接设置为0
            }
        }
        return result;
    }

    /**
     * 从输入double[数据行数][10]中提取第2、3、4列加速度计三轴数据
     *
     * @param inputs 输入的IMU数据（包括时间戳、加速度计三轴数据、陀螺仪三轴数据、磁力计三轴数据）
     */
    private double[][] getAccelerometer(double[][] inputs){
        // 检查输入是否为空
        if (inputs == null || inputs.length == 0) {
            return new double[0][0];
        }

        int rows = inputs.length;  // 获取结果数组的行数
        double[][] result = new double[rows][3]; // 每一行只有3个元素

        // 遍历每一行，取第一列的值
        for (int i = 0; i < rows; i++) {
            // 检查当前行是否为空
            if (inputs[i] != null && inputs[i].length > 0) {
                result[i][1] = inputs[i][1]; // 将第2列的值存储到结果数组中
                result[i][2] = inputs[i][2]; // 将第3列的值存储到结果数组中
                result[i][3] = inputs[i][3]; // 将第4列的值存储到结果数组中
            } else {
                // 如果某一行为空或长度为0，可以抛出异常或设置默认值
                result[i] = new double[]{0.0, 0.0, 0.0}; // 示例中直接设置为0
            }
        }
        return result;
    }

    /**
     * 从输入double[数据行数][10]中提取第5、6、7列陀螺仪三轴数据
     *
     * @param inputs 输入的IMU数据（包括时间戳、加速度计三轴数据、陀螺仪三轴数据、磁力计三轴数据）
     */
    private double[][] getGyroscope(double[][] inputs){
        // 检查输入是否为空
        if (inputs == null || inputs.length == 0) {
            return new double[0][0];
        }

        int rows = inputs.length;  // 获取结果数组的行数
        double[][] result = new double[rows][3]; // 每一行只有一个元素

        // 遍历每一行，取第一列的值
        for (int i = 0; i < rows; i++) {
            // 检查当前行是否为空
            if (inputs[i] != null && inputs[i].length > 0) {
                result[i][4] = inputs[i][4]; // 将第5列的值存储到结果数组中
                result[i][5] = inputs[i][5]; // 将第6列的值存储到结果数组中
                result[i][6] = inputs[i][6]; // 将第7列的值存储到结果数组中
            } else {
                // 如果某一行为空或长度为0，可以抛出异常或设置默认值
                result[i] = new double[]{0.0, 0.0, 0.0}; // 示例中直接设置为0
            }
        }
        return result;
    }

    /**
     * 从输入double[数据行数][10]中提取第8、9、10列磁力计三轴数据
     *
     * @param inputs 输入的IMU数据（包括时间戳、加速度计三轴数据、陀螺仪三轴数据、磁力计三轴数据）
     */
    private double[][] getMagnetometer(double[][] inputs){
        // 检查输入是否为空
        if (inputs == null || inputs.length == 0) {
            return new double[0][0];
        }

        int rows = inputs.length;  // 获取结果数组的行数
        double[][] result = new double[rows][3]; // 每一行只有一个元素

        // 遍历每一行，取第一列的值
        for (int i = 0; i < rows; i++) {
            // 检查当前行是否为空
            if (inputs[i] != null && inputs[i].length > 0) {
                result[i][7] = inputs[i][7]; // 将第8列的值存储到结果数组中
                result[i][8] = inputs[i][8]; // 将第9列的值存储到结果数组中
                result[i][9] = inputs[i][9]; // 将第10列的值存储到结果数组中
            } else {
                // 如果某一行为空或长度为0，可以抛出异常或设置默认值
                result[i] = new double[]{0.0, 0.0, 0.0}; // 示例中直接设置为0
            }
        }
        return result;
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
