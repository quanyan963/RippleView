package com.example.komoriwu.rippleview.widget;

import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MotionEvent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.Interpolator;

/**
 * Created by KomoriWu on 2017/12/14.
 */

public class RippleDrawable extends Drawable {
    //透明度 0~255
    private int mAlpha = 255;
    private int mRippleColor;
    private int mBgAlpha = 0,mCircleAlpha = 255;
    private Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float mPointX, mPointY, mRadius,mMoveX,mMoveY;
    private double maxRadius;
    private int mWidth, mHeight;
    private float mProgress = 0;
    private float mExitProgress = 0;
    //每次递增的进度值
    private float mIncrement = 16f / 300;
    private float mExitIncrement = 16f / 200;
    //插值器
    private Interpolator mInterpolator;
    private Interpolator mExitInterpolator;
    private int backgroundColor;
    private boolean isEnterFinish,isActionUp;

    public RippleDrawable(int color) {
        //抗锯齿
        mPaint.setAntiAlias(true);
        //防抖动
        mPaint.setDither(true);
        //设置画笔为填充方式
        mPaint.setStyle(Paint.Style.FILL);
        //设置涟漪颜色
        setRippleColor(color);

        mInterpolator = new DecelerateInterpolator(2);//表示3倍速  由快到慢
        mExitInterpolator = new AccelerateInterpolator(2);//由慢到快
        //设置滤镜  LightingColorFilter  第一个代表保留的颜色 第二个代表要填充的颜色
        //setColorFilter(new LightingColorFilter(Color.BLACK,Color.RED));
    }

    /**
     * 设置涟漪的颜色
     * @param color
     */
    public void setRippleColor(int color) {
        mRippleColor = color;
        onColorOrAlphaChange();
    }

    private void onColorOrAlphaChange() {
        mPaint.setColor(mRippleColor);
        //得到颜色本身的透明度
        //计算透明度
        int alpha = mPaint.getAlpha();
        int realAlpha = (int) (alpha * (mAlpha / 255f));
        mPaint.setAlpha(realAlpha);
        invalidateSelf();
    }

    //更改颜色透明度
    private int changeColorAlpha(int color, int alpha){
        int a = (color >> 24) & 0xFF;
        a = (int) (a * (alpha/255f));
        int r = (color >> 16) & 0xFF;
        int g = (color >> 8) & 0xFF;
        int b = color & 0xFF;
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        int preAlpha = mPaint.getAlpha();
        //计算背景透明度
        int bgAlpha = (int) (preAlpha*(mBgAlpha/255f));
        int maxCircleAlpha = getCircleAlpha(preAlpha,bgAlpha);
        int circleAlpha = (int) (maxCircleAlpha*(mCircleAlpha/255f));
        //绘制背景区颜色
        mPaint.setAlpha(bgAlpha);
        canvas.drawColor(mPaint.getColor());
        //画圆
        mPaint.setAlpha(circleAlpha);
        canvas.drawCircle(mPointX, mPointY, mRadius, mPaint);
        //invalidateSelf();
        mPaint.setAlpha(preAlpha);
    }

    /**
     * 通过两块玻璃叠加颜色更深，光线透过更少的算法反向推出其中一块玻璃的值
     * @param preAlpha
     * @param bgAlpha
     * @return
     */
    private int getCircleAlpha(int preAlpha, int bgAlpha){
        int dAlpha = preAlpha - bgAlpha;
        return (int) ((dAlpha*255f)/(255f - bgAlpha));
    }

    @Override
    public void setAlpha(int alpha) {
        mAlpha = alpha;
        onColorOrAlphaChange();
    }

    @Override
    public int getAlpha() {
        return mPaint.getAlpha();
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        //设置颜色滤镜
        if (mPaint.getColorFilter() != colorFilter) {
            mPaint.setColorFilter(colorFilter);
            invalidateSelf();
        }
    }

    @Override
    public int getOpacity() {
        //得到当前drawable是否有透明度
        mAlpha = mPaint.getAlpha();
        switch (mAlpha) {
            case 255:
                //不透明
                return PixelFormat.OPAQUE;
            case 0:
                //全透明
                return PixelFormat.TRANSPARENT;
            default:
                //半透明
                return PixelFormat.TRANSLUCENT;

        }
    }

