package com.example.komoriwu.rippleview.widget;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import com.example.komoriwu.rippleview.R;

/**
 * Created by KomoriWu on 2017/12/14.
 */

@SuppressLint("AppCompatCustomView")
public class RippleButton extends Button {
    private RippleDrawable mRippleDrawable;
    private Bitmap mBitmap;
    private double maxRaduio;
    public RippleButton(Context context) {
        this(context,null);
    }

    public RippleButton(Context context, AttributeSet attrs) {
        this(context, attrs,0);
    }

    public RippleButton(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
//        mRippleDrawable = new RippleDrawable(BitmapFactory.
//                decodeResource(getResources(), R.mipmap.ic_launcher));
        mRippleDrawable = new RippleDrawable();
        //设置刷新接口，此接口在view中已经实现  无需new
        mRippleDrawable.setCallback(this);
    }

    //invalidateSelf()中会初始化区域，不写onSizeChanged  初始化的所有参数为空  自然无法绘制
    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        //设置drawable绘制及刷新区域
        mRippleDrawable.setBounds(0,0,getWidth(),getHeight());
    }

    //验证drawable  让其能够走通invalidateSelf()方法
    @Override
    protected boolean verifyDrawable(@NonNull Drawable who) {
        return who == mRippleDrawable || super.verifyDrawable(who);
    }

    @Override
    protected void onDraw(Canvas canvas) {


        //绘制自己的drawable  先绘制自己的drawable  不然会覆盖xml中的其他效果
        mRippleDrawable.draw(canvas);

        super.onDraw(canvas);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mRippleDrawable.onTouch(event);
        //return super.onTouchEvent(event);
        return true;
    }
}
