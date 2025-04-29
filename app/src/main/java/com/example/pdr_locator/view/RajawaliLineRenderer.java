package com.example.pdr_locator.view;

/**
 * @Author: Bai Ruiqi
 * @Date: 2025/4/16
 * @Time: 14:26
 */
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.view.MotionEvent;
import org.rajawali3d.Object3D;
import org.rajawali3d.lights.DirectionalLight;
import org.rajawali3d.materials.Material;
import org.rajawali3d.math.vector.Vector3;
import org.rajawali3d.primitives.Line3D;
import org.rajawali3d.renderer.Renderer;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.rajawali3d.materials.textures.ATexture;
import org.rajawali3d.materials.textures.Texture;
import org.rajawali3d.primitives.Plane;

/**
 * 用于渲染3D线条的Rajawali渲染器
 */
public class RajawaliLineRenderer extends Renderer {
    private DirectionalLight directionalLight; // 方向光
    private Stack<Object3D> lineSegments = new Stack<>(); // 线段堆栈
    private List<Vector3> points = new ArrayList<>(); // 点列表
    private Material lineMaterial; // 线条材质
    private float lineThickness = 3.0f; // 线条粗细
    public boolean isAutoRotate = true; // 是否自动旋转
    private float lastTouchX, lastTouchY; // 上次触摸位置
    private float mPreviousScale = 1.0f; // 上次的缩放比例
    private float mScaleFactor = 1.0f; // 当前缩放因子
    private static final float MIN_SCALE = 0.2f; // 最小缩放比例
    private static final float MAX_SCALE = 3.0f; // 最大缩放比例
    private Vector3 mOriginOffset = new Vector3(0, 0, 0); // 原点偏移量
    private boolean mIsDragging = false; // 是否正在拖动
    private static final float DRAG_SENSITIVITY = 0.02f; // 拖动灵敏度


    /**
     * 构造函数
     *
     * @param context 应用程序上下文
     */
    public RajawaliLineRenderer(Context context) {
        super(context);
        setFrameRate(60);
    }

    /**
     * 初始化场景
     */
    @Override
    protected void initScene() {
        getCurrentScene().setBackgroundColor(Color.WHITE); // 设置场景背景颜色为白色

        // 创建一个方向光，设置光的方向和强度
        directionalLight = new DirectionalLight(1, -1, -1);
        directionalLight.setColor(1, 1, 1); // 设置光的颜色为白色
        directionalLight.setPower(1.5f); // 设置光的强度
        getCurrentScene().addLight(directionalLight); // 将光源添加到场景中

        createCoordinateAxes();

        // 创建线条的材质，设置为蓝色
        lineMaterial = new Material();
        lineMaterial.setColor(Color.BLUE);
    }

