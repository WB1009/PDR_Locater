package com.example.pdr_locator.Thread;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.pdr_locator.algorithm.AlgorithmFactory;
import com.example.pdr_locator.algorithm.IAlgorithm;
import com.example.pdr_locator.model.SensorData;
import com.example.pdr_locator.model.SensorType;
import com.example.pdr_locator.sensor.SensorDataCollector;
import com.example.pdr_locator.utils.SlidingWindowManager;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/16
 * @Time: 21:04
 */

/**
 * 定位器类，一个中间层，设置使用的算法和相应的传感器类型
 */
@Getter
@Setter
public class Locator {
    private static final Map<String, List<SensorType>> ALGORITHM_SENSOR_MAPPING = new HashMap<>();  // 算法名称与传感器组合的映射

    static {
        // 初始化映射关系
        ALGORITHM_SENSOR_MAPPING.put("PdrLocalOri", Arrays.asList(SensorType.ACCELEROMETER, SensorType.GYROSCOPE));
        ALGORITHM_SENSOR_MAPPING.put("CollectData", Arrays.asList(SensorType.ACCELEROMETER, SensorType.GYROSCOPE, SensorType.MAGNETOMETER));
        // 下面可以继续添加其他算法和传感器组合
    }

    private Context context;  // 上下文
    private String algorithmName;  // 算法名称
    private List<SensorType> sensorType;  // 传感器类型
    private IAlgorithm algorithm;  // 算法
    private SensorDataCollector collector;  // 传感器数据采集器
    private SlidingWindowManager slidingWindowManager;  // 滑动窗口管理器

    private String directoryName = "SensorData";
    private String fileName = "sensor_data.csv";
    private boolean append = true;
    private FileWriter fileWriter;

    private Handler mHandler;  // 定位结果消息处理器

    /**
     * 构造函数
     * @param context  // 上下文
     */
    public Locator(Context context) {
        this.context = context;
    }

    public void setFileWriter(String fileName){
        try {
            // 获取应用私有目录（外部存储）
            File dir = context.getExternalFilesDir(null);
            File csvFile = new File(dir, fileName);
            Log.d("phoneDir", dir.getAbsolutePath());
            if (!csvFile.exists()) {
                boolean is = csvFile.createNewFile(); // 创建文件
                System.out.println(is);
            }
            this.fileWriter = new FileWriter(csvFile, append);
        } catch (IOException e) {
            Log.i("ExternalStoragePath", "IO问题");
            e.printStackTrace();
        }
        this.collector = new SensorDataCollector(context, sensorType, this.fileWriter);
        this.slidingWindowManager = collector.slidingWindowManager;
    }

    /**
     * 设置mHandler
     * @param handler 定位结果消息处理器
     */
    public void setHandler(Handler handler) {
        this.mHandler = handler;
    }

    /**
     * 设置定位器的定位算法
     * @param algorithmName 算法名称
     */
    public void setAlgorithm(String algorithmName){
        this.algorithmName = algorithmName;
        this.sensorType = ALGORITHM_SENSOR_MAPPING.get(algorithmName);
        // 动态加载算法和传感器
        this.algorithm = AlgorithmFactory.createAlgorithm(context, algorithmName);
    }

    /**
     * 定位函数，从消息队列接收任务数据，得到定位结果并使用message传递定位结果
     * @throws InterruptedException
     */
    public void locate() throws Exception {
        List<SensorData> sensorData = slidingWindowManager.getTask(); // 获取传感器数据
        double[] coordinate = algorithm.getCoordinate(sensorData);
        // 将结果传递到主线程
        if (mHandler != null) {
            Message msg = mHandler.obtainMessage();
            msg.obj = coordinate;
            mHandler.sendMessage(msg);
        }
    }

    /**
     * 数据采集器启动
     */
    public void onCreate(){
        collector.startCollecting();
    }

    /**
     * 数据采集器停止
     */
    public void onDestroy() {
        algorithm.reset();  // 定位器重置定位结果
        collector.stopCollecting(); // 卸载传感器
    }
}
