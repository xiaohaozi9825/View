package pw.xiaohaozi.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Size;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.FrameLayout;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import pw.xiaohaozi.view.R;

/**
 * 自动滚动视图
 * 功能：
 * 向左或向上滚动子视图
 * 使用对象：当子视图内容过多，且不能人工滑动滚动的情况，
 * 如：温馨提示，滚动通知
 * <p>
 * 使用注意事项：
 * 1、如果想要子视图尺寸发生变化，则重新滚动，则需要将子控件的宽高设置成包裹内容
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class AutoScrollView extends FrameLayout {
    /**
     * 慢速滚动
     */
    public static final int VELOCITY_SLOW = 100;
    /**
     * 中速滚动
     */
    public static final int VELOCITY_HOROTELIC_RATE = 200;
    /**
     * 快速滚动
     */
    public static final int VELOCITY_FAST = 300;

    private int mVelocity = VELOCITY_HOROTELIC_RATE;
    private TranslateAnimation mTranslateAnimation;
    private Attribute mAttribute = Attribute.EXCEED;//属性
    private Direction mDirection = Direction.HORIZONTAL;
    private int mWidth;
    private int mHeight;
    private Size mChildSize;
    private Size mChildOldSize = new Size(-1, -1);
    private ScrollCallBack mScrollCallBack;
    private boolean isScroll = false;

    public AutoScrollView(@NonNull Context context) {
        this(context, null);
    }

    public AutoScrollView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AutoScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    /**
     * 方法二
     * 虽然繁琐，但可以有效解决方法一中的bug
     */
    public AutoScrollView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        if (attrs != null) {
            //1、获取 TypedArray
            //AutoScrollView 为attrs中declare-styleable的name值
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable.AutoScrollView);

            //2、获取属性，enum用getInt();返回值是定义enum中的value
            //滚动速度
            mVelocity = typedArray.getInt(R.styleable.AutoScrollView_autoScrollVelocity, VELOCITY_HOROTELIC_RATE);
            //滚动方向：水平、垂直
            int autoScrollDirection = typedArray.getInt(R.styleable.AutoScrollView_autoScrollDirection, 1);
            mDirection = autoScrollDirection == 1 ? Direction.HORIZONTAL : Direction.VERTICAL;
            //属性：超出滚动、永久滚动
            int autoScrollAttribute = typedArray.getInt(R.styleable.AutoScrollView_autoScrollAttribute, 1);
            mAttribute = autoScrollAttribute == 1 ? Attribute.EXCEED : Attribute.ALWAYS;

            //3、回收typedArray，提高性能
            typedArray.recycle();
        }
        init();
    }

    private void init() {
    }


    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // MeasureSpec.AT_MOST; 至多模式, 控件有多大显示多大, wrap_content
        // MeasureSpec.EXACTLY; 确定模式, 类似宽高写死成dip, match_parent
        // MeasureSpec.UNSPECIFIED; 未指定模式.
        int widthMode = MeasureSpec.getMode(widthMeasureSpec);//获取宽模式
        int heightMode = MeasureSpec.getMode(heightMeasureSpec);//获取高模式
        int width = MeasureSpec.getSize(widthMeasureSpec);//获取宽度值
        int height = MeasureSpec.getSize(heightMeasureSpec);//获取高度值

        int mPaddingLeft = getPaddingLeft();
        int mPaddingTop = getPaddingTop();
        int mPaddingRight = getPaddingRight();
        int mPaddingBottom = getPaddingBottom();
        //子控件尺寸测量
        View child = getChildAt(0);
        LayoutParams childLayoutParams = (LayoutParams) child.getLayoutParams();
        int childWidth = width - mPaddingLeft - mPaddingRight - childLayoutParams.leftMargin - childLayoutParams.rightMargin;
        int childHeight = height - mPaddingTop - mPaddingBottom - childLayoutParams.topMargin - childLayoutParams.bottomMargin;
        int childWidthMeasureSpec;
        int childHeightMeasureSpec;

        if (mDirection == Direction.HORIZONTAL) {
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, MeasureSpec.UNSPECIFIED);
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, heightMode);
        } else {
            //因为只能容纳一个空间，所以会填充宽度
            childWidthMeasureSpec = MeasureSpec.makeMeasureSpec(childWidth, widthMode);
            childHeightMeasureSpec = MeasureSpec.makeMeasureSpec(childHeight, MeasureSpec.UNSPECIFIED);
        }
        child.measure(childWidthMeasureSpec, childHeightMeasureSpec);//测量某一个子控件，以上三个方法都调用了该方法

        //本空间尺寸测量
        if (heightMode == MeasureSpec.AT_MOST) {
            if (mDirection == Direction.HORIZONTAL) {
                height = child.getMeasuredHeight() + mPaddingTop + mPaddingBottom + childLayoutParams.topMargin + childLayoutParams.bottomMargin;
            } else {
                width = child.getMeasuredWidth() + mPaddingLeft + mPaddingRight + childLayoutParams.leftMargin + childLayoutParams.rightMargin;
            }
        }
        widthMeasureSpec = MeasureSpec.makeMeasureSpec(width, widthMode);
        heightMeasureSpec = MeasureSpec.makeMeasureSpec(height, heightMode);
        setMeasuredDimension(widthMeasureSpec, heightMeasureSpec);
        mChildSize = new Size(child.getMeasuredWidth(), child.getMeasuredHeight());
        if (mChildOldSize.getWidth() != mChildSize.getWidth()
                || mChildOldSize.getHeight() != mChildSize.getHeight()) {
            if (mWidth > 0 && mHeight > 0) reStart();
        }
        mChildOldSize = new Size(child.getMeasuredWidth(), child.getMeasuredHeight());
    }

    @Override
    protected void onFinishInflate() {
        if (getChildCount() > 1) {
            throw new IllegalStateException("AutoScrollView 只能拥有一个子控件");
        }
        super.onFinishInflate();
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        mWidth = w;
        mHeight = h;
        start();
    }

    private void start() {
        if (getChildCount() < 1) return;
        final View child = getChildAt(0);
        int s;//位移（单位：像素）

        int childMeasuredWidth = child.getMeasuredWidth();
        int childMeasuredHeight = child.getMeasuredHeight();

        if (mDirection == Direction.HORIZONTAL) {
            if (mAttribute == Attribute.EXCEED && childMeasuredWidth <= mWidth) return;
            s = childMeasuredWidth + mWidth;
            //fromXDelta     起始点X轴坐标，可以是数值、百分数、百分数p 三种样式，同scale
            //fromYDelta    起始点Y轴从标，可以是数值、百分数、百分数p 三种样式
            //toXDelta         结束点X轴坐标
            //toYDelta        结束点Y轴坐标
            mTranslateAnimation = new TranslateAnimation(mWidth, -childMeasuredWidth, 0, 0);
        } else {
            if (mAttribute == Attribute.EXCEED && childMeasuredHeight <= mHeight) return;

            s = childMeasuredHeight + mHeight;
            mTranslateAnimation = new TranslateAnimation(0, 0, mHeight, -childMeasuredHeight);
        }

        int t = s / mVelocity;//时间 （单位：毫秒）
        mTranslateAnimation.setDuration(t * 1000);

        //重复次数
        mTranslateAnimation.setRepeatCount(-1);
        mTranslateAnimation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                if (mScrollCallBack != null) mScrollCallBack.onStart(child);
                isScroll = true;
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if (mScrollCallBack != null) mScrollCallBack.onEnd(child);
                isScroll = false;
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                if (mScrollCallBack != null) mScrollCallBack.onRepeat(child);
            }
        });
        child.startAnimation(mTranslateAnimation);
    }

    private void stop() {
        if (getChildCount() < 1) return;
        final View child = getChildAt(0);
        if (mTranslateAnimation != null) {
            child.clearAnimation();
        }
    }

    private void reStart() {
        stop();
        start();
    }


    /**
     * 设置滚动回调函数
     *
     * @param scrollCallBack
     */
    public void setScrollCallBack(ScrollCallBack scrollCallBack) {
        mScrollCallBack = scrollCallBack;
    }


    /**
     * 设置滚动速度
     *
     * @param velocity 滚动速度（单位：像素/秒）
     */
    public void setVelocity(int velocity) {
        mVelocity = velocity;
        invalidate();
    }

    /**
     * 设置滚动方向
     *
     * @param direction 水平或垂直
     */
    public void setDirection(Direction direction) {
        mDirection = direction;
        invalidate();
    }

    /**
     * 设置滚动属性
     * ALWAYS 永远滚动，不管子控件尺寸如何，都会滚动；
     * EXCEED 超出范围滚动，只有当子控件尺寸超出了父控件，才会滚动
     *
     * @param attribute
     */
    public void setAttribute(Attribute attribute) {
        mAttribute = attribute;
    }

    /**
     * 是否正在滚动
     *
     * @return
     */
    public boolean isScroll() {
        return isScroll;
    }


    /**************************************************/
    @Override
    public void addView(View child) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("AutoScrollView 只能拥有一个子控件");
        }
        super.addView(child);
    }

    @Override
    public void addView(View child, int index) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("AutoScrollView 只能拥有一个子控件");
        }

        super.addView(child, index);
    }

    @Override
    public void addView(View child, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("AutoScrollView 只能拥有一个子控件");
        }

        super.addView(child, params);
    }

    @Override
    public void addView(View child, int index, ViewGroup.LayoutParams params) {
        if (getChildCount() > 0) {
            throw new IllegalStateException("AutoScrollView 只能拥有一个子控件");
        }

        super.addView(child, index, params);
    }

    /******************************************************/
    public interface ScrollCallBack {
        void onStart(View child);

        void onEnd(View child);

        void onRepeat(View child);

    }

    public enum Direction {

        /**
         * 水平滚动
         */
        HORIZONTAL,
        /**
         * 垂直滚动
         */
        VERTICAL

    }

    public enum Attribute {
        /**
         * 超出范围滚动，只有当子控件尺寸超出了父控件，才会滚动
         */
        EXCEED,
        /**
         * 永远滚动，不管子控件尺寸如何，都会滚动；
         */
        ALWAYS

    }
}
