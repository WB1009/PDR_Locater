package com.example.pdr_locator;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.pdr_locator.Thread.LocateThread;
import com.example.pdr_locator.Thread.Locator;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/16
 * @Time: 22:48
 */

/**
 * 主活动
 */
public class MainActivity extends AppCompatActivity {
    private Locator locator = new Locator(this);  // 定位器
    private LocateThread locateThread;  // 定位线程

    // 我的定位结果放在这个mHandler里，就是这个coordinate，它是double[3],[x,y,z],在这里调用你的更新UI的函数
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            double[] coordinate = (double[]) msg.obj;
            // 更新 UI
            updateUI(coordinate);
        }
    };  // 接收定位结果，每当定位线程中得到一个定位结果，就以message形式发送到mHandler

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    //TODO 这里写更新UI的逻辑
    private void updateUI(double[] coordinate) {
        // 更新 UI 的逻辑
    }

    /*
    当点击选择算法按钮时，按下面这个方法创建Locator对象，并用locator.serAlgorithm("算法名称")来设置定位算法，如下：
        locator.setAlgorithm("PdrLocalOri");
        locator.setHandler(mHandler);
     */

    /*
    当点击开始定位时，需要新建定位线程，然后启动它，即开始定位，如下：
        locateThread = new LocateThread(locator);
        locateThread.start();
     */

    /*
    当点击停止定位时，就调用locateThread的stopThread()方法，如下：
        locateThread.stopThread();
     */

}