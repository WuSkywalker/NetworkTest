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
     * sleep time
     * don't htrow a interrupted exception
     *
     * @param time
     */
    public static void sleepIgnoreInterrupt(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * copy file to file
     *
     * @param source source file
     * @param target target file
     * @return copy is ok
     */
    public static boolean copyFile(File source, File target) {
        boolean bFlag = false;
        FileInputStream in = null;
        FileOutputStream out = null;
        try {
            if (!target.exists()) {
                if (!target.createNewFile()) {
                    // create error
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
        } catch (IOException e) {
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
     * equipment is started for the first time the generated number
     * are potential "9774d56d682e549c"
     *
     * @param context
     * @return
     */
    public static String getAndroidId(Context context) {
        return android.provider.Settings.System.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    /**
     * device's sn
     *
     * @return
     */
    public static String getSerialNumber() {
        String serialNumber = Build.SERIAL;
        if (serialNumber == null || serialNumber.length() == 0 || serialNumber.contains("unknown")) {
            String[] keys = new String[]{"ro.boot.serialno", "ro.serialno"};
            for (String key : keys) {
                try {
                    Method systemProperties_get = Class.forName("android.os.SystemProperties").getMethod("get", String.class);
                    serialNumber = (String) systemProperties_get.invoke(null, key);
                    if (serialNumber != null && serialNumber.length() > 0 && !serialNumber.contains("unknown")) {
                        break;
                    }
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
        }

        return serialNumber;
    }

    /**
     * get a new color, the color's alpha is new set
     *
     * @param color color
     * @param alpha alpha
     * @return new color
     */
    public static int getNewAlphaColor(int color, int alpha) {
        int r = (color >> 16) & 0xff;
        int g = (color >> 8) & 0xff;
        int b = (color >> 0) & 0xff;
        return alpha << 24 | r << 16 | g << 8 | b << 0;
    }
}
