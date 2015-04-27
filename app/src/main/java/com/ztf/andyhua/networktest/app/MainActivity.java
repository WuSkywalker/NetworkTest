package com.ztf.andyhua.networktest.app;

import android.app.Activity;
import android.content.Intent;
import android.os.*;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.ztf.andyhua.networktest.app.nettools.DnsResolve;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.Process;

public class MainActivity extends Activity {

    private Button commandBtn;
    private Button networkBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        commandBtn = (Button) findViewById(R.id.command);
//        networkBtn = (Button) findViewById(R.id.network);
    }

    public void command_onClick(View view) {
        Intent intent = new Intent(MainActivity.this, CommandActivity.class);
        startActivity(intent);
    }

    public void network_onClick(View view) {
        Intent intent = new Intent(MainActivity.this, NetworkActivity.class);
        startActivity(intent);
    }
}
