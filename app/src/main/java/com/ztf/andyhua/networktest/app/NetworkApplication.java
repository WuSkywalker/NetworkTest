package com.ztf.andyhua.networktest.app;

import android.app.Application;

/**
 * Created by AndyHua on 2015/4/27.
 */
public class NetworkApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        NetworkTest.initialize(this);
    }
}
