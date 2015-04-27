package com.ztf.andyhua.networktest.app;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.ztf.andyhua.networktest.app.nettools.DnsResolve;
import com.ztf.andyhua.networktest.app.nettools.Ping;
import com.ztf.andyhua.networktest.app.nettools.Telnet;
import com.ztf.andyhua.networktest.app.nettools.TraceRoute;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * 基本网络测试
 * Created by AndyHua on 2015/4/27.
 */
public class NetworkActivity extends Activity {
    private static final String TAG = NetworkActivity.class.getSimpleName();

    private static final String TARGET = "www.baidu.com";
    private TextView result;
    private Button dnsBtn;
    private Button pingBtn;
    private Button telnetBtn;
    private Button traceRouteBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_network);

        result = (TextView) findViewById(R.id.result);
        result.setText("");

//        dnsBtn = (Button) findViewById(R.id.DNS);
//        pingBtn = (Button) findViewById(R.id.Ping);
//        telnetBtn = (Button) findViewById(R.id.Telnet);
//        traceRouteBtn = (Button) findViewById(R.id.TraceRoute);

        /*String lost = new String();
        String delay = new String();
        String resu = new String();
        try {
            Process process = new ProcessBuilder().command(
                    new String[]{"/system/bin/ping", "-c", "5", "www.baidu.com"}).redirectErrorStream(true).start();

            BufferedReader buff = new BufferedReader(new InputStreamReader(process.getInputStream()));

            int state = process.waitFor();
            if (state == 0) {
                resu = "success";
            } else {
                resu = "failed";
            }
            result.append("state : " + resu);
            String str = new String();

            while ((str = buff.readLine()) != null) {
                result.append(str);
                if (str.contains("packet loss")) {
                    int i = str.indexOf("received");
                    int j = str.indexOf("%");
                    System.out.println("丢包率:" + str.substring(i + 10, j + 1));
//                    System.out.println("丢包率:" + str.substring(j - 3, j + 1));
                    lost = str.substring(i + 10, j + 1);
                }
                if (str.contains("avg")) {
                    int i = str.indexOf("/", 20);
                    int j = str.indexOf(".", i);
                    System.out.println("延迟:" + str.substring(i + 1, j));
                    delay = str.substring(i + 1, j);
                    delay = delay + "ms";
                }

            }

            result.append(" lost : " + lost);
            result.append(" delay : " + delay);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    public void dns_onClick(View view) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                // target
                // DnsResolve dnsResolve = new DnsResolve(TARGET);
                DnsResolve dnsResolve = null;
                try {
                    // add DNS service
                    dnsResolve = new DnsResolve(TARGET, InetAddress.getByName("202.96.128.166"));
                    dnsResolve.start();
                    result.setText("DnsResolve：" + dnsResolve.toString());
                    Log.i(TAG, "DnsResolve：" + dnsResolve.toString());
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                dnsResolve.start();
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public void ping_onClick(View view) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                // packets, packet size, the target, whether parsing IP
                Ping ping = new Ping(4, 32, TARGET, true);
                ping.start();
                result.setText("Ping：" + ping.toString());
                Log.i(TAG, "Ping：" + ping.toString());
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public void telnet_onClick(View view) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                // target, port
                Telnet telnet = new Telnet(TARGET, 80);
                telnet.start();
                result.setText("Telnet：" + telnet.toString());
                Log.i(TAG, "Telnet：" + telnet.toString());
            }
        };
        thread.setDaemon(true);
        thread.start();
    }

    public void traceRoute_onClick(View view) {
        Thread thread = new Thread() {
            @Override
            public void run() {
                // target
                TraceRoute traceRoute = new TraceRoute(TARGET);
                traceRoute.start();
                result.setText("TraceRoute：" + traceRoute.toString());
                Log.i(TAG, "TraceRoute：" + traceRoute.toString());
            }
        };

        thread.setDaemon(true);
        thread.start();
    }
}
