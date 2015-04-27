package com.ztf.andyhua.networktest.app.command;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import com.ztf.andyhua.networktest.app.NetworkTest;
import com.ztf.andyhua.networktest.app.command.aidl.ICommandInterface;
import com.ztf.andyhua.networktest.app.utils.Tools;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * command same cmd line
 * <p/>
 * Created by AndyHua on 2015/4/25.
 */
public final class Command {
    // time out is 90 seconds
    public static final int TIMEOUT = 90000;
    // threads
    private static ExecutorService EXECUTORSERVICE = null;
    // ICommandInterface
    private static ICommandInterface I_COMMAND = null;
    // IService Lock
    private static final Object I_LOCK = new Object();
    /**
     * service link class, used to instantiate the service interface
     */
    private static ServiceConnection I_CONN = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            synchronized (I_LOCK) {
                I_COMMAND = ICommandInterface.Stub.asInterface(service);
                if (I_COMMAND == null) {
                    restart();
                } else {
                    try {
                        I_LOCK.notifyAll();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            dispose();
        }
    };

    // mark if bind service
    private static boolean IS_BIND = false;

    // destroy service thread
    private static Thread DESTROY_THREAD = null;

    /**
     * destroy service after 5 seconds run
     */
    private static void destroyService() {
        if (DESTROY_THREAD == null) {
            DESTROY_THREAD = new Thread() {
                @Override
                public void run() {
                    try {
                        sleep(5000);
                        dispose();
                    } catch (InterruptedException e) {
                        //e.printStackTrace();
                    }
                    DESTROY_THREAD = null;
                }
            };

            DESTROY_THREAD.setDaemon(true);
            DESTROY_THREAD.start();
        }
    }

    /**
     * cancel destroy service
     */
    private static void cancelDestroyService() {
        if (DESTROY_THREAD != null) {
            DESTROY_THREAD.interrupt();
            DESTROY_THREAD = null;
        }
    }

    /**
     * start bind service
     */
    private static void bindService() {
        synchronized (Command.class) {
            if (!IS_BIND) {
                Context context = NetworkTest.getApplication();
                if (context == null) {
                    throw new NullPointerException("Application is not null.Please NetworkTest.initialize(Application)");
                } else {
                    // init service
                    context.bindService(new Intent(context, CommandService.class),
                            I_CONN, Context.BIND_AUTO_CREATE);
                    IS_BIND = true;
                }
            }
        }
    }

    /**
     * run do command
     *
     * @param command command
     * @return result
     */
    private static String commandRun(Command command) {
        // wait bind
        if (I_COMMAND == null) {
            synchronized (I_LOCK) {
                if (I_COMMAND == null) {
                    try {
                        I_LOCK.wait();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        // cancel destroy service
        cancelDestroyService();

        // get result
        int count = 5;
        Exception error = null;
        while (count > 0) {
            if (command.isCancel) {
                if (command.listener != null) {
                    command.listener.onCancel();
                }
                break;
            }
            try {
                command.result = I_COMMAND.command(command.mId, command.timeout, command.parameters);
                if (command.listener != null) {
                    command.listener.onCompleted(command.result);
                }
                break;
            } catch (RemoteException e) {
                e.printStackTrace();
                error = e;
                count--;
                Tools.sleepIgnoreInterrupt(3000);
            }
        }

        // check is error
        if (count <= 0 && command.listener != null) {
            command.listener.onError(error);
        }

        // check is end and call destroy service
        if (I_COMMAND != null) {
            try {
                if (I_COMMAND.getTaskCount() <= 0) {
                    destroyService();
                }
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        return command.result;
    }

    /**
     * command the test
     *
     * @param command
     * @return
     */
    public static String command(Command command) {
        // check service
        if (!IS_BIND) {
            bindService();
        }

        return commandRun(command);
    }

    /**
     * command the test
     *
     * @param command
     * @param listener
     */
    public static void command(final Command command, CommandListener listener) {
        command.listener = listener;
        // check service
        if (!IS_BIND) {
            bindService();
        }

        // check and init executor
        if (EXECUTORSERVICE == null) {
            synchronized (Command.class) {
                if (EXECUTORSERVICE == null) {
                    // init threads executor
                    int size = Runtime.getRuntime().availableProcessors();
                    EXECUTORSERVICE = Executors.newFixedThreadPool(size > 0 ? size : 1);
                }
            }
        }

        // add executor service thread in executor run
        try {
            EXECUTORSERVICE.execute(new Runnable() {
                @Override
                public void run() {
                    commandRun(command);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            restart();
        }
    }

    /**
     * cancel test
     *
     * @param command
     */
    public static void cancel(Command command) {
        command.isCancel = true;
        if (I_COMMAND != null) {
            try {
                I_COMMAND.cancel(command.mId);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * restart the command service
     */
    public static void restart() {
        dispose();
        bindService();
    }

    /**
     * dispose unbind service stop service
     */
    public static void dispose() {
        synchronized (Command.class) {
            if (EXECUTORSERVICE != null) {
                try {
                    EXECUTORSERVICE.shutdownNow();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                EXECUTORSERVICE = null;
            }

            if (IS_BIND) {
                Context context = NetworkTest.getApplication();
                if (context != null) {
                    try {
                        context.unbindService(I_CONN);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                I_CONN = null;
                IS_BIND = false;
            }
        }
    }

    private int timeout = TIMEOUT;
    private String mId = null;
    private String parameters = null;
    private String result = null;
    private CommandListener listener = null;
    private boolean isCancel = false;

    /**
     * get a command
     *
     * @param params params eg: "/system/bin/ping", "-c", "4", "-s", "100","www.baidu.com"
     */
    public Command(String... params) {
        this(TIMEOUT, params);
    }

    /**
     * Get a Command
     *
     * @param timeout set this run timeOut
     * @param params  params eg: "/system/bin/ping", "-c", "4", "-s", "100","www.baidu.com"
     */
    public Command(int timeout, String... params) {
        // check params
        if (params == null) {
            throw new NullPointerException("params is not null");
        }

        //run
        StringBuilder sb = new StringBuilder();
        for (String str : params) {
            sb.append(str);
            sb.append(" ");
        }

        this.parameters = sb.toString();
        this.mId = UUID.randomUUID().toString();
        this.timeout = timeout;
    }

    /**
     * delete the callback command listener
     */
    public void removeListener() {
        listener = null;
    }

    public static interface CommandListener {
        public void onCompleted(String str);

        public void onCancel();

        public void onError(Exception e);
    }
}
