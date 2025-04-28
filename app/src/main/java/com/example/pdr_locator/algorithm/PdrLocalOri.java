package com.example.pdr_locator.algorithm;

import android.content.Context;
import android.util.Log;

import com.example.pdr_locator.model.Quat;
import com.example.pdr_locator.model.QuatModel;
import com.example.pdr_locator.model.SensorData;
import com.example.pdr_locator.utils.OnnxInferenceHelper;
import com.example.pdr_locator.utils.QuaternionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/13
 * @Time: 17:17
 */

/**
 * PdrLocalOri算法类
 */
public class PdrLocalOri implements IAlgorithm{
    private PDR pdr;  // pdr模型对象
    private boolean firstWin;  // 首个窗口
    private double minVar;  // acce数据方差最小阈值
    private QuatModel oriM;  // 四元数模型对象，存储当前四元数和历史四元数
    private int halfResetWindow;
    private double[] pxy;  // 此时的位置[x,y,z]
    public PdrLocalOri(Context context, String modelName) {
        this.pdr = new PDR(context, modelName);
        this.firstWin = true;
        this.minVar = 1000;
        this.halfResetWindow = 25;
        this.pxy = new double[]{0.0, 0.0, 0.0};
    }

    /**
     * 计算定位结果[x,y,z]
     * @param inputs 输入IMU数据
     * @return 返回定位结果double[] (x,y,z)
     */
    @Override
    public double[] getCoordinate(List<SensorData> inputs) throws Exception {
        long[][] timestamp = getTimestamp(inputs);  // 时间戳
        float[][] accelerometerData = getAccelerometer(inputs);  //加速度计三轴数据
        float[][] gyroscopeData = getGyroscope(inputs);  // 陀螺仪三轴数据
//        double[][] magnetometerData = getMagnetometer(inputs);  // 磁力计三轴数据

        float[] vxy = getVxy(timestamp, accelerometerData, gyroscopeData);  // 测得此时的x、y轴方向的速度

        Log.d("vxy", Arrays.toString(vxy));
        double timeDiff = (timestamp[timestamp.length-1][0] - timestamp[0][0])/1000.0;  // 窗口时间
        Log.d("timeDiff", String.valueOf(timeDiff));
        if(!Float.isInfinite(vxy[0]) && !Float.isInfinite(vxy[1]) && !Float.isNaN(vxy[0]) && !Float.isNaN(vxy[1])){
            pxy[0] = pxy[0] + vxy[0]*timeDiff;  // 计算当前坐标x
            pxy[1] = pxy[1] + vxy[1]*timeDiff;  // 计算当前坐标y
        }
        Log.d("xy", Arrays.toString(pxy));
        return pxy;
    }

    /**
     * 根据IMU数据预测速度vx和vy
     * @param time double[][] 二维数组，windowSize行，时间戳
     * @param acce double[][] 二维数组，windowSize行，加速度计三轴数据
     * @param gyro double[][] 二维数组，windowSize行，陀螺仪三轴数据
     * @return [vx, vy] x轴预测的速度、y轴预测的速度
     */
    public float[] getVxy(long[][] time, float[][] acce, float[][] gyro) throws Exception {


        // 1. 确保 oriM 初始化
        if (oriM == null) {
            initializeOrientationModel(acce);
        }

        // 2. 构建缓存数据
        float[][] cache = buildCache(time, acce, gyro);


        // 3. 计算加速度方差
        double var = calculateAccelerationVariance(acce);
        handleVarianceConditions(var, acce);

        // 4. 更新四元数
        updateQuaternions(cache);

        firstWin = false;

        // 5. 执行旋转和推理
        return processImuData(time, acce, gyro);
    }

    /**
     * oriM初始化，根据第一次的acce数据进行初始化
     *
     * @param acce 加速度计数据
     */
    private void initializeOrientationModel(float[][] acce) {
        // 记录acce数据
//        for (float[] sample : acce) {
//            Log.d("acceFloat", Arrays.toString(sample));
//        }

        // 计算初始重力方向(重力方向吗？还是运动加速度和重力加速度的合方向)
        float[] initGrav = new float[3];
        for (int i = 0; i < 3; i++) {
            double sum = 0;
            for (float[] sample : acce) {
                sum += sample[i];
            }
            initGrav[i] = (float) (sum / acce.length);
        }
        normalizeVector(initGrav);
        Quat initialQ = QuaternionUtil.getQFromGrav(initGrav, 0);
        this.oriM = new QuatModel(0.02, initialQ);
        this.halfResetWindow = 25;
    }

