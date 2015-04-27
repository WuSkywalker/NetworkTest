package com.ztf.andyhua.networktest.app;

import android.app.Application;
import com.ztf.andyhua.networktest.app.command.Command;

/**
 * Created by AndyHua on 2015/4/26.
 */
public final class NetworkTest {

    private static Application application;

    public static Application getApplication() {
        return application;
    }

    public static void initialize(Application application) {
        NetworkTest.application = application;
    }

    public static void dispose() {
        Command.dispose();
        NetworkTest.application = null;
    }
}
