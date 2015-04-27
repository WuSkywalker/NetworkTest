// ICommandInterface.aidl
package com.ztf.andyhua.networktest.app.command.aidl;

// Declare any non-default types here with import statements

interface ICommandInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    String command(String id, int timeout, String params);
    void cancel(String id);
    int getTaskCount();
}
