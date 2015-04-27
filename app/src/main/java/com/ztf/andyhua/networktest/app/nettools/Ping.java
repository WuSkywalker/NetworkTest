package com.ztf.andyhua.networktest.app.nettools;

import com.ztf.andyhua.networktest.app.command.Command;

/**
 * Created by AndyHua on 2015/4/25.
 */
public class Ping extends NetModel {

    private String target;
    private String ip = null;
    private float loss = 1f;
    private float delay = 0;
    private float totalTime = 0;

    private transient boolean isAnalysisIp;
    private transient int count, size;
    private transient Command command;

    /**
     * To specify the IP or domain name to Ping test and return the IP, packet loss,
     * delay parameter to specify the IP or domain name such as Ping test and return the IP,
     * packet loss, delay and other parameters
     *
     * @param target The target
     */
    public Ping(String target) {
        this(4, 32, target, true);
    }

    /**
     * To specify the IP or domain name to Ping test and return the IP, packet loss,
     * delay parameter to specify the IP or domain name such as Ping test and return the IP,
     * packet loss, delay and other parameters
     *
     * @param count  Packets
     * @param size   Packet size
     * @param target The target
     */
    public Ping(int count, int size, String target) {
        this(count, size, target, true);
    }

    /**
     * To specify the IP or domain name to Ping test and return the IP, packet loss,
     * delay parameter to specify the IP or domain name such as Ping test and return the IP,
     * packet loss, delay and other parameters
     *
     * @param count        Packets
     * @param size         Packet size
     * @param target       The target
     * @param isAnalysisIp Whether parsing IP
     */
    public Ping(int count, int size, String target, boolean isAnalysisIp) {
        this.isAnalysisIp = isAnalysisIp;
        this.count = count;
        this.size = size;
        this.target = target;
    }

    /**
     * To parse and load
     *
     * @return
     */
    private String launchPing() {
        long startTime = System.currentTimeMillis();
        command = new Command("/system/bin/ping",
                "-c", String.valueOf(count),
                "-s", String.valueOf(size),
                target);
        try {
            String res = Command.command(command);
            totalTime = (System.currentTimeMillis() - startTime);
            return res;
        } catch (Exception e) {
            cancel();
            return null;
        } finally {
            command = null;
        }
    }

    /**
     * parse ip
     *
     * @param ping
     * @return
     */
    private String parseIp(String ping) {
        String ip = null;
        try {
            if (ping.contains(NetModel.PING)) {
                int indexOpen = ping.indexOf(NetModel.PING_PAREN_THESE_OPEN);
                int indexClose = ping.indexOf(NetModel.PING_PAREN_THESE_CLOSE);
                ip = ping.substring(indexOpen + 1, indexClose);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ip;
    }

    /**
     * parse loss packets
     *
     * @param ping
     * @return
     */
    private float parseLoss(String ping) {
        float transmit = 0f, error = 0f, receive = 0f, lossRate = 0f;
        try {
            if (ping.contains(NetModel.PING_STATISTICS)) {
                String lossStr = ping.substring(ping.indexOf(NetModel.PING_BREAK_LINE, ping.indexOf(NetModel.PING_STATISTICS)) + 1);
                lossStr = lossStr.substring(0, lossStr.indexOf(NetModel.PING_BREAK_LINE));
                String strArray[] = lossStr.split(NetModel.PING_COMMA);
                for (String str : strArray) {
                    if (str.contains(NetModel.PING_TRANSMIT))
                        transmit = Float.parseFloat(str.substring(0, str.indexOf(NetModel.PING_TRANSMIT)));
                    else if (str.contains(NetModel.PING_RECEIVED))
                        receive = Float.parseFloat(str.substring(0, str.indexOf(NetModel.PING_RECEIVED)));
                    else if (str.contains(NetModel.PING_ERRORS))
                        error = Float.parseFloat(str.substring(0, str.indexOf(NetModel.PING_ERRORS)));
                    else if (str.contains(NetModel.PING_LOSS))
                        lossRate = Float.parseFloat(str.substring(0, str.indexOf(NetModel.PING_RATE)));
                }
            }
            if (transmit != 0)
                lossRate = error / transmit;
            else if (lossRate == 0)
                lossRate = error / (error + receive);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return lossRate;
    }

    /**
     * parse delay
     *
     * @param ping
     * @return
     */
    private float parseDelay(String ping) {
        float delay = 0;
        try {
            if (ping.contains(NetModel.PING_RTT)) {
                String lossStr = ping.substring(ping.indexOf(NetModel.PING_RTT));
                lossStr = lossStr.substring(lossStr.indexOf(NetModel.PING_EQUAL) + 2);
                String strArray[] = lossStr.split(NetModel.PING_SLASH);
                delay = Float.parseFloat(strArray[1]);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return delay;
    }

    @Override
    public void start() {
        String res = launchPing();
        if (res != null && res.length() > 0) {
            res = res.toLowerCase();
            if (res.contains(NetModel.PING_UNREACHABLE) && !res.contains(NetModel.PING_EXCEED)) {
                // Failed
                loss = 1f;
                error = HOST_UNREACHABLE_ERROR;
            } else {
                // Succeed
                loss = parseLoss(res);
                delay = parseDelay(res);
                if (isAnalysisIp)
                    ip = parseIp(res);
            }
        } else {
            error = DROP_DATA_ERROR;
        }
    }

    @Override
    public void cancel() {
        if (command != null)
            Command.cancel(command);
    }

    public String getIp() {
        return ip;
    }

    public float getLossRate() {
        return loss;
    }

    public float getDelay() {
        return delay;
    }

    public float getTotalTime() {
        return totalTime;
    }

    @Override
    public String toString() {
        return "Ping{" +
                "target='" + target + '\'' +
                ", ip='" + ip + '\'' +
                ", loss=" + loss +
                ", delay=" + delay +
                ", totalTime=" + totalTime +
                '}';
    }
}