    /**
     * 构建数据缓存
     *
     * @param time 时间戳数据 [50,1]
     * @param acce 加速度计数据[50,3]
     * @param gyro 陀螺仪数据[50,3]
     * @return 返回cache数据[相邻时间间隔，acce三轴数据，gyro三轴数据] [50,7]
     */
    private float[][] buildCache(long[][] time, float[][] acce, float[][] gyro) {
        int rows = time.length;
        if(!firstWin){
            rows = 10;
        }
        int cols = time[0].length + gyro[0].length + acce[0].length;

        float[][] cache = new float[rows][cols];
        if(firstWin){
            for (int i = 0; i < rows; i++) {
                if(i==0){
                    cache[i][0] = 20.0f;
                }
                else{
                    cache[i][0] = time[i][0] - time[i-1][0];
                }

                // 传感器数据填充
                int sensorStart = 1;

                System.arraycopy(
                        gyro[i], 0,
                        cache[i], sensorStart,
                        gyro[i].length
                );

                System.arraycopy(
                        acce[i], 0,
                        cache[i], sensorStart + 3,
                        acce[i].length
                );
            }
        }
        else{
            for (int i = 0; i < rows; i++) {
                cache[i][0] = time[i+40][0] - time[i+39][0];

                // 传感器数据填充
                int sensorStart = 1;

                System.arraycopy(
                        gyro[i+40], 0,
                        cache[i], sensorStart,
                        gyro[i].length
                );

                System.arraycopy(
                        acce[i+40], 0,
                        cache[i], sensorStart + 3,
                        acce[i].length
                );
            }
        }
        return cache;
    }

    /**
     * 计算加速度计数据方差
     *
     * @param acce 加速度计三轴数据
     * @return 返回方差结果
     */
    private double calculateAccelerationVariance(float[][] acce) {
        double[] norms = new double[acce.length];
        for (int i = 0; i < acce.length; i++) {
            double sumSq = 0;
            for (int j = 0; j < 3; j++) {
                sumSq += Math.pow(acce[i][j], 2);
            }
            norms[i] = Math.sqrt(sumSq);
        }
        return calculateVariance(norms);
    }

    /**
     * 根据方差重置四元数
     *
     * @param var  acce数据方差
     * @param acce  acce数据
     */
    private void handleVarianceConditions(double var, float[][] acce) {
        if (var < minVar) {
            minVar = var;
            oriM.reset_q(acce);
        }
        if (var < 0.005) {
            oriM.reset_q(acce);
        }
    }

    private void updateQuaternions(float[][] cache) {
        for (float[] row : cache) {
            float[] params = Arrays.copyOfRange(row, 0, 4);
            oriM.update(params);
        }
    }

    private float[] processImuData(long[][] time, float[][] acce, float[][] gyro) throws Exception {
        // 获取最新的50个四元数
        List<double[]> qArrayList = oriM.getQArrayList();
        int start = Math.max(0, qArrayList.size() - 50);
        double[][] latestQ = new double[50][4];
        for (int i = 0; i < 50 && (start + i) < qArrayList.size(); i++) {
            latestQ[i] = qArrayList.get(start + i);
        }

        // 执行旋转
        double[][][] rotatedData = new double[time.length][2][3]; // [gyro, acce]
        for (int i = 0; i < time.length; i++) {
            double[] q = latestQ[Math.min(i, latestQ.length-1)];

            // 陀螺仪旋转
            double[] rotatedGyro = rotateVector(q, gyro[i]);
            System.arraycopy(rotatedGyro, 0, rotatedData[i][0], 0, 3);

            // 加速度计旋转
            double[] rotatedAcce = rotateVector(q, acce[i]);
            System.arraycopy(rotatedAcce, 0, rotatedData[i][1], 0, 3);
        }

        Log.d("acceInput", Arrays.toString(rotatedData[0][0]));
        Log.d("gyroInput", Arrays.toString(rotatedData[0][1]));
        // 转换为模型输入
        double[] inputData = flattenImuData(rotatedData);
        for(int i=0; i<10; i++){
            double[] subData = Arrays.copyOfRange(inputData, i*30, i*30+30);
            Log.d("InputData", Arrays.toString(subData));
        }
        Log.d("InputData", Arrays.toString(inputData));
        return pdr.run(inputData);
    }

    //=== 数学工具方法 ===//
    private static void normalizeVector(float[] vec) {
        float norm = (float) Math.sqrt(vec[0]*vec[0] + vec[1]*vec[1] + vec[2]*vec[2]);
        if(norm != 0.0){
            for (int i = 0; i < 3; i++) vec[i] /= norm;
        }
        Log.d("normGrav", Arrays.toString(vec));
    }

    private static double calculateVariance(double[] data) {
        double mean = Arrays.stream(data).average().orElse(0);
        return Arrays.stream(data)
                .map(v -> Math.pow(v - mean, 2))
                .average()
                .orElse(0);
    }

    private static double[] rotateVector(double[] quaternion, float[] vector) {
        double[] vec4 = {0, vector[0], vector[1], vector[2]};
        double[] conj = QuaternionUtil.inverseQuaternion(quaternion);
        double[] temp = QuaternionUtil.multiplyQuaternion(quaternion, vec4);
        double[] result = QuaternionUtil.multiplyQuaternion(temp, conj);
        return new double[]{result[1], result[2], result[3]};
    }

