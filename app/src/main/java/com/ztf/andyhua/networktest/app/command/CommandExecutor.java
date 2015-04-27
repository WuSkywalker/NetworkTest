package com.ztf.andyhua.networktest.app.command;

import com.ztf.andyhua.networktest.app.utils.Tools;

import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by AndyHua on 2015/4/25.
 */
public class CommandExecutor {
    private static final String TAG = CommandExecutor.class.getSimpleName();

    private static final String BREAK_LINE = "\n";
    private static final int BUFFER_LENGTH = 128;
    private static final byte[] BUFFER = new byte[BUFFER_LENGTH];
    private static final Lock LOCK = new ReentrantLock();

    // process builder

    private static final ProcessBuilder PRC = new ProcessBuilder();

    // class value
    private final Process process;
    private final int timeout;
    private final long startTime;

    // result
    private final StringBuilder result;

    // stream
    private InputStream inStream;
    private InputStream errStream;
    private OutputStream outStream;
    private InputStreamReader inStreamReader = null;
    private BufferedReader inStreamBuffer = null;

    // is end
    private boolean isDone;

    private CommandExecutor(Process process, int timeout) {
        // init
        this.process = process;
        this.timeout = timeout;
        this.startTime = System.currentTimeMillis();

        if (inStream != null) {
            inStreamReader = new InputStreamReader(inStream);
            inStreamBuffer = new BufferedReader(inStreamReader, BUFFER_LENGTH);
        }

        result = new StringBuilder();

        if (inStream != null) {
            // start read thread
            Thread processThread = new Thread(TAG) {
                @Override
                public void run() {
                    startRead();
                }
            };

            processThread.setDaemon(true);
            processThread.start();
        }
    }

    /**
     * read
     */
    private void read() {
        String str;
        // read data
        try {
            while ((str = inStreamBuffer.readLine()) != null) {
                result.append(str);
                result.append(BREAK_LINE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void startRead() {
        // while to end
        while (true) {
            try {
                process.exitValue();
                // read last
                read();
                break;
            } catch (IllegalThreadStateException e) {
                read();
                //e.printStackTrace();
            }
            Tools.sleepIgnoreInterrupt(50);
        }

        // read end
        int len;
        if (inStream != null) {
            try {
                while (true) {
                    len = inStream.read(BUFFER);
                    if (len <= 0) {
                        break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // close destroy and done the read
        close();
        destroy();

        isDone = true;
    }

    /**
     * close the io
     */
    private void close() {
        // out
        if (outStream != null) {
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outStream = null;
        }

        // error
        if (errStream != null) {
            try {
                errStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            errStream = null;
        }

        // in
        if (inStream != null) {
            try {
                inStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inStream = null;
        }

        if (inStreamReader != null) {
            try {
                inStreamReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inStreamReader = null;
        }

        if (inStreamBuffer != null) {
            try {
                inStreamBuffer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            inStreamBuffer = null;
        }
    }

    /**
     * @param timeout
     * @param param   eg:"/system/bin/ping -c 4 -s 100 www.baidu.com"
     * @return
     */
    protected static CommandExecutor create(int timeout, String param) {
        String[] params = param.split(" ");
        CommandExecutor processModel = null;
        try {
            LOCK.lock();
            Process process = PRC.command(params).redirectErrorStream(true).start();
            processModel = new CommandExecutor(process, timeout);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // sleep 10 to create next
            Tools.sleepIgnoreInterrupt(10);
            LOCK.unlock();
        }

        return processModel;
    }

    /**
     * get is time out
     *
     * @return
     */
    protected boolean isTimeOut() {
        return ((System.currentTimeMillis() - startTime) >= timeout);
    }

    /**
     * get result
     *
     * @return
     */
    protected String getResult() {
        // until read end
        while (!isDone) {
            Tools.sleepIgnoreInterrupt(500);
        }

        // get return value
        if (result.length() == 0) {
            return null;
        } else {
            return result.toString();
        }
    }

    /**
     *
     */
    protected void destroy() {
        String str = process.toString();
        try {
            int i = str.indexOf("=") + 1;
            int j = str.indexOf("]");
            str = str.substring(i, j);
            int pid = Integer.parseInt(str);
            try {
                android.os.Process.killProcess(pid);
            } catch (Exception e) {
                try {
                    process.destroy();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
