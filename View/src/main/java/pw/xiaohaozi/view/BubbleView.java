package pw.xiaohaozi.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

import static pw.xiaohaozi.view.BubbleView.IndicatorDirection.BOTTOM;
import static pw.xiaohaozi.view.BubbleView.IndicatorDirection.TOP;
import static pw.xiaohaozi.view.BubbleView.IndicatorDirection.LEFT;
import static pw.xiaohaozi.view.BubbleView.IndicatorDirection.RIGHT;


/**
 * 描述：聊天气泡
 * 作者：小耗子
 * 简书地址：https://www.jianshu.com/u/2a2ea7b43087
 * github：https://github.com/xiaohaozi9825
 * 创建时间：2020/5/23 0023 14:16
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BubbleView extends ViewGroup {
    private static final String TAG = "BubbleView";
    private int mShadowColor;//阴影颜色
    private int mElevation = 0;//Z轴方向高度
    private int mBubbleColor = 0xffffffff;//气泡颜色，默认白色
    private int mIndicatorHeight = dp2px(8);//三角形指示器高度，默认8dp
    private int mIndicatorWidth = dp2px(8);//三角形指示器宽度，默认8dp
    private int mRadius = dp2px(8);//圆角角度
    private IndicatorDirection mIndicatorDirection = BOTTOM;//箭头方向
    private boolean isFillIndicator = false;//子控件是否填充到指示器上
    private DrawIndicator mDrawIndicator = new DrawTrilateralIndicator();

    private int mBubbleIndicatorLocationType;//位置属性类别
    private float location_f;//浮点型
    private float location_d;//尺寸值
    private int location_e;//枚举值

    private Path mPath;
    private Paint mPaint;
    private int w;
    private int h;


    public BubbleView(Context context) {
        this(context, null);
    }

    public BubbleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public BubbleView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public BubbleView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        setBackgroundColor(0x00000000);
        getTypedArray(context, attrs);
        mPath = new Path();
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setColor(mBubbleColor);
        if (mElevation > 0)
            setLayerType(LAYER_TYPE_SOFTWARE, null);

    }

    /**
     * 获取属性值
     *
     * @param context
     * @param attrs
     */
    private void getTypedArray(Context context, AttributeSet attrs) {
        TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.BubbleView);
        mBubbleColor = typedArray.getColor(R.styleable.BubbleView_bubbleColor, 0xffffffff);
        int indicatorDirection = typedArray.getInteger(R.styleable.BubbleView_bubbleIndicatorDirection, 0);
        switch (indicatorDirection) {
//            case 0:
//                mIndicatorDirection = LEFT;
//                break;
            case 1:
                mIndicatorDirection = TOP;
                break;
            case 2:
                mIndicatorDirection = RIGHT;
                break;
            case 3:
                mIndicatorDirection = BOTTOM;
                break;
            default:
                mIndicatorDirection = LEFT;
                break;
        }
        isFillIndicator = typedArray.getBoolean(R.styleable.BubbleView_bubbleFillIndicator, false);
        mRadius = (int) typedArray.getDimension(R.styleable.BubbleView_bubbleRadius, dp2px(8));
        mIndicatorHeight = (int) typedArray.getDimension(R.styleable.BubbleView_bubbleIndicatorHeight, dp2px(8));
        mIndicatorWidth = (int) typedArray.getDimension(R.styleable.BubbleView_bubbleIndicatorWidth, dp2px(8));
        mElevation = (int) typedArray.getDimension(R.styleable.BubbleView_bubbleElevation, 0);
        mShadowColor = typedArray.getColor(R.styleable.BubbleView_bubbleShadowColor, 0xff888888);


        mBubbleIndicatorLocationType = typedArray.getType(R.styleable.BubbleView_bubbleIndicatorLocation);
        //枚举 int  16
        //浮点 4
        //尺寸值 5
        switch (mBubbleIndicatorLocationType) {
            case TypedValue.TYPE_FLOAT://浮点型
                location_f = typedArray.getFloat(R.styleable.BubbleView_bubbleIndicatorLocation, 0.5f);
                if (location_f < 0) location_f = 0;
                else if (location_f > 1) location_f = 1;
//                Log.i(TAG, "BubbleView: type = " + mBubbleIndicatorLocationType + " === " + location_f);
                break;
            case TypedValue.TYPE_DIMENSION://尺寸值
                location_d = typedArray.getDimension(R.styleable.BubbleView_bubbleIndicatorLocation, -1);
//                Log.i(TAG, "BubbleView: type = " + mBubbleIndicatorLocationType + " === " + location_d);
                break;
            case 16://枚举
                location_e = typedArray.getInt(R.styleable.BubbleView_bubbleIndicatorLocation, -1);
//                Log.i(TAG, "BubbleView: type = " + mBubbleIndicatorLocationType + " === " + location_e);
                break;
            default:
                break;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //①获取控件的宽高模式和宽高值
        // MeasureSpec.AT_MOST; 至多模式, 控件有多大显示多大, wrap_content
        // MeasureSpec.EXACTLY; 确定模式, 类似宽高写死成dip, match_parent
        // MeasureSpec.UNSPECIFIED; 未指定模式.
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);//获取宽模式
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);//获取高模式
        int width = MeasureSpec.getSize(widthMeasureSpec);//获取宽度值
        int height = MeasureSpec.getSize(heightMeasureSpec);//获取高度值
        Log.i(TAG, "onMeasure: width = " + width + "  height = " + height);
        Log.i(TAG, "onMeasure: widthMode = " + toMode(widthMode) + "  heightMode = " + toMode(heightMode));
        int childWidth = width;
        int childHeight = height;