    private static double[] flattenImuData(double[][][] data) {
        double[] flat = new double[data.length * 6];
        int idx = 0;
        for (double[][] sample : data) {
            for (double[] sensor : sample) {
                for (double val : sensor) {
                    flat[idx++] = val;
                }
            }
        }
        return flat;
    }

    // 辅助方法：四元数旋转
    private static double[] rotateQuaternion(double[] q, double[] p) {
        double[] qConj = new double[]{q[0], -q[1], -q[2], -q[3]};
        double[] temp = QuaternionUtil.multiplyQuaternion(q, p);
        return QuaternionUtil.multiplyQuaternion(temp, qConj);
    }

    // 辅助方法：三维数组转一维
    private static double[] flatten3DArray(double[][][] arr) {
        List<Double> list = new ArrayList<>();
        for (double[][] matrix : arr) {
            for (double[] row : matrix) {
                for (double val : row) {
                    list.add(val);
                }
            }
        }
        double[] result = new double[list.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = list.get(i);
        }
        return result;
    }

    /**
     * 从输入double[数据行数][10]中提取第一列timestamp
     *
     * @param inputs 输入的IMU数据（包括时间戳、加速度计三轴数据、陀螺仪三轴数据、磁力计三轴数据）
     */
    private long[][] getTimestamp(List<SensorData> inputs){
        // 检查输入是否为空
        if (inputs == null || inputs.isEmpty()) {
            return new long[0][0];
        }

        int rows = inputs.size();  // 获取结果数组的行数
        long[][] result = new long[rows][1]; // 每一行只有一个元素

        for (int i = 0; i < rows; i++) {
            result[i][0] = inputs.get(i).getTimestamp();
        }
        return result;
    }

    /**
     * 从输入double[数据行数][10]中提取第2、3、4列加速度计三轴数据
     *
     * @param inputs 输入的IMU数据（包括时间戳、加速度计三轴数据、陀螺仪三轴数据、磁力计三轴数据）
     */
    private float[][] getAccelerometer(List<SensorData> inputs){
        // 检查输入是否为空
        if (inputs == null || inputs.isEmpty()) {
            return new float[0][0];
        }

        int rows = inputs.size();  // 获取结果数组的行数
        float[][] result = new float[rows][3]; // 每一行只有3个元素

        // 遍历每一行，取第一列的值
        for (int i = 0; i < rows; i++) {
            // 检查当前行是否为空
            result[i] = inputs.get(i).getAccelerometerData();
        }
        return result;
    }

    /**
     * 从输入double[数据行数][10]中提取第5、6、7列陀螺仪三轴数据
     *
     * @param inputs 输入的IMU数据（包括时间戳、加速度计三轴数据、陀螺仪三轴数据、磁力计三轴数据）
     */
    private float[][] getGyroscope(List<SensorData> inputs){
        // 检查输入是否为空
        if (inputs == null || inputs.isEmpty()) {
            return new float[0][0];
        }

        int rows = inputs.size();  // 获取结果数组的行数
        float[][] result = new float[rows][3]; // 每一行只有一个元素

        // 遍历每一行，取第一列的值
        for (int i = 0; i < rows; i++) {
            result[i] = inputs.get(i).getGyroscopeData();
        }
        return result;
    }

    /**
     * 从输入double[数据行数][10]中提取第8、9、10列磁力计三轴数据
     *
     * @param inputs 输入的IMU数据（包括时间戳、加速度计三轴数据、陀螺仪三轴数据、磁力计三轴数据）
     */
    private float[][] getMagnetometer(List<SensorData> inputs){
        // 检查输入是否为空
        if (inputs == null || inputs.isEmpty()) {
            return new float[0][0];
        }

        int rows = inputs.size();  // 获取结果数组的行数
        float[][] result = new float[rows][3]; // 每一行只有一个元素

        // 遍历每一行，取第一列的值
        for (int i = 0; i < rows; i++) {
            result[i] = inputs.get(i).getMagnetometerData();
        }
        return result;
    }
}

/**
 * PDR类，仅供PdrLocalOri算法使用
 */
class PDR{
    private Context context;  // 上下文
    private String modelName;  // 模型名称
    /**
     * 构造函数
     *
     * @param modelName 选择的模型文件名
     */
    public PDR(Context context, String modelName) {
        this.context = context;
        this.modelName = modelName;
    }

    /**
     * 运行算法得出结果
     * @param inputData 输入数据
     * @return 返回结果，
     */
    public float[] run(double[] inputData) throws Exception{
        OnnxInferenceHelper helper = new OnnxInferenceHelper(context, modelName);
        // 推理
        float[] output = helper.runInference(inputData);
        // 使用完后关闭
        helper.close();
        return output;
    }
}
