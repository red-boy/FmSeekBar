package com.example.hw.fmseekbar2;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.widget.Scroller;

import java.text.DecimalFormat;

/**
 * 收音机自定义样式Seekbar控件
 */
public class FmSeekbar extends View {
    public static final int MOD_TYPE = 5;  //刻度盘精度
    private static final int ITEM_HEIGHT_DIVIDER = 12;
    private int mLineDivider = ITEM_HEIGHT_DIVIDER;
    private static final int ITEM_MAX_HEIGHT = 36;  //最大刻度高度
    private static final int ITEM_MIN_HEIGHT = 25;  //最小刻度高度
    private int mModType = MOD_TYPE;  //刻度盘精度
    private float mValue = 50;
    private float mMaxValue = 100;
    private float mDefaultMinValue = 0;

    private Scroller mScroller;//滚动计算器
    private VelocityTracker mVelocityTracker;//触摸屏幕事件的速率跟踪

    private float mDensity;
    private int mMinVelocity;
    private int mWidth, mHeight;
    private int mLastX, mMove;
    private Paint mLinePaint = new Paint();
    private Paint mSelectPaint = new Paint();

    private OnValueChangeListener mListener;


    public interface OnValueChangeListener {
        void onValueChange(float value);
    }


    public void setValueChangeListener(OnValueChangeListener listener) {
        mListener = listener;
    }

    /**
     * 注意：
     * 这里构造方法中删除原基类的构造函数，修改成this
     */

    public FmSeekbar(Context context) {
        this(context, null);
    }

