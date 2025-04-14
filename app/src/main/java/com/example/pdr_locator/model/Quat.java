package com.example.pdr_locator.model;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/13
 * @Time: 22:19
 */

/**
 * 四元数存储结构
 */
@Getter
@Setter
public class Quat {
    private double qw;  // 四元数实部w
    private double qx;  // x
    private double qy;  // y
    private double qz;  // z

    /**
     * 构造函数1 根据4个数据构造四元数对象
     * @param w 传入w
     * @param x 传入x
     * @param y 传入y
     * @param z 传入z
     */
    public Quat(double w, double x, double y, double z){
        this.qw = w;
        this.qx = x;
        this.qy = y;
        this.qz = z;
    }

    /**
     * 构造函数2，根据一个double数组构造一个四元数对象
     * @param q double数组q
     */
    public Quat(double[] q){
        if(q.length == 4){
            this.qw = q[0];
            this.qx = q[1];
            this.qy = q[2];
            this.qz = q[3];
        }
    }

    /**
     * 获取当前四元数的相反四元数对象
     * @return 一个四个相反数的四元数对象Quat
     */
    public Quat navigateQuat(){
        return new Quat(-qw, -qx, -qy, -qz);
    }

    /**
     * 将四元数对象转换为一个double数组
     * @return 返回四元数数组
     */
    public double[] toDoubleArray(){
        return new double[]{qw, qx, qy, qz};
    }
}
