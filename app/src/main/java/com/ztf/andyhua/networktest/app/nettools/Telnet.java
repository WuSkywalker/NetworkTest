package com.ztf.andyhua.networktest.app.nettools;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;

/**
 * Created by AndyHua on 2015/4/27.
 */
public class Telnet extends NetModel {
    private static final int TIME_OUT = 3000;
    private String host;
    private int port;
    private long delay;
    private boolean isConnected;

    public Telnet(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void start() {
        Socket socket = null;
        try {
            Long startTime = System.currentTimeMillis();
            socket = new Socket();
            try {
                socket.setSoTimeout(TIME_OUT);
            } catch (SocketException e) {
                e.printStackTrace();
            }

            socket.connect(new InetSocketAddress(host, port), TIME_OUT);
            if (isConnected = socket.isConnected()) {
                delay = System.currentTimeMillis() - startTime;
            } else {
                error = TCP_LINK_ERROR;
            }
        } catch (UnknownHostException e) {
            error = UNKNOWN_HOST_ERROR;
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void cancel() {

    }

    public boolean isConnected() {
        return isConnected;
    }

    public long getDelay() {
        return delay;
    }

    @Override
    public String toString() {
        return "Telnet{" +
                "host='" + host + '\'' +
                ", port=" + port +
                ", delay=" + delay +
                ", isConnected=" + isConnected +
                '}';
    }
}
