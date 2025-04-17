package com.example.pdr_locator;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Button;
import android.widget.Spinner;
import android.view.View;
import android.widget.AdapterView;
import androidx.appcompat.app.AppCompatActivity;

import com.example.pdr_locator.Thread.LocateThread;
import com.example.pdr_locator.Thread.Locator;
import com.example.pdr_locator.view.GraphView;
import com.example.pdr_locator.view.RajawaliGraphView;

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

    // UI Components
    private GraphView graphView; // 2D图形视图
    private RajawaliGraphView graph3DView; // 3D图形视图
    private Button startButton, stopButton, switchButton; // 三个按钮
    private Spinner algorithmSpinner, dimensionSpinner; //下拉选择框

    private int selectedDimension = 0; // 0=2D, 1=3D
    private boolean isLocating = false;

    // 我的定位结果放在这个mHandler里，就是这个coordinate，它是double[3],[x,y,z],在这里调用你的更新UI的函数
    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            double[] coordinate = (double[]) msg.obj;
            // 更新 UI
            updateUI(coordinate);
        }
    };  // 接收定位结果，每当定位线程中得到一个定位结果，就以message形式发送到mHandler

    /**
     * Activity创建时调用的方法，初始化UI和事件监听器
     *
     * @param savedInstanceState 保存的实例状态
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        graphView = findViewById(R.id.graph_view); // 2D图形视图
        graph3DView = findViewById(R.id.graph_3d_view); // 3D图形视图
        graph3DView.setClickable(true);
        startButton = findViewById(R.id.start_button); // 开始按钮
        stopButton = findViewById(R.id.stop_button); // 停止按钮
        switchButton = findViewById(R.id.switch_button); // 切换按钮(3D视图自动旋转)
        algorithmSpinner = findViewById(R.id.algorithm_spinner); // 算法选择下拉框
        dimensionSpinner = findViewById(R.id.dimension_spinner); // 维度选择下拉框

        // 设置维度选择监听器
        dimensionSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedDimension = position;
                if (position == 0) { // 2D
                    graphView.setVisibility(View.VISIBLE);
                    graph3DView.setVisibility(View.GONE);
                    switchButton.setVisibility(View.GONE);
                } else { // 3D
                    graphView.setVisibility(View.GONE);
                    graph3DView.setVisibility(View.VISIBLE);
                    switchButton.setVisibility(View.VISIBLE);
                }
                clearGraphs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        /*
        当点击选择算法按钮时，按下面这个方法创建Locator对象，并用locator.serAlgorithm("算法名称")来设置定位算法，如下：
            locator.setAlgorithm("PdrLocalOri");
            locator.setHandler(mHandler);
        */
        algorithmSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String algorithm = getAlgorithmName(position);
                locator.setAlgorithm(algorithm);
                locator.setHandler(mHandler);
                clearGraphs();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // 开始按钮
        startButton.setOnClickListener(v -> {
            if (!isLocating) {
                startLocating();
                startButton.setEnabled(false);
                stopButton.setEnabled(true);
            }
        });

        // 停止按钮
        stopButton.setOnClickListener(v -> {
            if (isLocating) {
                stopLocating();
                startButton.setEnabled(true);
                stopButton.setEnabled(false);
            }
        });

        // 切换按钮
        switchButton.setOnClickListener(v -> {
            graph3DView.notIsAutoRotate();
        });

        // 初始状态
        stopButton.setEnabled(false);
        switchButton.setVisibility(View.GONE);
    }

    /**
     * 根据位置索引获取算法名称
     *
     * @param position 下拉框中的位置索引
     * @return 对应的算法名称字符串
     */
    private String getAlgorithmName(int position) {
        switch (position) {
            case 0: return "PdrLocalOri";
            case 1: return "PdrBasic";
            case 2: return "PdrAdvanced";
            case 3: return "PdrWithFilter";
            default: return "PdrLocalOri";
        }
    }

    /*
    当点击开始定位时，需要新建定位线程，然后启动它，即开始定位，如下：
        locateThread = new LocateThread(locator);
        locateThread.start();
     */
    private void startLocating() {
        isLocating = true;
        clearGraphs();
        locateThread = new LocateThread(locator);
        locateThread.start();
    }

    /*
    当点击停止定位时，就调用locateThread的stopThread()方法，如下：
        locateThread.stopThread();
     */
    private void stopLocating() {
        isLocating = false;
        if (locateThread != null) {
            locateThread.stopThread();
        }
    }

    private void clearGraphs() {
        graphView.clearPoints();
        graph3DView.clearPoints();
    }


    //TODO 这里写更新UI的逻辑
    /**
     * 更新UI显示定位坐标
     *
     * @param coordinate 包含x,y,z坐标的数组
     */
    private void updateUI(double[] coordinate) {
        // 更新 UI 的逻辑
        if (coordinate == null || coordinate.length < 3) return;

        float x = (float) coordinate[0];
        float y = (float) coordinate[1];
        float z = (float) coordinate[2];

        runOnUiThread(() -> {
            if (selectedDimension == 0) {
                // 2D view - show X and Y coordinates
                graphView.addPoint(x, y);
            } else {
                // 3D view - show all three coordinates
                // Scale down coordinates for better visualization in 3D space
                graph3DView.addPoint(x/5, y/5, z/5);
            }
        });
    }

    /**
     * Activity暂停时调用，停止定位和3D视图
     */
    @Override
    protected void onPause() {
        super.onPause();
        if (isLocating) {
            stopLocating();
        }
        if (graph3DView != null) {
            graph3DView.onPause();
        }
    }

    /**
     * Activity暂停时调用，停止定位和3D视图
     */
    @Override
    protected void onResume() {
        super.onResume();
        if (graph3DView != null) {
            graph3DView.onResume();
        }
    }

    /**
     * Activity销毁时调用，确保停止定位
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopLocating();
    }


}