    public FmSeekbar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public FmSeekbar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        init(context);
    }

    private void init(Context context) {
        mScroller = new Scroller(context);
        mDensity = context.getResources().getDisplayMetrics().density;
        mMinVelocity = ViewConfiguration.get(getContext()).getScaledMinimumFlingVelocity();

    }

    /**
     * 获取当前刻度值
     */
    public float getValue() {
        return mValue;
    }

    /**
     * 设置当前刻度值.同时异步刷新视图重绘
     */
    public void setValue(float value) {
        mValue = value;
        postInvalidate();
    }

    /**
     * @param defaultValue 初始值
     * @param maxValue     最大值
     */
    public void initViewParam(float defaultValue, float maxValue, float defaultMinValue) {
        mValue = defaultValue;
        mMaxValue = maxValue;
        mDefaultMinValue = defaultMinValue;

        invalidate();//UI线程中重新draw()，但只会绘制调用者本身

        mLastX = 0;
        mMove = 0;
        notifyValueChange();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        mWidth = getWidth();
        mHeight = getHeight();
        super.onLayout(changed, left, top, right, bottom);
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        drawScaleLine(canvas);
        drawMiddleLine(canvas);
    }

    /**
     * 画中间指针
     */
    private void drawMiddleLine(Canvas canvas) {
        canvas.save();

        int selectWidth = 8;
        mSelectPaint.setStrokeWidth(selectWidth);//设置画笔宽度
        String selectColor = "#F7577F";
        mSelectPaint.setColor(Color.parseColor(selectColor));
        canvas.drawLine(mWidth / 2, 0, mWidth / 2, mHeight, mSelectPaint);


        canvas.restore();
    }

    /**
     * 从中间往两边开始画刻度线
     *
     * @param canvas 画布
     */
    private void drawScaleLine(Canvas canvas) {
        canvas.save();

        int normalLineWidth = 4;
        mLinePaint.setStrokeWidth(normalLineWidth);
        String normalLineColor = "#E8E8E8";
        mLinePaint.setColor(Color.parseColor(normalLineColor));

        //刻度线上标数值
        TextPaint textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);//消除锯齿
        textPaint.setTextSize(8 * mDensity);

        int width = mWidth;

        int drawCount = 0;
        float xPosition;

        for (int i = 0; drawCount < 4 * width; i++) {

            xPosition = (width / 2 - mMove) + i * mLineDivider * mDensity;
            if (xPosition + getPaddingLeft() < mWidth && (mValue + i) < mMaxValue) {
                if ((mValue + i) % mModType == 0) {

                    if (mValue + i <= mMaxValue) {
                        canvas.drawText(mhzCastStr((int) (mValue + i)), xPosition - 8 * mDensity, getHeight() - mDensity * ITEM_MAX_HEIGHT - 4, textPaint);
                    }

                    canvas.drawLine(xPosition, getHeight(), xPosition, getHeight() - mDensity * ITEM_MAX_HEIGHT, mLinePaint);
                } else {
                    canvas.drawLine(xPosition, getHeight(), xPosition, getHeight() - mDensity * ITEM_MIN_HEIGHT, mLinePaint);
                }
            }

            xPosition = (width / 2 - mMove) - i * mLineDivider * mDensity;
            if (xPosition > getPaddingLeft() && (mValue - i) >= mDefaultMinValue) {
                if ((mValue - i) % mModType == 0) {
                    if (mValue - i >= 0) {
                        canvas.drawText(mhzCastStr((int) (mValue - i)), xPosition - 8 * mDensity, getHeight() - mDensity * ITEM_MAX_HEIGHT - 4, textPaint);
                    }

                    canvas.drawLine(xPosition, getHeight(), xPosition, getHeight() - mDensity * ITEM_MAX_HEIGHT, mLinePaint);
                } else {
                    canvas.drawLine(xPosition, getHeight(), xPosition, getHeight() - mDensity * ITEM_MIN_HEIGHT, mLinePaint);
                }
            }


            drawCount += 2 * mLineDivider * mDensity;
        }

        canvas.restore();
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int action = event.getAction();
        int xPosition = (int) event.getX();

        if (mVelocityTracker == null) {
            mVelocityTracker = VelocityTracker.obtain();
        }
        mVelocityTracker.addMovement(event);

        switch (action) {
            case MotionEvent.ACTION_DOWN://按下
                mScroller.forceFinished(true);//强制停止滑动

                mLastX = xPosition;
                mMove = 0;
                break;
            case MotionEvent.ACTION_MOVE://移动
                mMove += (mLastX - xPosition);//得到移动的距离
                changeMoveAndValue();
                break;
            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                countMoveEnd();
                countVelocityTracker();
                return false;
            default:
                break;

        }

        mLastX = xPosition;
        return true;
    }

    //计数速率
    private void countVelocityTracker() {
        mVelocityTracker.computeCurrentVelocity(1000);
        float xVelocity = mVelocityTracker.getXVelocity();
        if (Math.abs(xVelocity) > mMinVelocity) {
            mScroller.fling(0, 0, (int) xVelocity, 0, Integer.MIN_VALUE, Integer.MAX_VALUE, 0, 0);
        }

    }

    private void countMoveEnd() {
        int roundMove = Math.round(mMove / (mLineDivider * mDensity));
        mValue = mValue + roundMove;
        mValue = mValue <= 0 ? 0 : mValue;
        mValue = mValue > mMaxValue ? mMaxValue : mValue;

        mLastX = 0;
        mMove = 0;

        notifyValueChange();
        postInvalidate();//子线程中实现界面刷新
    }

    //通知move值的改变
    private void changeMoveAndValue() {
        int tValue = (int) (mMove / (mLineDivider * mDensity));//多少个刻度
        if (Math.abs(tValue) > 0) {
            mValue += tValue;
            mMove -= tValue * mLineDivider * mDensity;
            if (mValue <= mDefaultMinValue || mValue > mMaxValue) {//超出刻度量程范围，从初始值开始
                mValue = mValue <= mDefaultMinValue ? mDefaultMinValue : mMaxValue;
                mMove = 0;
                mScroller.forceFinished(true);
            }

            notifyValueChange();
        }

        postInvalidate();
    }

    /**
     * 通知value值改变
     */
    private void notifyValueChange() {
        if (null != mListener) {
            if (mModType == MOD_TYPE) {
                mListener.onValueChange(mValue);
            }
        }
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            if (mScroller.getCurrX() == mScroller.getFinalX()) {//当前水平滚动的位置   最终停止的水平位置
                countMoveEnd();
            } else {
                int xPosition = mScroller.getCurrX();
                mMove += (mLastX - xPosition);
                changeMoveAndValue();
                mLastX = xPosition;
            }
        }
    }

    //频率值转换成String
    public static String mhzCastStr(int mhz) {
        DecimalFormat df = new DecimalFormat("00");
        return df.format(mhz / 10) + "." + String.valueOf(mhz % 10);
    }
}