//        // ②根据获取到的宽高模式和宽高值重新生成新的数据
        if (!isFillIndicator) {
            switch (mIndicatorDirection) {
                case LEFT:
                case RIGHT:
                    childWidth = childWidth - mIndicatorHeight;
                    break;
                case TOP:
                case BOTTOM:
                    childHeight = childHeight - mIndicatorHeight;
                    break;
                default:
                    break;
            }


        }
//        // ③根据最新的高度来重新生成heightMeasureSpec(高度模式是确定模式)
        int childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, widthMode);
        int childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, heightMode);
        measureChildren(childWidthMeasureSpec, childHeightMeasureSpec);//测量所有子控件
        if (widthMode != MeasureSpec.EXACTLY) {
            int cw = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                cw = Math.max(childAt.getMeasuredWidth(), cw);
            }
            if ((mIndicatorDirection == IndicatorDirection.LEFT || mIndicatorDirection == IndicatorDirection.RIGHT) && !isFillIndicator)
                width = (int) (cw + getPaddingLeft() + getPaddingRight() + mIndicatorHeight + 1.5 * mElevation);
            else width = (int) (cw + getPaddingLeft() + getPaddingRight() + 1.5 * mElevation);
            if (widthMode == MeasureSpec.UNSPECIFIED) widthMode = MeasureSpec.AT_MOST;
        }
        if (heightMode != MeasureSpec.EXACTLY) {
            int ch = 0;
            for (int i = 0; i < getChildCount(); i++) {
                View childAt = getChildAt(i);
                ch = Math.max(childAt.getMeasuredHeight(), ch);
            }
            if ((mIndicatorDirection == TOP || mIndicatorDirection == BOTTOM) && !isFillIndicator)
                height = (int) (ch + getPaddingTop() + getPaddingBottom() + mIndicatorHeight + 1.5 * mElevation);
            else height = (int) (ch + getPaddingTop() + getPaddingBottom() + 1.5 * mElevation);
            if (heightMode == MeasureSpec.UNSPECIFIED) heightMode = MeasureSpec.AT_MOST;
        }
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, widthMode);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, heightMode);
        // ④按照最新的宽高测量控件
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    private String toMode(int mode) {
//        Log.i("自定义控件", " MeasureSpec.EXACTLY = " + MeasureSpec.EXACTLY +
//                "  MeasureSpec.AT_MOST = " + MeasureSpec.AT_MOST +
//                "  MeasureSpec.UNSPECIFIED = " + MeasureSpec.UNSPECIFIED);
        switch (mode) {
            case 0:
                return "UNSPECIFIED   ";
            case 1073741824:
                return "EXACTLY   ";
            case -2147483648:
                return "AT_MOST   ";
            default:
                return "未知   ";
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        Log.i(TAG, "onSizeChanged: w = " + w + "  h = " + h);
        this.w = w;
        this.h = h;
        setPath();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    //此处的左上右下是该空间在父容器中的位置
    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        if (!isFillIndicator) {
            switch (mIndicatorDirection) {
                case LEFT:
                    layout_(mIndicatorHeight + mElevation / 2, mElevation / 2,
                            right - mElevation, bottom - mElevation);
                    break;
                case TOP:
                    layout_(mElevation / 2, mIndicatorHeight + mElevation / 2,
                            right - mElevation, bottom - mElevation);
                    break;
                case RIGHT:
                    layout_(mElevation / 2, mElevation / 2,
                            right - mElevation - mIndicatorHeight, bottom - mElevation);
                    break;
                case BOTTOM:
                    layout_(mElevation / 2, mElevation / 2,
                            right - mElevation, bottom - mElevation - mIndicatorHeight);
                    break;
                default:
                    break;
            }

            return;
        }
        layout_(mElevation / 2, mElevation / 2,
                right - mElevation, bottom - mElevation);
    }

    /**
     * 摆放子控件
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    private void layout_(int left, int top, int right, int bottom) {
        //此处的左上右下是该空间可显示区域的坐标
        for (int i = 0; i < getChildCount(); i++) {
            View childAt = getChildAt(i);
            int childWidth = childAt.getMeasuredWidth();
            int childHeight = childAt.getMeasuredHeight();
//            Log.i(TAG, "layout_: childWidth = " + childWidth + "  childHeight = " + childHeight);
            childAt.layout(left + getPaddingLeft(), top + getPaddingTop(),
                    left + childWidth + getPaddingLeft(),
                    top + childHeight + getPaddingTop());
        }
    }

    private void setPath() {
//        Log.i(TAG, "onSizeChanged: w = " + w + " h = " + h);
        mPath.reset();
        switch (mIndicatorDirection) {
            case TOP:
                bubbleTop();
                break;
            case LEFT:
                bubbleLeft();
                break;
            case RIGHT:
                bubbleRight();
                break;
            default:
                bubbleBottom();
                break;
        }
        mPath.close();
    }

    /**
     * 指示器（等腰三角形）底边中点位置
     *
     * @param length    圆角矩形与指示器相切的边的长度
     * @param elevation 圆角矩形相对view边界偏移量
     * @return
     */
    private float computeMiddle(double length, int elevation) {
        switch (mBubbleIndicatorLocationType) {
            case TypedValue.TYPE_FLOAT:
                return getMiddle(length, location_f, elevation);
            case TypedValue.TYPE_DIMENSION://尺寸值
                if (location_d == 0) {
                    return getMiddle(length, 0.5f, elevation);
                } else if (location_d > 0) {
                    return getMiddle(length, 0f, elevation) + location_d;
                }
                return getMiddle(length, 1f, elevation) + location_d;
            case 16://枚举
                return (location_e == 0) ?
                        getMiddle(length, 0.0f, elevation) :
                        (location_e == 2) ? getMiddle(length, 1.0f, elevation) : getMiddle(length, 0.5f, elevation);
        }
        return getMiddle(length, 0.5f, elevation);
    }

    /**
     * 根据比例获取中点位置
     *
     * @param length
     * @param ratio     比例
     * @param elevation
     * @return
     */
    private float getMiddle(double length, float ratio, int elevation) {
        return (float) ((length - mRadius * 2 - mIndicatorWidth) * ratio + mRadius + elevation + mIndicatorWidth / 2);
    }


    /**
     * 三角形指示器
     */
    private class DrawTrilateralIndicator implements DrawIndicator {
        @Override
        public void drawLeft(Path path, int left, int top, int right, int bottom) {
            path.moveTo(right, top);//右上
            path.lineTo(left, top + (bottom - top) / 2);//左中
            path.lineTo(right, bottom);//右下
        }

        @Override
        public void drawTop(Path path, int left, int top, int right, int bottom) {
            path.moveTo(left, bottom);//下左
            path.lineTo(left + (right - left) / 2, top);//上中
            path.lineTo(right, bottom);//下右
        }

        @Override
        public void drawRight(Path path, int left, int top, int right, int bottom) {
            path.moveTo(left, top);//左上
            path.lineTo(right, top + (bottom - top) / 2);//右中
            path.lineTo(left, bottom);//左下
        }

        @Override
        public void drawBottom(Path path, int left, int top, int right, int bottom) {
            path.moveTo(left, top);//上左
            path.lineTo(left + (right - left) / 2, bottom);//下中
            path.lineTo(right, top);//上右
        }
    }

    /**
     * 指示器在左边
     */

    private void bubbleLeft() {

        mPath.addRoundRect(mIndicatorHeight + mElevation / 2, mElevation / 2, w - mElevation,
                h - mElevation, mRadius, mRadius, Path.Direction.CW);

        float computeMiddle = computeMiddle(h - 1.5 * mElevation, mElevation / 2);
        mDrawIndicator.drawLeft(mPath, mElevation / 2, (int) (computeMiddle - mIndicatorWidth / 2)
                , mIndicatorHeight + mElevation / 2, (int) (computeMiddle + mIndicatorWidth / 2));
    }

    /**
     * 指示器在上边
     */
    private void bubbleTop() {
        mPath.addRoundRect(mElevation / 2, mIndicatorHeight + mElevation / 2, w - mElevation,
                h - mElevation, mRadius, mRadius, Path.Direction.CW);

        float computeMiddle = computeMiddle(w - 1.5 * mElevation, mElevation / 2);
        mDrawIndicator.drawTop(mPath, (int) (computeMiddle - mIndicatorWidth / 2), mElevation / 2,
                (int) (computeMiddle + mIndicatorWidth / 2),
                mIndicatorHeight + mElevation / 2);
    }

    /**
     * 指示器在右边
     */
    private void bubbleRight() {

        mPath.addRoundRect(mElevation / 2, mElevation / 2, w - mElevation - mIndicatorHeight,
                h - mElevation, mRadius, mRadius, Path.Direction.CW);

        float computeMiddle = computeMiddle(h - 1.5 * mElevation, mElevation / 2);
        mDrawIndicator.drawRight(mPath, w - mElevation - mIndicatorHeight, (int) (computeMiddle - mIndicatorWidth / 2),
                w - mElevation, (int) (computeMiddle + mIndicatorWidth / 2));
    }

    /**
     * 指示器在底部
     */
    private void bubbleBottom() {
        mPath.addRoundRect(mElevation / 2, mElevation / 2, w - mElevation,
                h - mElevation - mIndicatorHeight, mRadius, mRadius, Path.Direction.CW);

        float computeMiddle = computeMiddle(w - 1.5 * mElevation, mElevation / 2);
        mDrawIndicator.drawBottom(mPath, (int) (computeMiddle - mIndicatorWidth / 2), h - mElevation - mIndicatorHeight,
                (int) (computeMiddle + mIndicatorWidth / 2), h - mElevation);
    }


    private int dp2px(float dipValue) {
        float scale = getResources().getDisplayMetrics().density;
        return (int) (dipValue * scale + 0.5F);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        mPaint.setShadowLayer(mElevation / 2, mElevation / 8, mElevation / 4,
                mShadowColor & 0x00ffffff | 0x88000000);
        canvas.drawPath(mPath, mPaint);
        canvas.clipPath(mPath);
    }

/************************外界可以使用的方法*********************************/
    /**
     * 设置气泡颜色
     *
     * @param backgroundColor
     */
    public void setBubbleColor(int backgroundColor) {
        mBubbleColor = backgroundColor;
        mPaint.setColor(mBubbleColor);
        requestLayout();
    }

    /**
     * 设置空间Z轴方向高度
     *
     * @param elevation 单位：dp
     */
    public void setElevation(int elevation) {
        if (elevation < 0) elevation = 0;
        mElevation = dp2px(elevation);
        if (mElevation > 0)
            setLayerType(LAYER_TYPE_SOFTWARE, null);
        else setLayerType(LAYER_TYPE_NONE, null);
        setPath();
        requestLayout();
    }

    /**
     * 设置阴影颜色，elevation>0才会有效果
     *
     * @param shadowColor
     */
    public void setShadowColor(int shadowColor) {
        mShadowColor = shadowColor;
        setPath();
        requestLayout();
    }

    /**
     * 设置指示器高度，顶点到圆角矩形的距离
     *
     * @param indicatorHeight 单位：dp
     */
    public void setIndicatorHeight(int indicatorHeight) {
        mIndicatorHeight = dp2px(indicatorHeight);
        setPath();
        requestLayout();
    }

    /**
     * 设置指示器宽度，贴着圆角矩形的边长度
     *
     * @param indicatorWidth 单位：dp
     */
    public void setIndicatorWidth(int indicatorWidth) {
        mIndicatorWidth = dp2px(indicatorWidth);
        setPath();
        requestLayout();
    }

    /**
     * 设置圆角矩形的圆角半径
     *
     * @param radius 单位：dp
     */
    public void setRadius(int radius) {
        mRadius = dp2px(radius);
        setPath();
        requestLayout();
    }

    /**
     * 设置指示器方向，可以是 左，上，右，下
     *
     * @param indicatorDirection
     */
    public void setIndicatorDirection(IndicatorDirection indicatorDirection) {
        mIndicatorDirection = indicatorDirection;
        setPath();
        requestLayout();
    }

    /**
     * 子控件是否填充到指示器中
     *
     * @param fillIndicator
     */
    public void setFillIndicator(boolean fillIndicator) {
        isFillIndicator = fillIndicator;
        setPath();
        requestLayout();
    }

    /**
     * 设置指示器相对位置
     *
     * @param location 枚举值，可以是起始位置，中间位置，结束位置，按从左到右，从上到下算
     *                 默认中间位置
     */
    public void setIndicatorLocation(IndicatorLocation location) {
        mBubbleIndicatorLocationType = 16;
        location_e = location.getIndex();
        setPath();
        requestLayout();
    }

    /**
     * 设置指示器相对位置
     *
     * @param location 范围0-1f，0.5是中间，默认0.5
     */
    public void setIndicatorLocation(float location) {
        if (location < 0) location = 0;
        else if (location > 1) location = 1;

        mBubbleIndicatorLocationType = TypedValue.TYPE_FLOAT;
        location_f = location;
        setPath();
        requestLayout();
    }

    /**
     * 设置指示器相对位置
     *
     * @param location 0:中间，正数：从开始位置向中间偏移，负数：从结束位置中间偏移
     *                 单位：pd
     */
    public void setIndicatorLocation(int location) {
        mBubbleIndicatorLocationType = TypedValue.TYPE_DIMENSION;
        location_d = dp2px(location);
        setPath();
        requestLayout();
    }

    /**
     * 绘制指示器形状
     *
     * @param drawIndicator
     */
    public void setDrawIndicator(DrawIndicator drawIndicator) {
        mDrawIndicator = drawIndicator;
        setPath();
        requestLayout();
    }

    /**
     * 指示器方向
     */
    public enum IndicatorDirection {
        LEFT(0), TOP(1), RIGHT(2), BOTTOM(3);
        private int index;

        public int getIndex() {
            return index;
        }

        IndicatorDirection(int index) {
            this.index = index;
        }
    }

    /**
     * 指示器位置
     */
    public enum IndicatorLocation {
        START(0), CENTRE(1), END(2);
        int index;

        IndicatorLocation(int i) {
            index = i;
        }

        public int getIndex() {
            return index;
        }
    }

    /**
     * 绘制指示器
     */
    public interface DrawIndicator {
        /**
         * 指示器在左边
         *
         * @param path
         * @param left
         * @param top
         * @param right
         * @param bottom
         */
        void drawLeft(Path path, int left, int top, int right, int bottom);

        /**
         * 指示器在顶部
         *
         * @param path
         * @param left
         * @param top
         * @param right
         * @param bottom
         */
        void drawTop(Path path, int left, int top, int right, int bottom);

        /**
         * 指示器在右边
         *
         * @param path
         * @param left
         * @param top
         * @param right
         * @param bottom
         */
        void drawRight(Path path, int left, int top, int right, int bottom);

        /**
         * 指示器在底部
         *
         * @param path
         * @param left
         * @param top
         * @param right
         * @param bottom
         */
        void drawBottom(Path path, int left, int top, int right, int bottom);
    }
}