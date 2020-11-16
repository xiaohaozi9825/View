package pw.xiaohaozi.view;

import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import pw.xiaohaozi.view.R;


/**
 * 登录控件
 */
@SuppressLint("AppCompatCustomView")
public class LoginView extends TextView {
    private Paint mPaint;
    private Path mPath;
    private RectF mChangeRectF;
    private RectF mMaxRectF;
    private float mBorderWidth;
    private static final int STATE_DEFAULT = 0;//默认状态
    private static final int STATE_SHRINK = 1; //缩小状态
    private static final int STATE_LOADER = 2; //加载中
    private static final int STATE_ZOOM_IN = 3;//放大
    private int mState = STATE_DEFAULT;
    private CallBack mCallBack;
    private int mBorderColor = 0xffff0000;
    private int mContentColor = 0x46dbdbdb;

    private int startAngle, sweepAngle;
    private float downX, downY;


    public LoginView(Context context) {
        this(context, null);
    }

    public LoginView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoginView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBorderWidth = dp2px(4);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoginView);

            mBorderColor = typedArray.getColor(R.styleable.LoginView_loginBorderColor, mBorderColor);
            mBorderWidth = typedArray.getDimension(R.styleable.LoginView_loginBorderWidth, mBorderWidth);

            mContentColor = typedArray.getColor(R.styleable.LoginView_loginContentColor, mContentColor);
            typedArray.recycle();
        }
        init();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public LoginView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        mBorderWidth = dp2px(4);
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.LoginView);

            mBorderColor = typedArray.getColor(R.styleable.LoginView_loginBorderColor, mBorderColor);
            mBorderWidth = typedArray.getDimension(R.styleable.LoginView_loginBorderWidth, mBorderWidth);

            mContentColor = typedArray.getColor(R.styleable.LoginView_loginContentColor, mContentColor);
            typedArray.recycle();
        }
        init();
    }

    private void init() {
        //抗锯齿画笔
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
//        mPaint.setStrokeCap(Paint.Cap.ROUND);//线条是圆角的
        //防止边缘锯齿
        mPaint.setAntiAlias(true);
        mPaint.setColor(mBorderColor);
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setStrokeWidth(mBorderWidth);
        mPath = new Path();
        //需要重写onDraw就得调用此
        this.setWillNotDraw(false);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mChangeRectF = new RectF(
                mBorderWidth / 2 + getPaddingLeft(),
                mBorderWidth / 2 + getPaddingTop(),
                w - mBorderWidth / 2 - getPaddingRight(),
                h - mBorderWidth / 2 - getPaddingBottom());
        mMaxRectF = new RectF(mChangeRectF);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mPath.reset();
        switch (mState) {
            case STATE_DEFAULT:
                drawDefault(canvas);
                super.onDraw(canvas);
                break;
            case STATE_SHRINK:
            case STATE_ZOOM_IN:
                drawZoom(canvas);
                break;
            case STATE_LOADER:
                drawLoader(canvas);
                break;
            default:
                break;
        }


    }

    /**
     * 画公共部分
     *
     * @param canvas
     */
    private void drawPublic(Canvas canvas) {
        mPaint.setColor(mBorderColor);
        mPaint.setStyle(Paint.Style.STROKE);
        canvas.drawPath(mPath, mPaint);
    }

    /**
     * 画默认状态
     *
     * @param canvas
     */
    private void drawDefault(Canvas canvas) {
        mPaint.setColor(mBorderColor);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        //左半圆
        RectF rectFL = new RectF(mChangeRectF.left, mChangeRectF.top, mChangeRectF.left + mChangeRectF.height(), mChangeRectF.bottom);
        mPath.addArc(rectFL, 90, 180);
        //上横线,下一个半圆用arcTo来画，可以不用画上横线了
        //  mPath.lineTo(showWidth - showHeight / 2f + 0.5f, mChangeRectF.top);

        //右半圆
        RectF rectFR = new RectF(mChangeRectF.right - mChangeRectF.height(), mChangeRectF.top, mChangeRectF.right, mChangeRectF.bottom);
        mPath.arcTo(rectFR, 270, 180);
        //下横线
        mPath.lineTo(rectFL.centerX(), rectFL.bottom);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawPath(mPath, mPaint);

        drawPublic(canvas);
    }

    /**
     * 缩放状态动画
     *
     * @param canvas
     */
    private void drawZoom(Canvas canvas) {
        //左半圆
        RectF rectFL = new RectF(mChangeRectF.left, mChangeRectF.top, mChangeRectF.left + mChangeRectF.height(), mChangeRectF.bottom);
        mPath.addArc(rectFL, 90, 180);
        //上横线,下一个半圆用arcTo来画，可以不用画上横线了
        //  mPath.lineTo(showWidth - showHeight / 2f + 0.5f, mChangeRectF.top);

        //右半圆
        RectF rectFR = new RectF(mChangeRectF.right - mChangeRectF.height(), mChangeRectF.top, mChangeRectF.right, mChangeRectF.bottom);
        mPath.arcTo(rectFR, 270, 180);
        //下横线
        mPath.lineTo(rectFL.centerX(), rectFL.bottom);

        //中间填充
        mPaint.setColor(mContentColor);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawPath(mPath, mPaint);
        drawPublic(canvas);
    }

    /**
     * 绘制加载时动画
     *
     * @param canvas
     */
    private void drawLoader(Canvas canvas) {
        RectF rectFL = new RectF(mChangeRectF.left, mChangeRectF.top, mChangeRectF.left + mChangeRectF.height(), mChangeRectF.bottom);
        mPath.addArc(rectFL, startAngle, sweepAngle);
        mPaint.setColor(mContentColor);
        canvas.drawCircle(mChangeRectF.centerX(), mChangeRectF.centerY(), mChangeRectF.width() / 2, mPaint);// 圆点
        mPaint.setColor(mContentColor);
        mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
        canvas.drawCircle(mChangeRectF.centerX(), mChangeRectF.centerY(), mChangeRectF.width() / 2 + mBorderWidth, mPaint);// 圆点
        mPaint.setColor(mBorderColor);
        mPaint.setStyle(Paint.Style.STROKE);
        drawPublic(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        Log.i("cao", "onTouchEvent: "+event.getAction());
        if (!isEnabled()) {
            return true;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mState = STATE_SHRINK;
                invalidate();
                downX = event.getX();
                downY = event.getY();
                break;

            case MotionEvent.ACTION_UP:
                if (Math.abs(downX - event.getX()) > 15 || Math.abs(downY - event.getY()) > 15) {
                    mState = STATE_DEFAULT;
                    invalidate();
                    return super.onTouchEvent(event);
                }
                shrink();
                setEnabled(false);
                performClick();
                break;
            case MotionEvent.ACTION_CANCEL:
                mState = STATE_DEFAULT;
                invalidate();
                break;
        }
        return super.onTouchEvent(event);
    }

    /**
     * 缩小动画
     */
    private void shrink() {
        int start = 0;
        final int end = (int) ((mChangeRectF.width() - mChangeRectF.height()) / 2);
        long animTime = 500;
        mState = STATE_SHRINK;
        final float left = mChangeRectF.left;
        final float right = mChangeRectF.right;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(start, end);
        valueAnimator.setDuration(animTime);
//        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);//动画循环次数（无限循环）
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = Integer.valueOf(String.valueOf(animation.getAnimatedValue()));
                mChangeRectF.left = left + value;
                mChangeRectF.right = right - value;
                invalidate();
                if (value == end) {
                    loader();
                }
            }
        });
        valueAnimator.start();
    }

    /**
     * 放大动画
     */
    private void zoomIn() {
        mState = STATE_ZOOM_IN;
        int start = 0;
        final int end = (int) ((mMaxRectF.width() - mMaxRectF.height()) / 2);
        long animTime = 500;
        final float left = mChangeRectF.left;
        final float right = mChangeRectF.right;
        ValueAnimator valueAnimator = ValueAnimator.ofInt(start, end);
        valueAnimator.setDuration(animTime);
//        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);//动画循环次数（无限循环）
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = Integer.valueOf(String.valueOf(animation.getAnimatedValue()));
                mChangeRectF.left = left - value;
                mChangeRectF.right = right + value;
                if (value == end) {
                    setEnabled(true);
                    mState = STATE_DEFAULT;
                    if (mCallBack != null) {
                        mCallBack.call();
                        mCallBack = null;
                    }
                }
                invalidate();
            }
        });
        valueAnimator.start();
    }

    /**
     * 加载动画
     */
    private void loader() {
        mState = STATE_LOADER;
        final ValueAnimator valueAnimator = ValueAnimator.ofInt(0, 360);
        valueAnimator.setDuration(1000);
        valueAnimator.setRepeatCount(ValueAnimator.INFINITE);//动画循环次数（无限循环）
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                int value = Integer.valueOf(String.valueOf(animation.getAnimatedValue()));
                if (mCallBack != null && value >= 358) {
                    valueAnimator.cancel();
                    zoomIn();
                    return;
                }
                startAngle = value;
                double radians = Math.PI / 180 * value;  //角度转弧度
                sweepAngle = (int) (-Math.abs(90 * Math.sin(radians / 2)) - 45);
                invalidate();
            }
        });
        valueAnimator.start();
    }

    /**
     * 设置边框颜色
     *
     * @param borderColor
     */
    public void setBorderColor(int borderColor) {
        mBorderColor = borderColor;
        invalidate();
    }

    /**
     * 设置填充颜色
     *
     * @param contentColor
     */
    public void setContentColor(int contentColor) {
        mContentColor = contentColor;
        invalidate();
    }

    /**
     * 设置边框宽度
     *
     * @param borderWidth
     */
    public void setBorderWidth(int borderWidth) {
        mBorderWidth = borderWidth;
        invalidate();
    }

    /**
     * 数据加载完成调用
     *
     * @param callBack 监听放大动画是否完成
     */
    public void loadComplete(CallBack callBack) {
        mCallBack = callBack;

    }

    public interface CallBack {
        /**
         * 放大动画执行完成回调
         */
        void call();
    }

    /**
     * 将dp转换为与之相等的px
     */
    private int dp2px(float dipValue) {
        float scale = getContext().getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5f);
    }
}
