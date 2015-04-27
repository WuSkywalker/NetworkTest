package com.ztf.andyhua.networktest.app.command;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import com.ztf.andyhua.networktest.app.command.aidl.ICommandInterface;
import com.ztf.andyhua.networktest.app.utils.Tools;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class CommandService extends Service {

    private CommandServiceImpl impl;

    public CommandService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        impl = new CommandServiceImpl();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (impl == null) {
            impl = new CommandServiceImpl();
        }
        return impl;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        super.onUnbind(intent);
        stopSelf();
        return false;
    }

    @Override
    public void onDestroy() {
        if (impl != null) {
            impl.destroy();
            impl = null;
        }
        super.onDestroy();

        // kill process
        android.os.Process.killProcess(android.os.Process.myPid());
    }

    private class CommandServiceImpl extends ICommandInterface.Stub {

        private Map<String, CommandExecutor> commandExecutorMap = new Hashtable<String, CommandExecutor>();
        private Lock mapLock = new ReentrantLock();
        private Thread timeoutThread;

        public CommandServiceImpl() {
            // init
            timeoutThread = new Thread(CommandServiceImpl.class.getName()) {
                @Override
                public void run() {
                    // when thread is not destroy
                    while (timeoutThread == this && !this.isInterrupted()) {
                        if (commandExecutorMap != null && commandExecutorMap.size() > 0) {
                            try {
                                mapLock.lock();
                                Collection<CommandExecutor> commandExecutors = commandExecutorMap.values();
                                for (CommandExecutor executor : commandExecutors) {
                                    // kill service process
                                    if (executor.isTimeOut()) {
                                        android.os.Process.killProcess(android.os.Process.myPid());
                                    }
                                    if (timeoutThread != this && this.isInterrupted()) {
                                        break;
                                    }
                                }
                            } finally {
                                mapLock.unlock();
                            }
                        }
                        // sleep 10 second
                        Tools.sleepIgnoreInterrupt(10);
                    }
                }
            };
            timeoutThread.setDaemon(true);
            timeoutThread.start();
        }

        /**
         * destroy
         */
        protected void destroy() {
            if (timeoutThread != null) {
                timeoutThread.interrupt();
                timeoutThread = null;
            }

            try {
                mapLock.lock();
                commandExecutorMap.clear();
                commandExecutorMap = null;
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mapLock.unlock();
            }
        }

        /**
         * run command
         *
         * @param id
         * @param timeout
         * @param params
         * @return
         * @throws RemoteException
         */
        @Override
        public String command(String id, int timeout, String params) throws RemoteException {
            CommandExecutor executor = commandExecutorMap.get(id);
            if (executor == null) {
                try {
                    mapLock.lock();
                    executor = commandExecutorMap.get(id);
                    if (executor == null) {
                        executor = CommandExecutor.create(timeout, params);
                        commandExecutorMap.put(id, executor);
                    }
                } finally {
                    mapLock.unlock();
                }
            }

            // get result
            String result = executor.getResult();

            try {
                mapLock.lock();
                commandExecutorMap.remove(id);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mapLock.unlock();
            }
            return result;
        }

        /**
         * cancel command
         *
         * @param id command id
         * @throws RemoteException
         */
        @Override
        public void cancel(String id) throws RemoteException {
            CommandExecutor executor = commandExecutorMap.get(id);
            if (executor != null) {
                try {
                    mapLock.lock();
                    commandExecutorMap.remove(id);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mapLock.unlock();
                }
                executor.destroy();
            }
        }

        /**
         * get task count
         *
         * @return
         * @throws RemoteException
         */
        @Override
        public int getTaskCount() throws RemoteException {
            if (commandExecutorMap == null)
                return 0;
            return commandExecutorMap.size();
        }
    }
}
