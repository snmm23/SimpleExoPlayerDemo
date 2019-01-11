package sun.bo.lin.exoplayer.util;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import sun.bo.lin.exoplayer.R;


/**
 * 自定义LoadingView
 */

public class MyULCLoadingView extends View {

    /**
     * 总共要旋转的角度
     */
    private int maxRotate = 720;

    /**
     * 边框的粗细
     */
    private int borderWidth;

    /**
     * 边框的颜色
     */
    private int borderColor;

    /**
     * 边框的最大长度，这个是划过的角度
     */
    private int maxAngle;

    /**
     * 画笔
     */
    private Paint paint;

    /**
     * 绘制的区域
     */
    private RectF contentRectF;

    /**
     * 旋转动画
     */
    private ValueAnimator valueAnimator;

    /**
     * 动画时长
     */
    private int duration;

    /**
     * 偏移的角度
     */
    private int offsetAngle;

    /**
     * 进度划过的角度
     */
    private float progressAngle;

    public MyULCLoadingView(Context context) {
        this(context, null);
    }

    public MyULCLoadingView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MyULCLoadingView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        contentRectF = new RectF();
        // 获取自定义属性的值
        initProperties(context, attrs);
        // 初始化画笔
        initPaint();
    }

    /**
     * 初始化自定义属性
     *
     * @param context 上下文
     * @param attrs   属性集合
     */
    private void initProperties(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.MyULCLoadingView);
        borderWidth = typedArray.getDimensionPixelOffset(R.styleable.MyULCLoadingView_borderWidth,
                context.getResources().getDimensionPixelOffset(R.dimen.default_loading_border_width));
        borderColor = typedArray.getColor(R.styleable.MyULCLoadingView_borderColor,
                Color.BLACK);
        maxAngle = typedArray.getInt(R.styleable.MyULCLoadingView_maxAngle, 100);
        duration = typedArray.getInt(R.styleable.MyULCLoadingView_duration, 2000);
        typedArray.recycle();
    }

    /**
     * 初始化画笔
     */
    private void initPaint() {
        paint = new Paint();
        // 去除锯齿
        paint.setAntiAlias(true);
        paint.setDither(true);
        // 画笔颜色
        paint.setColor(borderColor);
        // 设置画笔样式为边框
        paint.setStyle(Paint.Style.STROKE);
        // 设置画笔的宽度
        paint.setStrokeWidth(borderWidth);
        // 设置画笔的两端样式，这里是圆滑
        paint.setStrokeCap(Paint.Cap.ROUND);
    }

    /**
     * 初始化旋转动画
     */
    private void initValueAnimator() {
        if (valueAnimator != null) {
            return;
        }
        valueAnimator = ValueAnimator.ofFloat(0, maxRotate);
        valueAnimator.setDuration(duration);
        valueAnimator.setInterpolator(new AccelerateDecelerateInterpolator());
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float value = (float) valueAnimator.getAnimatedValue();
                // 计算偏移值
                offsetAngle = (int) (-90 + value);
                // 计算进度条的宽度变化
                if (value <= maxRotate / 2) {
                    progressAngle = (int) (maxAngle * (valueAnimator.getAnimatedFraction() * 2));
                } else {
                    progressAngle = (int) (maxAngle * (2 - valueAnimator.getAnimatedFraction() * 2));
                }
                if (progressAngle == 0) {
                    progressAngle = 0.01f;
                }
                invalidate();
            }

        });
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = getMeasuredWidth();
        int height = getMeasuredHeight();
        // 计算半径
        int radius = width > height ? height / 2 : width / 2;
        // 计算要绘制的区域
        contentRectF.set(width / 2 - radius + borderWidth / 2, height / 2 - radius + borderWidth / 2,
                width / 2 + radius - borderWidth / 2, height / 2 + radius - borderWidth / 2);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // 这里要画圆弧
        canvas.drawArc(contentRectF, offsetAngle, progressAngle, false, paint);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initValueAnimator();
        // 开始旋转动画
        if (!valueAnimator.isStarted()) {
            valueAnimator.start();
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        // 停止旋转动画
        if (valueAnimator.isStarted()) {
            valueAnimator.cancel();
            valueAnimator = null;
        }
    }
}
