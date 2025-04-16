package com.example.pdr_locator.utils;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/13
 * @Time: 14:13
 */
import com.example.pdr_locator.model.SensorData;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * SlidingWindowManager 类是基于滑动窗口方式读取采集的数据，并使用消息队列生成任务的工具
 */
public class SlidingWindowManager {
    private final int WINDOW_SIZE; // 窗口大小
    private final int SLIDE_STEP;  // 滑动步长
    private Queue<float[]> dataBuffer = new LinkedList<>(); // 数据缓冲区
    private LinkedBlockingQueue<float[][]> taskQueue = new LinkedBlockingQueue<>(); // 任务队列

    /**
     * 构造函数，初始化滑动窗口管理器的窗口大小和滑动步长
     * @param windowSize 滑动窗口大小
     * @param slideStep 滑动步长
     */
    public SlidingWindowManager(int windowSize, int slideStep){
        this.WINDOW_SIZE = windowSize;
        this.SLIDE_STEP = slideStep;

    }

    /**
     * 将最新采集到的IMU数据添加到数据缓冲区，并且当数据达到窗口大小时则生成一个任务
     * @param data double[10],一条数据，包括timepstamp(时间戳), 加速度计x,y,z, 陀螺仪x,y,z, 磁力计x,y,z
     */
    public synchronized void addData(float[] data) {
        dataBuffer.add(data);

        // 当数据足够形成一个窗口时，生成任务
        if (dataBuffer.size() >= SLIDE_STEP) {
            generateTask();
        }
    }

    /**
     * 生成一个任务，将任务加入消息队列，并删除已使用的、窗口外的缓存数据
     */
    private synchronized void generateTask() {
        int startIndex = 0;  // 数据遍历的开始下标
        while (startIndex + WINDOW_SIZE <= dataBuffer.size()) {
            float[][] windowData = new float[WINDOW_SIZE][];
            for (int i = 0; i < WINDOW_SIZE; i++) {
                windowData[i] = dataBuffer.toArray(new float[0][])[startIndex + i];
            }
            taskQueue.add(windowData);
            startIndex += SLIDE_STEP;
        }

        // 移除已经处理的数据
        for (int i = 0; i < startIndex; i++) {
            dataBuffer.poll();
        }
    }

    /**
     * 从消息队列中获取一个任务
     * @return float[][] 一个任务，即包含一个窗口大小长度的数据，一行为一条数据
     * @throws InterruptedException 可能抛出InterruptedException
     */
    public float[][] getTask() throws InterruptedException {
        return taskQueue.take();
    }
}
