package com.example.pdr_locator.utils;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/13
 * @Time: 22:48
 */

import com.example.pdr_locator.model.Quat;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

/**
 * QuaternionUtil 类提供与四元数相关的通用工具方法。
 */
public class QuaternionUtil {

    /**
     * 归一化四元数，确保其模长为 1。
     *
     * @param quat 输入四元数
     * @return 归一化后的四元数
     */
    public static Quat normalizeQuaternion(Quat quat) {
        double[] q = quat.toDoubleArray(); // 四元数对象转为数组
        double norm = Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]); // 计算四元数的模长
        double[] norm_q = new double[4]; // 创建归一化后的四元数
        for (int i = 0; i < 4; i++) {
            norm_q[i] = q[i] / norm; // 归一化
        }
        return new Quat(norm_q);
    }

    /**
     * 将欧拉角转换为四元数。
     *
     * @param yaw   偏航角（弧度）
     * @param pitch 俯仰角（弧度）
     * @param roll  横滚角（弧度）
     * @return 转换后的四元数
     */
    public static Quat eulerToQuaternion(double yaw, double pitch, double roll) {
        double cy = Math.cos(yaw / 2.0); // 偏航角的半角余弦
        double sy = Math.sin(yaw / 2.0); // 偏航角的半角正弦
        double cr = Math.cos(roll / 2.0); // 横滚角的半角余弦
        double sr = Math.sin(roll / 2.0); // 横滚角的半角正弦
        double cp = Math.cos(pitch / 2.0); // 俯仰角的半角余弦
        double sp = Math.sin(pitch / 2.0); // 俯仰角的半角正弦

        // 根据欧拉角公式计算四元数
        double qw = cy * cr * cp + sy * sr * sp;
        double qx = cy * sr * cp - sy * cr * sp;
        double qy = cy * cr * sp + sy * sr * cp;
        double qz = sy * cr * cp - cy * sr * sp;

        return new Quat(qw, qx, qy, qz);
    }

    /**
     * 根据重力方向和偏航角计算四元数。
     *
     * @param grav 重力方向（归一化后的向量）
     * @param yaw  偏航角（弧度）
     * @return 计算后的四元数
     */
    public static Quat getQFromGrav(double[] grav, double yaw) {
        double pitch = Math.atan2(-grav[0], grav[2]); // 计算俯仰角
        double roll = Math.atan2(grav[1], Math.sqrt(grav[0] * grav[0] + grav[2] * grav[2])); // 计算横滚角
        return eulerToQuaternion(yaw, pitch, roll); // 转换为四元数
    }

    // 四元数旋转
    public static INDArray rotateQuaternion(INDArray oriQ, INDArray inputQ) {
        INDArray result = Nd4j.zeros(inputQ.shape());
        for (int i = 0; i < inputQ.rows(); i++) {
            double[] q = oriQ.getRow(i).toDoubleVector();
            double[] p = inputQ.getRow(i).toDoubleVector();
            // 四元数旋转公式：q * p * q^{-1}
            double[] rotated = QuaternionUtil.multiplyQuaternion(q, QuaternionUtil.multiplyQuaternion(p, QuaternionUtil.inverseQuaternion(q)));
            result.putRow(i, Nd4j.create(rotated));
        }
        return result;
    }

    // 四元数乘法
    public static double[] multiplyQuaternion(double[] q1, double[] q2) {
        double w = q1[0] * q2[0] - q1[1] * q2[1] - q1[2] * q2[2] - q1[3] * q2[3];
        double x = q1[0] * q2[1] + q1[1] * q2[0] + q1[2] * q2[3] - q1[3] * q2[2];
        double y = q1[0] * q2[2] - q1[1] * q2[3] + q1[2] * q2[0] + q1[3] * q2[1];
        double z = q1[0] * q2[3] + q1[1] * q2[2] - q1[2] * q2[1] + q1[3] * q2[0];
        return new double[]{w, x, y, z};
    }

    // 四元数逆
    public static double[] inverseQuaternion(double[] q) {
        double norm = q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3];
        return new double[]{q[0] / norm, -q[1] / norm, -q[2] / norm, -q[3] / norm};
    }
}