package com.example.pdr_locator.model;

import com.example.pdr_locator.utils.QuaternionUtil;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/13
 * @Time: 22:21
 */

/**
 * QuatModel 类用于处理四元数的姿态估计和更新。
 * 该类实现了基于四元数的扩展卡尔曼滤波（EKF）算法，
 * 用于从传感器数据（如陀螺仪和加速度计）中估计姿态。
 */
@Getter
@Setter
public class QuatModel {
    private Quat q;  // 四元数对象q
    private double halfT;  // 时间步长的一半
    private double[][] AMatrix;  // 旋转矩阵，用于四元数更新
    private List<Quat> qList;  // 存储四元数历史记录的列表
    private List<double[]> qArrayList; // 存储数组型四元数历史记录列表

    /**
     * 构造函数1，初始化默认四元数model和相关参数。
     *
     * @param period 时间步长（秒）
     */
    public QuatModel(double period) {
        this.halfT = 0.5 * period; // 时间步长的一半
        this.AMatrix = new double[4][4]; // 初始化旋转矩阵
        this.q = new Quat(1.0, 0.0, 0.0, 0.0); // 默认四元数（单位四元数）
        this.qList = new ArrayList<>(); // 初始化四元数历史记录列表
        this.qArrayList = new ArrayList<>(); // 初始化四元数数组历史记录
        this.q = QuaternionUtil.normalizeQuaternion(this.q); // 归一化四元数
    }

    /**
     * 构造函数2，初始化四元数model和相关参数
     *
     * @param period 时间步长（秒）
     * @param q 四元数姿态Quat
     */
    public QuatModel(double period, Quat q) {
        this.halfT = 0.5 * period; // 时间步长的一半
        this.AMatrix = new double[4][4]; // 初始化旋转矩阵
        this.q = q; // 传入四元数
        this.qList = new ArrayList<>(); // 初始化四元数历史记录列表
        this.qArrayList = new ArrayList<>(); // 初始化四元数数组历史记录
        this.q = QuaternionUtil.normalizeQuaternion(this.q); // 归一化四元数
    }

    public List<Quat> getQList(){
        return this.qList;
    }

    public List<double[]> getQArrayList(){
        return this.qArrayList;
    }

    /**
     * 根据输入的加速度计数据重置四元数。
     * 该方法用于初始化姿态估计。
     *
     * @param data 输入的加速度计数据（多个样本）
     */
    public void reset_q(float[][] data) {
        // 计算加速度计数据的平均值
        float[] grav = new float[3];
        for (int i = 0; i < 3; i++) {
            double sum = 0.0;
            for (float[] datum : data) {
                sum += datum[i];
            }
            grav[i] = (float) (sum / data.length); // 平均值
        }

        // 归一化重力方向
        double norm = Math.sqrt(grav[0] * grav[0] + grav[1] * grav[1] + grav[2] * grav[2]);
        if(norm != 0.0){
            for (int i = 0; i < 3; i++) {
                grav[i] /= norm;
            }
        }

        // 计算偏航角（yaw）
        double yaw = Math.atan2(2.0 * (q.getQw() * q.getQz() + q.getQx() * q.getQy()),
                1.0 - 2.0 * (q.getQy() * q.getQy() + q.getQz() * q.getQz()));
        // 根据重力方向和偏航角更新四元数
        q = QuaternionUtil.getQFromGrav(grav, yaw);
    }

    /**
     * 根据陀螺仪数据更新四元数（预测步骤）。
     *
     * @param dt 时间步长（毫秒）
     * @param gx 陀螺仪 x 轴数据
     * @param gy 陀螺仪 y 轴数据
     * @param gz 陀螺仪 z 轴数据
     */
    public void priori(double dt, double gx, double gy, double gz) {
        // 更新时间步长的一半
        this.halfT = 0.5 * dt / 1000;
        // 计算旋转矩阵的参数
        double gx_ = gx * this.halfT;
        double gy_ = gy * this.halfT;
        double gz_ = gz * this.halfT;

        // 构造旋转矩阵
        this.AMatrix = new double[][]{
                {1, -gx_, -gy_, -gz_},
                {gx_, 1, gz_, -gy_},
                {gy_, -gz_, 1, gx_},
                {gz_, gy_, -gx_, 1}
        };

        // 使用旋转矩阵更新四元数
        double[] newQ = new double[4];
        for (int i = 0; i < 4; i++) {
            double[] qArray = this.q.toDoubleArray();
            for (int j = 0; j < 4; j++) {
                newQ[i] += this.AMatrix[i][j] * qArray[j]; // 矩阵乘法
            }
        }

        this.q = new Quat(newQ);
    }

    /**
     * 保存当前四元数到历史记录中。
     * 如果四元数的 w 分量为负，则存储其负值以保持一致性。
     */
    public void saveQ() {
        if (q.getQw() > 0) {
            // 如果 w 分量为正，直接保存
            qList.add(q);
            qArrayList.add(q.toDoubleArray());
        } else {
            // 如果 w 分量为负，保存其负值
            qList.add(q.navigateQuat());
            qArrayList.add(q.navigateQuat().toDoubleArray());
        }
    }

    /**
     * 更新四元数（主流程）。
     * 该方法调用 priori 方法进行预测，然后归一化并保存四元数。
     *
     * @param data 输入数据（时间步长、陀螺仪 x、y、z）
     */
    public void update(float[] data) {
        // 调用 priori 方法进行预测
        priori(data[0], data[1], data[2], data[3]);
        // 归一化四元数
        q = QuaternionUtil.normalizeQuaternion(q);
        // 保存四元数
        saveQ();
    }
}
