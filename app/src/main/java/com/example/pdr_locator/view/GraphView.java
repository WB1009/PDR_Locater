package com.example.pdr_locator.view;

/**
 * @Author: Bai Ruiqi
 * @Date: 2025/4/15
 * @Time: 19:19
 */
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * GraphView 类实现了一个可缩放的2D坐标系视图，支持绘制数据点和连线
 * 功能包括：
 * - 绘制坐标轴和网格
 * - 添加/清除数据点
 * - 支持双指缩放视图
 * - 自动调整坐标范围
 */
public class GraphView extends View {
    private Paint axisPaint; // 坐标轴画笔
    private Paint gridPaint; // 网格画笔
    private Paint pointPaint; // 数据点画笔
    private Paint linePaint; // 连线画笔

    private List<Float> xValues = new ArrayList<>(); //x坐标集合
    private List<Float> yValues = new ArrayList<>(); // y坐标集合

    private float minX = -10, maxX = 10;// 当前视图的坐标范围
    private float minY = -10, maxY = 10;

    private float scaleFactor = 1.0f; // 当前缩放因子
    private ScaleGestureDetector scaleGestureDetector; // 缩放手势检测器
    private static final float MIN_SCALE = 0.5f; // 最小缩放比例
    private static final float MAX_SCALE = 5.0f; // 最大缩放比例

    /**
     * 构造方法1：通过代码创建视图时调用
     *
     * @param context 上下文环境
     */
    public GraphView(Context context) {
        super(context);
        init();
    }

    /**
     * 构造方法2：通过XML布局创建视图时调用
     *
     * @param context 上下文环境
     * @param attrs 属性集合
     */
    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    /**
     * 初始化视图组件和画笔
     * 创建各种绘制用的Paint对象并设置初始参数
     * 初始化缩放手势检测器
     */
    private void init() {
        // 初始化坐标轴画笔
        axisPaint = new Paint();
        axisPaint.setColor(Color.BLACK);
        axisPaint.setStrokeWidth(3);
        axisPaint.setStyle(Paint.Style.STROKE);

        // 初始化网格画笔
        gridPaint = new Paint();
        gridPaint.setColor(Color.LTGRAY);
        gridPaint.setStrokeWidth(1);
        gridPaint.setStyle(Paint.Style.STROKE);

        // 初始化数据点画笔
        pointPaint = new Paint();
        pointPaint.setColor(Color.RED);
        pointPaint.setStrokeWidth(10);
        pointPaint.setStyle(Paint.Style.STROKE);
        pointPaint.setStrokeCap(Paint.Cap.ROUND);

        // 初始化连线画笔
        linePaint = new Paint();
        linePaint.setColor(Color.BLUE);
        linePaint.setStrokeWidth(3);
        linePaint.setStyle(Paint.Style.STROKE);

        // 初始化缩放手势检测器
        scaleGestureDetector = new ScaleGestureDetector(getContext(), new ScaleListener());
    }