    Runnable exitRunnable = new Runnable() {
        @Override
        public void run() {
            if (mExitProgress < 1) {
                mExitProgress = mExitProgress + mExitIncrement;
                float realProgress = mExitInterpolator.getInterpolation(mExitProgress);
                onExitProgressChange(realProgress);

                //刷新频率接近60fps
                scheduleSelf(this, SystemClock.uptimeMillis() + 16);
            } else {
                unscheduleSelf(this);
            }

        }
    };

    /**
     * 退出动画
     * @param progress
     */
    private void onExitProgressChange(float progress) {
        //背景减淡
        mBgAlpha = (int) getProgressValue(172,0,progress > 1 ? 1 : progress);
        //圆形减淡
        mCircleAlpha = (int) getProgressValue(255,0,progress > 1 ? 1 : progress);
        //backgroundColor = changeColorAlpha(0x30000000,alpha);
        //mAlpha = (int) getProgressValue(255,0,progress > 1 ? 1 : progress);
        invalidateSelf();
    }

    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (mProgress < 1) {
                mProgress = mProgress + mIncrement;
                float realProgress = mInterpolator.getInterpolation(mProgress);
                onProgressChange(realProgress);

                //刷新频率接近60fps
                scheduleSelf(this, SystemClock.uptimeMillis() + 16);
            } else {
                isEnterFinish = true;
                if (isActionUp){
                    onExitRunnable();
                }
            }

        }
    };

    private void onExitRunnable() {
        mExitProgress = 0;
        unscheduleSelf(runnable);
        unscheduleSelf(exitRunnable);
        scheduleSelf(exitRunnable,SystemClock.uptimeMillis());
    }

    private void onProgressChange(float progress) {
        mRadius = (float) (maxRadius * progress);
        mRadius = getProgressValue(0, (float) maxRadius,progress);
        mPointX = getProgressValue(mMoveX,mWidth,progress);
        mPointY = getProgressValue(mMoveY,mHeight,progress);
        mBgAlpha = (int) getProgressValue(0,172,progress);
        //backgroundColor = changeColorAlpha(0x30000000,alpha);
        invalidateSelf();
    }

    private float getProgressValue(float start, float end, float progress) {
        return start + (end - start) * progress;
    }

    /**
     * 在RippleButton的onSizeChanged函数调用时会setbound 将改变的宽高进行传递
     * 复写onBoundsChange方法  获取宽高
     *
     * @param bounds
     */
    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mWidth = bounds.centerX();
        mHeight = bounds.centerY();
        maxRadius = Math.sqrt(Math.pow(mWidth, 2) + Math.pow(mHeight, 2));
    }

    public void onTouch(MotionEvent event) {
        /**
         * 1 getAction:触摸动作的原始32位信息，包括事件的动作，触控点信息
         * 2 getActionMask:触摸的动作,按下，抬起，滑动，多点按下，多点抬起
         * 3 getActionIndex:触控点信息
         */
        //判断点击操作类型
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                onTouchDown(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_UP:
                onTouchUp(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_MOVE:
                onTouchMove(event.getX(), event.getY());
                break;
            case MotionEvent.ACTION_CANCEL:
                onTouchCancel(event.getX(), event.getY());
                break;
        }
    }

    private void onTouchDown(float x, float y) {
        mCircleAlpha = 255;
        isEnterFinish = false;
        isActionUp = false;
        mRadius = 0;
        mMoveX = x;
        mMoveY = y;
        mProgress = 0;
        unscheduleSelf(runnable);
        unscheduleSelf(exitRunnable);
        scheduleSelf(runnable, SystemClock.uptimeMillis());
    }

    private void onTouchUp(float x, float y) {
        isActionUp = true;
        if (isEnterFinish){
            onExitRunnable();
        }
    }

    private void onTouchMove(float x, float y) {

    }

    private void onTouchCancel(float x, float y) {
        isActionUp = true;
        if (isEnterFinish){
            onExitRunnable();
        }
    }
}
