package com.ztf.andyhua.networktest.app.nettools;

import java.util.Comparator;

/**
 * routing info
 * <p/>
 * Created by AndyHua on 2015/4/27.
 */
public class TraceRouteContainer {
    public String ip;
    public int ttl;
    public float loss;
    public float delay;

    public TraceRouteContainer(int ttl, String ip, float loss, float delay) {
        this.ttl = ttl;
        this.ip = ip;
        this.loss = loss;
        this.delay = delay;
    }

    @Override
    public String toString() {
        return "ttl:" + ttl +
                ", ip:'" + ip + '\'' +
                ", loss:" + loss +
                ", delay:" + delay;
    }

    /**
     * the TraceRoute results are sorted by this class
     */
    protected static class TraceRouteContainerComparator implements Comparator<TraceRouteContainer> {

        @Override
        public int compare(TraceRouteContainer lhs, TraceRouteContainer rhs) {
            if (lhs == null) {
                return 1;
            }
            if (rhs == null) {
                return -1;
            }

            if (lhs.ttl < rhs.ttl) {
                return -1;
            } else if (lhs.ttl == rhs.ttl) {
                return 0;
            } else {
                return 1;
            }
        }
    }
}
