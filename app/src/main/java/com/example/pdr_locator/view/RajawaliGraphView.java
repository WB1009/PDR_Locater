package com.example.pdr_locator.view;

/**
 * @Author: Bai Ruiqi
 * @Date: 2025/4/16
 * @Time: 15:39
 */
import android.content.Context;
import android.util.AttributeSet;
import org.rajawali3d.view.SurfaceView;
import android.view.MotionEvent;
import com.example.pdr_locator.view.RajawaliLineRenderer;

/**
 * 使用Rajawali显示3D图形的自定义SurfaceView
 */
public class RajawaliGraphView extends SurfaceView {
    private RajawaliLineRenderer renderer; // 用于3D线条图形的渲染器

    /**
     * 仅包含context的构造函数
     *
     * @param context 应用程序的上下文
     */
    public RajawaliGraphView(Context context) {
        super(context);
        init(context);
    }

    /**
     * 包含context和属性集的构造函数
     *
     * @param context 应用程序的上下文
     * @param attrs 属性集
     */
    public RajawaliGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
        setClickable(true);
    }

    /**
     * 初始化视图，设置抗锯齿和渲染器
     *
     * @param context 应用程序的上下文
     */
    private void init(Context context) {
        // 设置抗锯齿
        setAntiAliasingMode(ANTI_ALIASING_CONFIG.MULTISAMPLING);

        // 创建渲染器
        renderer = new RajawaliLineRenderer(context);
        setSurfaceRenderer(renderer);

        // 设置帧率
        setFrameRate(60.0);
    }

    /**
     * 向3D图形添加一个点
     *
     * @param x 点的x坐标
     * @param y 点的y坐标
     * @param z 点的z坐标
     */
    public void addPoint(float x, float y, float z) {
        if (renderer != null) {
            renderer.addPoint(x, y, z);
        }
    }

    /**
     * 清除图形中的所有点
     */
    public void clearPoints() {
        if (renderer != null) {
            renderer.clearPoints();
        }
    }

    /**
     * 切换摄像头的自动旋转状态
     */
    public void notIsAutoRotate() {
        renderer.notIsAutoRotate();
    }

    /**
     * 处理触摸事件以控制摄像头
     *
     * @param event 触摸事件
     * @return boolean 是否处理了事件
     */
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (renderer != null) {
            renderer.onTouchEvent(event);
            return true; // 确保事件被消费
        }
        return super.onTouchEvent(event);
    }

    /**
     * 拦截触摸事件
     *
     * @param event 触摸事件
     * @return boolean 是否拦截事件
     */
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return true;
    }
}