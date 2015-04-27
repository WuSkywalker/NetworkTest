package com.ztf.andyhua.networktest.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import com.ztf.andyhua.networktest.app.command.Command;

/**
 * Created by AndyHua on 2015/4/27.
 */
public class CommandActivity extends Activity {
    private static final String TAG = CommandActivity.class.getSimpleName();

    private static final String TARGET = "http://d.weibo.com/";

    private TextView result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_command);
        result = (TextView) findViewById(R.id.result);
    }

    public void sync_onClick(View view) {
        // Sync
        Thread thread = new Thread() {
            @Override
            public void run() {
                // the same way call way and the ProcessBuilder mass participation
                Command command = new Command(Command.TIMEOUT, "/system/bin/ping",
                        "-c", "4", "-s", "100",
                        TARGET);
                String res = Command.command(command);
//                result.setText("\n\nCommand Sync£º\n" + res);
                Log.i(TAG, "\n\nCommand Sync£º\n" + res);
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public void async_onClick(View view) {
        // Async
        Command command = new Command("/system/bin/ping",
                "-c", "4", "-s", "100",
                TARGET);
        // Asynchronous execution using callback methods, do not need to build a thread callback by listener
        Command.command(command, new Command.CommandListener() {
            @Override
            public void onCompleted(String str) {
//                result.setText("\n\nCommand Async onCompleted£º\n" + str);
                Log.i(TAG, "\n\nCommand Async onCompleted£º\n" + str);
            }

            @Override
            public void onCancel() {
//                result.setText("\n\nCommand Async onCancel");
                Log.i(TAG, "\n\nCommand Async onCancel");
            }

            @Override
            public void onError(Exception e) {
//                result.setText("\n\nCommand Async onError:" + (e != null ? e.toString() : "null"));
                Log.i(TAG, "\n\nCommand Async onError:" + (e != null ? e.toString() : "null"));
            }
        });
    }

}
