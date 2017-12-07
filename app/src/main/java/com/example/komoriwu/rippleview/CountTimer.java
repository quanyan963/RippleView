package com.example.komoriwu.rippleview;

import android.os.CountDownTimer;


/**
 * Created by KomoriWu on 2017/10/9.
 */

public class CountTimer extends CountDownTimer {
    private TimerListener listener;
    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */
    public CountTimer(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    @Override
    public void onTick(long millisUntilFinished) {

    }

    @Override
    public void onFinish() {
        listener.onFinish();
    }

    public void setOnFinishListener(TimerListener listener){
        this.listener = listener;
    }
}
