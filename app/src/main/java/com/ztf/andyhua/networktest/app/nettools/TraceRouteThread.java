package com.ztf.andyhua.networktest.app.nettools;

import com.ztf.andyhua.networktest.app.command.Command;

/**
 * this thread is run thread to get ping by ip and ttl values
 * Created by AndyHua on 2015/4/27.
 */
public class TraceRouteThread extends Thread {
    private int ttl;
    private String ip;
    private Ping ping;
    private Command command;
    private TraceThreadInterface traceThreadInterface;

    private boolean isArrived;
    private boolean isError;

    public TraceRouteThread(String ip, int ttl, TraceThreadInterface traceThreadInterface) {
        this.ip = ip;
        this.ttl = ttl;
        this.traceThreadInterface = traceThreadInterface;

        this.setName("TraceThread:" + ip + " " + ttl);
        this.setDaemon(true);
        this.start();
    }

    /**
     * ttl route
     *
     * @param ip
     * @param ttl
     * @return isError
     */
    private TraceRouteContainer trace(String ip, int ttl) {
        String res = launchRoute(ip, ttl);
        if (!this.isInterrupted() && res != null && res.length() > 0) {
            res = res.toLowerCase();
            if (res.contains(NetModel.PING_EXCEED) || !res.contains(NetModel.PING_UNREACHABLE)) {
                // success
                String pIp = parseIpFromRoute(res);
                if (!this.isInterrupted() && pIp != null && pIp.length() > 0) {
                    ping = new Ping(4, 32, pIp, false);
                    ping.start();
                    TraceRouteContainer routeContainer = new TraceRouteContainer(ttl,
                            pIp, ping.getLoss(), ping.getDelay());
                    ping = null;
                    isArrived = pIp.contains(ip);
                    return routeContainer;
                }
            }
        }
        isError = true;
        return null;
    }

    /**
     * get ttl IP
     *
     * @param ip
     * @param ttl
     * @return ping ip and ttl result
     */
    private String launchRoute(String ip, int ttl) {
        command = new Command("/system/bin/ping",
                "-c", "4",
                "-s", "32",
                "-t", String.valueOf(ttl),
                ip);
        String str = null;
        try {
            str = Command.command(command);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            command = null;
        }
        return str;
    }

    /**
     * parsing ip address
     *
     * @param ping
     * @return ip address
     */
    private String parseIpFromRoute(String ping) {
        String ip = null;
        try {
            if (ping.contains(NetModel.PING_FROM)) {
                // get ip when ttl exceeded
                int index = ping.indexOf(NetModel.PING_FROM);
                ip = ping.substring(index + 5);
                if (ip.contains(NetModel.PING_PAREN_THESE_OPEN)) {
                    int indexOpen = ip.indexOf(NetModel.PING_PAREN_THESE_OPEN);
                    int indexClose = ip.indexOf(NetModel.PING_PAREN_THESE_CLOSE);
                    ip = ip.substring(indexOpen + 1, indexClose);
                } else {
                    // get ip when after from
                    ip = ip.substring(0, ip.indexOf("\n"));
                    if (ip.contains(":")) {
                        index = ip.indexOf(":");
                    } else {
                        index = ip.indexOf(" ");
                    }
                    ip = ip.substring(0, index);
                }
            } else if (ping.contains(NetModel.PING)) {
                int indexOpen = ping.indexOf(NetModel.PING_PAREN_THESE_OPEN);
                int indexClose = ping.indexOf(NetModel.PING_PAREN_THESE_CLOSE);
                ip = ping.substring(indexOpen + 1, indexClose);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }

    @Override
    public void run() {
        super.run();
        TraceRouteContainer routeContainer = trace(ip, ttl);
        traceThreadInterface.complete(this, this.isError, this.isArrived, routeContainer);
        traceThreadInterface = null;
    }

    /**
     * cancel this thread
     */
    public void cancel() {
        if (ping != null) {
            ping.cancel();
        }
        if (command != null) {
            Command.cancel(command);
        }
        try {
            this.interrupt();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected static interface TraceThreadInterface {
        void complete(TraceRouteThread trace, boolean isError, boolean isArrived, TraceRouteContainer routeContainer);
    }
}
