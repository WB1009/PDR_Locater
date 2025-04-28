package com.example.pdr_locator.Thread;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/16
 * @Time: 21:49
 */

/**
 * 定位线程类
 */
public class LocateThread extends Thread{
    private Locator locator;  // 定位器
    private boolean running;  // 线程是否运行

    /**
     * 构造函数
     * @param locator 定位器
     */
    public LocateThread(Locator locator) {
        this.locator = locator;
    }

    /**
     * 启动线程，定位器启动，开始采集数据并定位
     */
    @Override
    public void run() {
        running = true;
        locator.onCreate();
        if(!locator.getAlgorithmName().equals("CollectData")){
            while (running) {
                try {
                    locator.locate();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * 停止线程，定位器停止。注销传感器
     */
    public void stopThread() {
        running = false;
        locator.onDestroy();
        interrupt(); // 中断线程
    }
}
