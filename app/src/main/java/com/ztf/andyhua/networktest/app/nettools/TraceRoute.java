package com.ztf.andyhua.networktest.app.nettools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by AndyHua on 2015/4/27.
 */
public class TraceRoute extends NetModel implements TraceRouteThread.TraceThreadInterface {
    private static final int ONCE_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int LOOP_COUNT = 30 / ONCE_COUNT;

    private final Object LOCK = new Object();
    private String target;
    private String IP;
    private List<String> routes = null;

    private transient int errorCount = 0;
    private transient boolean isDone = false;
    private transient boolean isArrived = false;
    private transient List<TraceRouteContainer> routeContainers = null;
    private transient List<TraceRouteThread> threads = null;
    private transient CountDownLatch countDownLatch = null;

    /**
     * trace route domain or ip
     *
     * @param target
     */
    public TraceRoute(String target) {
        this.target = target;
    }

    /**
     * clear thread list
     */
    private void clear() {
        if (threads != null) {
            synchronized (LOCK) {
                for (TraceRouteThread thread : threads) {
                    thread.cancel();
                }
            }
        }
    }

    /**
     * override start
     */
    @Override
    public void start() {
        // get IPs
        DnsResolve dns = new DnsResolve(target);
        dns.start();
        List<String> ips = dns.getAddresses();
        if (dns.getError() != NetModel.SUCCEED || ips == null || ips.size() == 0) {
            return;
        }

        IP = ips.get(0);

        // init list
        routeContainers = new ArrayList<TraceRouteContainer>();
        threads = new ArrayList<TraceRouteThread>(ONCE_COUNT);

        // loop
        for (int i = 0; i < LOOP_COUNT; ++i) {
            countDownLatch = new CountDownLatch(ONCE_COUNT);
            synchronized (LOCK) {
                for (int j = 1; j <= ONCE_COUNT; ++j) {
                    // get ttl
                    final int ttl = i * ONCE_COUNT + j;
                    // thread run get tp ttl ping info
                    threads.add(new TraceRouteThread(IP, ttl, this));
                }
            }

            // await 40 seconds long time
            try {
                countDownLatch.await(40, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // end
            if (countDownLatch.getCount() > 0) {
                clear();
            }

            // clear
            countDownLatch = null;
            synchronized (LOCK) {
                threads.clear();
            }

            // break loop
            if (isDone || isArrived || errorCount > 3) {
                break;
            }
        }

        // set result
        if (routeContainers.size() > 0) {
            // sort
            Collections.sort(routeContainers,
                    new TraceRouteContainer.TraceRouteContainerComparator());

            // set values
            ArrayList<String> routes = new ArrayList<String>();
            int size = routeContainers.size();
            String prevIP = null;

            // for loop IP
            for (int s = 0; s < size; ++s) {
                TraceRouteContainer container = routeContainers.get(s);
                if (prevIP != null && container.ip.equals(prevIP)) {
                    break;
                } else {
                    routes.add(container.toString());
                    prevIP = container.ip;
                }
            }

            routes.trimToSize();
            this.routes = routes;
        }

        // clear
        routeContainers = null;
        threads = null;
    }

    /**
     *
     */
    @Override
    public void cancel() {
        isDone = true;
        clear();
    }

    @Override
    public void complete(TraceRouteThread trace, boolean isError, boolean isArrived, TraceRouteContainer routeContainer) {
        if (threads != null) {
            synchronized (LOCK) {
                try {
                    threads.remove(trace);
                } catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }

        if (!isDone) {
            if (isError) {
                this.errorCount++;
            }
            this.isArrived = isArrived;
            if (routeContainers != null && routeContainer != null) {
                routeContainers.add(routeContainer);
            }
        }

        if (countDownLatch != null && countDownLatch.getCount() > 0) {
            countDownLatch.countDown();
        }
    }

    /**
     * the routes target IP
     *
     * @return IP address
     */
    public String getAddress() {
        return IP;
    }

    /**
     * for routing values
     *
     * @return
     */
    public List<String> getRoutes() {
        return routes;
    }

    @Override
    public String toString() {
        return "TraceRoute{" +
                "target='" + target + '\'' +
                ", IP='" + IP + '\'' +
                ", routes=" + (routes == null ? "[]" : routes.toString()) +
                '}';
    }
}