    /**
     * 添加坐标轴标签
     *
     * @param text 标签文本
     * @param x x坐标
     * @param y y坐标
     * @param z z坐标
     * @param color 标签颜色
     */
    private void addAxisLabel(String text, float x, float y, float z, int color) {
        try {
            // 创建文字Bitmap
            Bitmap bitmap = Bitmap.createBitmap(128, 128, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint();
            paint.setColor(color);
            paint.setTextSize(48);
            paint.setTypeface(Typeface.DEFAULT_BOLD);
            paint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(text, 64, 64, paint); // 在Bitmap上绘制文字

            // 创建材质
            Material material = new Material();
            material.setColorInfluence(0);
            Texture texture = new Texture("LabelTexture", bitmap);
            material.addTexture(texture);

            // 创建平面显示文字
            Plane labelPlane = new Plane(1, 1, 1, 1);
            labelPlane.setMaterial(material);
            labelPlane.setPosition(x, y, z);
            labelPlane.setLookAt(0, 0, 0); // 始终面向原点
            labelPlane.setTransparent(true);

            getCurrentScene().addChild(labelPlane); // 将标签平面添加到场景中
        } catch (ATexture.TextureException e) {
            e.printStackTrace();
        }
    }

    /**
     * 创建坐标轴
     */
    private void createCoordinateAxes() {
        float axisLength = 6;
        float axisThickness = 2.5f;
        float arrowSize = 1.0f; // 箭头大小
        float labelOffset = 1.5f;

        // 创建坐标轴点集 - 交换Y和Z轴
        Stack<Vector3> xAxisPoints = new Stack<>();
        xAxisPoints.add(new Vector3(-axisLength, 0, 0));
        xAxisPoints.add(new Vector3(axisLength, 0, 0));

        Stack<Vector3> yAxisPoints = new Stack<>();
        yAxisPoints.add(new Vector3(0, 0, -axisLength));  // 原来的Z轴
        yAxisPoints.add(new Vector3(0, 0, axisLength));

        Stack<Vector3> zAxisPoints = new Stack<>();
        zAxisPoints.add(new Vector3(0, -axisLength, 0));  // 原来的Y轴
        zAxisPoints.add(new Vector3(0, axisLength, 0));

        // X轴 (红色)
        Line3D xAxis = new Line3D(xAxisPoints, axisThickness, Color.RED);
        xAxis.setMaterial(new Material());
        getCurrentScene().addChild(xAxis);

        // X轴箭头
        Stack<Vector3> xArrowPoints = new Stack<>();
        xArrowPoints.add(new Vector3(axisLength, 0, 0));
        xArrowPoints.add(new Vector3(axisLength - arrowSize, 0, arrowSize));  // 交换Y和Z
        xArrowPoints.add(new Vector3(axisLength, 0, 0));
        xArrowPoints.add(new Vector3(axisLength - arrowSize, 0, -arrowSize)); // 交换Y和Z
        xArrowPoints.add(new Vector3(axisLength, 0, 0));
        xArrowPoints.add(new Vector3(axisLength - arrowSize, arrowSize, 0));  // 交换Y和Z
        Line3D xArrow = new Line3D(xArrowPoints, axisThickness, Color.RED);
        xArrow.setMaterial(new Material());
        getCurrentScene().addChild(xArrow);

        // Y轴 (绿色) - 现在是原来的Z轴
        Line3D yAxis = new Line3D(yAxisPoints, axisThickness, Color.GREEN);
        yAxis.setMaterial(new Material());
        getCurrentScene().addChild(yAxis);

        // Y轴箭头
        Stack<Vector3> yArrowPoints = new Stack<>();
        yArrowPoints.add(new Vector3(0, 0, axisLength));
        yArrowPoints.add(new Vector3(arrowSize, 0, axisLength - arrowSize));
        yArrowPoints.add(new Vector3(0, 0, axisLength));
        yArrowPoints.add(new Vector3(-arrowSize, 0, axisLength - arrowSize));
        yArrowPoints.add(new Vector3(0, 0, axisLength));
        yArrowPoints.add(new Vector3(0, arrowSize, axisLength - arrowSize));
        Line3D yArrow = new Line3D(yArrowPoints, axisThickness, Color.GREEN);
        yArrow.setMaterial(new Material());
        getCurrentScene().addChild(yArrow);

        // Z轴 (蓝色) - 现在是原来的Y轴
        Line3D zAxis = new Line3D(zAxisPoints, axisThickness, Color.BLUE);
        zAxis.setMaterial(new Material());
        getCurrentScene().addChild(zAxis);

        // Z轴箭头
        Stack<Vector3> zArrowPoints = new Stack<>();
        zArrowPoints.add(new Vector3(0, axisLength, 0));
        zArrowPoints.add(new Vector3(0, axisLength - arrowSize, arrowSize));
        zArrowPoints.add(new Vector3(0, axisLength, 0));
        zArrowPoints.add(new Vector3(0, axisLength - arrowSize, -arrowSize));
        zArrowPoints.add(new Vector3(0, axisLength, 0));
        zArrowPoints.add(new Vector3(arrowSize, axisLength - arrowSize, 0));
        Line3D zArrow = new Line3D(zArrowPoints, axisThickness, Color.BLUE);
        zArrow.setMaterial(new Material());
        getCurrentScene().addChild(zArrow);

        // 更新标签位置 - 交换Y和Z
        addAxisLabel("X", axisLength - labelOffset, 0, 0, Color.RED);
        addAxisLabel("Y", 0, 0, axisLength - labelOffset, Color.GREEN);  // 原来的Z标签
        addAxisLabel("Z", 0, axisLength - labelOffset, 0, Color.BLUE);   // 原来的Y标签
    }

    /**
     * 添加一个点到3D图形中
     *
     * @param x x坐标
     * @param y y坐标
     * @param z z坐标
     */
    public void addPoint(float x, float y, float z) {
        // 交换Y和Z坐标
        Vector3 newPoint = new Vector3(x, z, y);  // 注意这里交换了y和z
        points.add(newPoint);

        if (points.size() > 1) {
            // 创建线段点集
            Stack<Vector3> segmentPoints = new Stack<>();
            segmentPoints.add(points.get(points.size() - 2));
            segmentPoints.add(newPoint);

            // 创建线段
            Line3D segment = new Line3D(
                    segmentPoints,
                    lineThickness,
                    getColorForDepth(y)  // 使用原来的Y坐标作为深度
            );
            segment.setMaterial(lineMaterial);
            getCurrentScene().addChild(segment);
            lineSegments.push(segment);
        }
    }

    /**
     * 根据深度获取颜色
     *
     * @param y 深度值
     * @return int 计算出的颜色值
     */
    private int getColorForDepth(float y) {  // 参数名改为y，因为现在深度基于Y坐标
        float normalizedY = (y + 10) / 20;
        int r = (int) (255 * normalizedY);
        int b = (int) (255 * (1 - normalizedY));
        return Color.rgb(r, 100, b);
    }

    /**
     * 清除所有点
     */
    public void clearPoints() {
        while (!lineSegments.isEmpty()) {
            getCurrentScene().removeChild(lineSegments.pop());
        }
        points.clear();
    }

    /**
     * 渲染循环
     *
     * @param elapsedTime 经过的时间
     * @param deltaTime 时间增量
     */
    @Override
    public void onRender(long elapsedTime, double deltaTime) {
        super.onRender(elapsedTime, deltaTime);

        if (isAutoRotate) {
            double time = System.currentTimeMillis() / 1000.0;
            getCurrentCamera().setPosition(
                    15 * Math.sin(time * 0.5),
                    8,
                    15 * Math.cos(time * 0.5)
            );
            getCurrentCamera().setLookAt(0, 0, 0);
        }
    }

    /**
     * 壁纸偏移变化处理
     */
    @Override
    public void onOffsetsChanged(float xOffset, float yOffset,
                                 float xOffsetStep, float yOffsetStep,
                                 int xPixelOffset, int yPixelOffset) {

    }

    /**
     * 处理触摸事件
     *
     * @param event 触摸事件
     */
    @Override
    public void onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                lastTouchX = x;
                lastTouchY = y;
                isAutoRotate = false; // 手动触摸时停止自动旋转
                break;

            case MotionEvent.ACTION_POINTER_DOWN:
                if (event.getPointerCount() >= 2) {
                    float x1 = event.getX(0);
                    float y1 = event.getY(0);
                    float x2 = event.getX(1);
                    float y2 = event.getY(1);
                    mPreviousScale = (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));
                }
                break;

