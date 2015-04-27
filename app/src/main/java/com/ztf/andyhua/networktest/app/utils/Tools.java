package com.ztf.andyhua.networktest.app.utils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by AndyHua on 2015/4/26.
 */
public final class Tools {
    /**
     * Sleep time
     * Don't throw an InterruptedException exception
     *
     * @param time long time
     */
    public static void sleepIgnoreInterrupt(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Copy file to file
     *
     * @param source Source File
     * @param target Target File
     * @return Return copy isOk
     */
    public static boolean copyFile(File source, File target) {
        boolean bFlag = false;
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            if (!target.exists()) {
                if (!target.createNewFile()) {
                    // Create Error
                    return false;
                }
            }
            in = new FileInputStream(source);
            out = new FileOutputStream(target);
            byte[] buffer = new byte[8 * 1024];
            int count;
            while ((count = in.read(buffer)) != -1) {
                out.write(buffer, 0, count);
            }
            bFlag = true;
        } catch (Exception e) {
            e.printStackTrace();
            bFlag = false;
        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                if (out != null) {
                    out.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return bFlag;
    }

    /**
     * Equipment is started for the first time the generated number
     * Are potential "9774d56d682e549c"
     *
     * @param context Context
     * @return Number
     */
    public static String getAndroidId(Context context) {
        return android.provider.Settings.System.getString(context.getContentResolver(), android.provider.Settings.Secure.ANDROID_ID);
    }

    /**
     * This device's SN
     *
     * @return SerialNumber
     */
    public static String getSerialNumber() {
        String serialNumber = android.os.Build.SERIAL;
        if ((serialNumber == null || serialNumber.length() == 0 || serialNumber.contains("unknown"))) {
            String[] keys = new String[]{"ro.boot.serialno", "ro.serialno"};
            for (String key : keys) {
                try {
                    Method systemProperties_get = Class.forName("android.os.SystemProperties").getMethod("get", String.class);
                    serialNumber = (String) systemProperties_get.invoke(null, key);
                    if (serialNumber != null && serialNumber.length() > 0 && !serialNumber.contains("unknown"))
                        break;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return serialNumber;
    }

    /**
     * Get a new color ,the color's alpha is new set
     *
     * @param color Color
     * @param alpha New alpha
     * @return New alpha color
     */
    public static int getNewAlphaColor(int color, int alpha) {
        int r = (color >> 16) & 0xff;
        int g = (color >> 8) & 0xff;
        int b = color & 0xff;
        return alpha << 24 | r << 16 | g << 8 | b;
    }
}
