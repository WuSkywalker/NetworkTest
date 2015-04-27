package com.ztf.andyhua.networktest.app.nettools;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AndyHua on 2015/4/27.
 */
public class DnsResolve extends NetModel {

    private static final byte[] ID = new byte[]{(byte) 2, (byte) 6};
    private static final int TIME_OUT = 8000;
    private String hostName;
    private InetAddress server;
    private List<String> IPs;
    private long delay;

    /**
     * domain name resolution test
     *
     * @param hostName domain name address
     */
    public DnsResolve(String hostName) {
        this.hostName = hostName;
    }

    /**
     * domain name resolution test
     *
     * @param hostName domain name address
     * @param server   specify the domain name server
     */
    public DnsResolve(String hostName, InetAddress server) {
        this.hostName = hostName;
        this.server = server;
    }

    /**
     * this resolve domain ips
     *
     * @param domain    domain name
     * @param dnsServer DNS server
     * @return ips
     */
    private ArrayList<String> resolve(String domain, InetAddress dnsServer) {
        // pointer
        int pos = 12;
        // init buffer
        byte[] sendBuffer = new byte[100];
        // message head
        sendBuffer[0] = ID[0];
        sendBuffer[1] = ID[1];
        sendBuffer[2] = 0x01;
        sendBuffer[3] = 0x00;
        sendBuffer[4] = 0x00;
        sendBuffer[5] = 0x01;
        sendBuffer[6] = 0x00;
        sendBuffer[7] = 0x00;
        sendBuffer[8] = 0x00;
        sendBuffer[9] = 0x00;
        sendBuffer[10] = 0x00;
        sendBuffer[11] = 0x00;

        // add domain
        String[] part = domain.split("\\.");
        for (String s : part) {
            if (s == null && s.length() <= 0) {
                continue;
            }
            int length = s.length();
            sendBuffer[pos++] = (byte) length;
            int i = 0;
            char[] val = s.toCharArray();
            while (i < length) {
                sendBuffer[pos++] = (byte) val[i++];
            }
        }

        // 0 end
        sendBuffer[pos++] = 0x00;
        sendBuffer[pos++] = 0x00;
        // 1 A record query
        sendBuffer[pos++] = 0x01;
        sendBuffer[pos++] = 0x00;
        // Internet record query
        sendBuffer[pos++] = 0x01;

        /**
         * UDP send
         */
        DatagramSocket ds = null;
        byte[] receiveBuffer = null;
        try {
            ds = new DatagramSocket();
            ds.setSoTimeout(TIME_OUT);

            // send
            DatagramPacket dp = new DatagramPacket(sendBuffer, pos, dnsServer, 53);
            ds.send(dp);

            // receive
            dp = new DatagramPacket(new byte[512], 512);
            ds.receive(dp);

            // copy to receive buffer
            int len = dp.getLength();
            receiveBuffer = new byte[len];
            System.arraycopy(dp.getData(), 0, receiveBuffer, 0, len);
        } catch (UnknownHostException e) {
            error = UNKNOWN_HOST_ERROR;
        } catch (SocketException e) {
            error = NETWORK_SOCKET_ERROR;
            e.printStackTrace();
        } catch (IOException e) {
            error = NETWORK_IO_ERROR;
            e.printStackTrace();
        } finally {
            if (ds != null) {
                ds.close();
            }
        }

        /**
         * resolve data
         */

        // check is return
        if (error != SUCCEED || receiveBuffer == null) {
            return null;
        }

        // get ID
        if (receiveBuffer[0] != ID[0] || receiveBuffer[1] != ID[1]
                || (receiveBuffer[2] & 0x80) != 0x80) {
            return null;
        }

        // get count
        int queryCount = (receiveBuffer[4] << 8) | receiveBuffer[5];
        if (queryCount == 0) {
            return null;
        }

        int answerCount = (receiveBuffer[6] << 8) | receiveBuffer[7];
        if (answerCount == 0) {
            return null;
        }

        // pointer restore
        pos = 12;

        // skip the query part head
        for (int i = 0; i < queryCount; i++) {
            while (receiveBuffer[pos] != 0x00) {
                pos += receiveBuffer[pos] + 1;
            }
            pos += 5;
        }

        // get ip form data
        ArrayList<String> iPs = new ArrayList<String>();
        for (int i = 0; i < answerCount; i++) {
            if (receiveBuffer[pos] == (byte) 0xC0) {
                pos += 2;
            } else {
                while (receiveBuffer[pos] != (byte) 0x00) {
                    pos += receiveBuffer[pos] + 1;
                }
                pos++;
            }
            byte queryType = (byte) (receiveBuffer[pos] << 8 | receiveBuffer[pos + 1]);
            pos += 8;
            int dataLength = (receiveBuffer[pos] << 8 | receiveBuffer[pos + 1]);
            pos += 2;
            // Add ip
            if (queryType == (byte) 0x01) {
                int address[] = new int[4];
                for (int n = 0; n < 4; n++) {
                    address[n] = receiveBuffer[pos + n];
                    if (address[n] < 0)
                        address[n] += 256;
                }
                iPs.add(String.format("%d.%d.%d.%d", address[0], address[1], address[2], address[3]));
            }
            pos += dataLength;
        }
        return iPs;
    }

    @Override
    public void start() {
        long startTime = System.currentTimeMillis();
        if (server == null) {
            try {
                InetAddress[] adds = InetAddress.getAllByName(hostName);
                if (adds != null && adds.length > 0) {
                    IPs = new ArrayList<String>(adds.length);
                    for (InetAddress add : adds) {
                        IPs.add(add.getHostAddress());
                    }
                }
            } catch (UnknownHostException e) {
                error = UNKNOWN_HOST_ERROR;
                e.printStackTrace();
            } catch (Exception e) {
                error = UNKNOWN_ERROR;
            }
        } else {
            try {
                IPs = resolve(hostName, server);
            } catch (Exception e) {
                if (error == SUCCEED) {
                    error = UNKNOWN_ERROR;
                }
                e.printStackTrace();
            }
        }

        delay = System.currentTimeMillis() - startTime;
    }

    @Override
    public void cancel() {

    }

    public List<String> getAddresses() {
        return IPs;
    }

    public long getDelay() {
        return delay;
    }

    @Override
    public String toString() {
        return "DnsResolve{" +
                "hostName='" + hostName + '\'' +
                ", server=" + server +
                ", IPs=" + IPs +
                ", delay=" + delay +
                '}';
    }
}