            case MotionEvent.ACTION_MOVE:
                if (event.getPointerCount() >= 2) {
                    // 处理缩放逻辑
                    float x1 = event.getX(0);
                    float y1 = event.getY(0);
                    float x2 = event.getX(1);
                    float y2 = event.getY(1);
                    float currentScale = (float) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2));

                    if (mPreviousScale > 0) {
                        float scale = currentScale / mPreviousScale;
                        mScaleFactor *= scale;
                        mScaleFactor = Math.max(MIN_SCALE, Math.min(mScaleFactor, MAX_SCALE));

                        getCurrentCamera().setFieldOfView(60.0 / mScaleFactor);
                    }
                    mPreviousScale = currentScale;
                } else {
                    // 处理旋转逻辑
                    float dx = x - lastTouchX;
                    float dy = y - lastTouchY;

                    double angle = Math.sqrt(dx * dx + dy * dy) * 0.5;
                    Vector3 axis = new Vector3(dy, dx, 0);

                    getCurrentCamera().rotateAround(
                            Vector3.ZERO,
                            angle,
                            true
                    );

                    lastTouchX = x;
                    lastTouchY = y;
                }
                break;

            case MotionEvent.ACTION_POINTER_UP:
                if (event.getPointerCount() == 2) {
                    mPreviousScale = -1.0f;
                }
                break;

            // 移除 ACTION_UP 和 ACTION_CANCEL 的自动恢复旋转逻辑
            // 这样手动操作后不会自动恢复旋转
        }
    }

    /**
     * 切换自动旋转状态
     */
    public void notIsAutoRotate() {
        isAutoRotate = !isAutoRotate; // 直接切换状态
    }
}