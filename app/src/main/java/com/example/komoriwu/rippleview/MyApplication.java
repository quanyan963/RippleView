package com.example.komoriwu.rippleview;

import android.app.Application;

import com.dhh.websocket.RxWebSocketUtil;
import com.lzy.okgo.OkGo;

import okhttp3.OkHttpClient;

/**
 * Created by KomoriWu on 2017/10/19.
 */

public class MyApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        OkGo.getInstance().init(this);
        // show log,default false
        RxWebSocketUtil.getInstance().setShowLog(true);
    }
}
