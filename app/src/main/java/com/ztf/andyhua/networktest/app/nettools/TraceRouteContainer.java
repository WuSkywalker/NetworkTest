package com.ztf.andyhua.networktest.app.nettools;

import java.util.Comparator;

/**
 * routing info
 * <p/>
 * Created by AndyHua on 2015/4/27.
 */
public class TraceRouteContainer {
    public String IP;
    public int TTL;
    public float loss;
    public float delay;

    public TraceRouteContainer(int ttl, String ip, float loss, float delay) {
        this.TTL = ttl;
        this.IP = ip;
        this.loss = loss;
        this.delay = delay;
    }

    /**
     * The TraceRouteRoute results are sorted to sort the TraceRouteThread result
     */
    protected static class TraceRouteContainerComparator implements Comparator<TraceRouteContainer> {
        public int compare(TraceRouteContainer container1, TraceRouteContainer container2) {
            if (container1 == null)
                return 1;
            if (container2 == null)
                return -1;
            if (container1.TTL < container2.TTL)
                return -1;
            else if (container1.TTL == container2.TTL)
                return 0;
            else
                return 1;
        }
    }

    @Override
    public String toString() {
        return "TraceRouteContainer{" +
                "IP='" + IP + '\'' +
                ", TTL=" + TTL +
                ", loss=" + loss +
                ", delay=" + delay +
                '}';
    }
}