    /**
     * 处理触摸事件
     * 将触摸事件传递给缩放手势检测器处理
     *
     * @param event 触摸事件对象
     * @return 始终返回true表示已处理该事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // 将触摸事件传递给缩放手势检测器
        scaleGestureDetector.onTouchEvent(event);
        return true;
    }

    /**
     * 缩放手势监听器内部类
     * 处理双指缩放手势并调整视图的坐标范围
     */
    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {
            float oldScale = scaleFactor;
            scaleFactor *= detector.getScaleFactor();

            // 限制缩放范围
            scaleFactor = Math.max(MIN_SCALE, Math.min(scaleFactor, MAX_SCALE));

            // 调整坐标范围以保持中心点不变
            float centerX = (minX + maxX) / 2;
            float centerY = (minY + maxY) / 2;

            // 计算新的坐标范围
            float newWidth = (maxX - minX) * (oldScale / scaleFactor);
            float newHeight = (maxY - minY) * (oldScale / scaleFactor);

            // 更新坐标范围
            minX = centerX - newWidth / 2;
            maxX = centerX + newWidth / 2;
            minY = centerY - newHeight / 2;
            maxY = centerY + newHeight / 2;

            invalidate();//重新绘图
            return true;
        }
    }

    /**
     * 添加数据点到视图
     * 会自动调整坐标范围以包含新点，并触发重绘
     *
     * @param x 数据点的x坐标
     * @param y 数据点的y坐标
     * @throws IllegalArgumentException 如果坐标值为NaN或无限大
     */
    public void addPoint(float x, float y) {
        if (Float.isNaN(x) || Float.isInfinite(x) ||
                Float.isNaN(y) || Float.isInfinite(y)) {
            throw new IllegalArgumentException("坐标值不能为NaN或无限大");
        }
        xValues.add(x);
        yValues.add(y);

        // 自动调整坐标范围
        if (x < minX) minX = x;
        if (x > maxX) maxX = x;
        if (y < minY) minY = y;
        if (y > maxY) maxY = y;

        invalidate();
    }

    /**
     * 清除所有数据点
     * 重置坐标范围和缩放因子，并触发重绘
     */
    public void clearPoints() {
        xValues.clear();
        yValues.clear();
        // 重置坐标范围和缩放因子
        minX = -10; maxX = 10;
        minY = -10; maxY = 10;
        scaleFactor = 1.0f;
        invalidate();
    }

    /**
     * 绘制视图内容
     * 包括网格、坐标轴和数据点连线
     *
     * @param canvas 用于绘制的画布对象
     */
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int width = getWidth();
        int height = getHeight();
        int padding = 50; // 边距

        // 绘制网格
        drawGrid(canvas, width, height, padding);

        // 绘制坐标轴
        drawAxes(canvas, width, height, padding);

        // 绘制数据点
        drawDataPoints(canvas, width, height, padding);
    }

    /**
     * 绘制网格线
     *
     * @param canvas 画布对象
     * @param width 视图可用宽度
     * @param height 视图可用高度
     * @param padding 视图内边距
     */
    private void drawGrid(Canvas canvas, int width, int height, int padding) {
        //留出边距
        float graphWidth = width - 2 * padding;
        float graphHeight = height - 2 * padding;

        // 水平网格线
        for (int i = 0; i <= 10; i++) {
            float y = padding + (i * graphHeight / 10);
            canvas.drawLine(padding, y, width - padding, y, gridPaint);
        }

        // 垂直网格线
        for (int i = 0; i <= 10; i++) {
            float x = padding + (i * graphWidth / 10);
            canvas.drawLine(x, padding, x, height - padding, gridPaint);
        }
    }

    /**
     * 绘制坐标轴和刻度标签
     *
     * @param canvas 画布对象
     * @param width 视图可用宽度
     * @param height 视图可用高度
     * @param padding 视图内边距
     */
    private void drawAxes(Canvas canvas, int width, int height, int padding) {
        //计算中心点
        float centerX = width / 2f;
        float centerY = height / 2f;

        // 绘制X轴
        canvas.drawLine(padding, centerY, width - padding, centerY, axisPaint);

        // 绘制Y轴
        canvas.drawLine(centerX, height - padding, centerX, padding, axisPaint);

        // 绘制刻度
        Paint textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(24);

        // X轴刻度
        for (int i = 0; i <= 10; i++) {
            float x = padding + (i * (width - 2 * padding) / 10.0f);
            String label = String.format("%.1f", minX + (maxX - minX) * i / 10);
            canvas.drawText(label, x - 15, centerY + 30, textPaint);
        }

        // Y轴刻度
        for (int i = 0; i <= 10; i++) {
            float y = padding + (i * (height - 2 * padding) / 10.0f);
            String label = String.format("%.1f", maxY - (maxY - minY) * i / 10);
            canvas.drawText(label, centerX + 10, y + 10, textPaint);
        }
    }

    /**
     * 绘制数据点和它们之间的连线
     *
     * @param canvas 画布对象
     * @param width 视图可用宽度
     * @param height 视图可用高度
     * @param padding 视图内边距
     */
    private void drawDataPoints(Canvas canvas, int width, int height, int padding) {
        if (xValues.isEmpty()) return; // 没有数据点时直接返回

        float graphWidth = width - 2 * padding;
        float graphHeight = height - 2 * padding;
        float centerX = width / 2f;
        float centerY = height / 2f;

        Path path = new Path(); // 用于绘制连线
        boolean first = true; // 标记是否是第一个点

        for (int i = 0; i < xValues.size(); i++) {
            // 获取数据点坐标
            float x = xValues.get(i);
            float y = yValues.get(i);

            // 将数据坐标转换为屏幕坐标
            float screenX = padding + ((x - minX) / (maxX - minX)) * graphWidth;
            float screenY = height - padding - ((y - minY) / (maxY - minY)) * graphHeight;

            // 绘制点
            canvas.drawPoint(screenX, screenY, pointPaint);

            // 绘制连线
            if (first) {
                path.moveTo(screenX, screenY);
                first = false;
            } else {
                path.lineTo(screenX, screenY);
            }
        }

        // 绘制连线
        canvas.drawPath(path, linePaint);
    }
}
