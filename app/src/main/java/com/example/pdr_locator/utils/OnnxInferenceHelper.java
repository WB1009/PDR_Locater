package com.example.pdr_locator.utils;

/**
 * @Author: Liu Wenbin
 * @Date: 2025/4/27
 * @Time: 22:52
 */
import android.content.Context;
import ai.onnxruntime.*;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;

public class OnnxInferenceHelper {

    private OrtEnvironment env;
    private OrtSession session;

    public OnnxInferenceHelper(Context context, String modelName){
        try{
            // 加载模型到字节数组
            InputStream is = context.getAssets().open(modelName);
            byte[] modelBytes = new byte[is.available()];
            is.read(modelBytes);
            is.close();

            // 初始化 ONNX Runtime 环境和 Session
            env = OrtEnvironment.getEnvironment();
            session = env.createSession(modelBytes);
        }
        catch (Exception e){
            Log.e("model_load", "模型加载失败");
        }

    }

    /**
     * 执行推理
     * @param accelGyroInput 你的传感器输入数据（假设为 float[]，长度根据你的模型输入定）
     * @return 推理输出（以 float[] 为例）
     */
    public float[] runInference(double[] accelGyroInput) throws Exception{
        // 模型输入名（请根据你的模型实际情况修改）
//        String inputName = session.getInputNames().iterator().next(); // 或写死："input" 等
        String inputName = "input"; // 或写死："input" 等

        float[][][] inputArray = new float[1][50][6];
        for(int i = 0; i<inputArray[0].length; i++){
            for(int j=0; j<inputArray[0][0].length; j++){
                inputArray[0][i][j] = (float)accelGyroInput[i*6+j];
            }
        }

        // 构造 ONNX 输入张量
        OnnxTensor tensor = OnnxTensor.createTensor(env, inputArray);// 输入映射
        Map<String, OnnxTensor> inputs = new HashMap<>();
        inputs.put(inputName, tensor);

        // 推理
        try (OrtSession.Result output = session.run(inputs)) {
            // 假设输出名同样只有一个
            OnnxValue ov = output.get(0);
            if (ov instanceof OnnxTensor) {
                // 输出类型和 shape 根据模型实际修改
                float[][] result = (float[][]) ((OnnxTensor) ov).getValue();
                return result[0]; // 或处理成你需要的格式
            } else {
                throw new Exception("Output is not a tensor");
            }
        }
    }

    public void close() throws Exception {
        if (session != null) session.close();
        if (env != null) env.close();
    }
}
