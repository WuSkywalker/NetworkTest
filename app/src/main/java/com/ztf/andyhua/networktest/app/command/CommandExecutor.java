package com.ztf.andyhua.networktest.app.command;

import com.ztf.andyhua.networktest.app.utils.Tools;

import java.io.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by AndyHua on 2015/4/25.
 */
public class CommandExecutor {
    // TAG
    private static final String TAG = CommandExecutor.class.getSimpleName();
    // Final
    private static final String BREAK_LINE = "\n";
    private static final int BUFFER_LENGTH = 128;
    private static final byte[] BUFFER = new byte[BUFFER_LENGTH];
    private static final Lock LOCK = new ReentrantLock();
    // ProcessBuilder
    private static final ProcessBuilder PRC = new ProcessBuilder();

    // Class value
    private final Process process;
    private final int timeout;
    private final long startTime;

    // Result
    private final StringBuilder result;

    // Stream
    private InputStream inStream;
    private InputStream errStream;
    private OutputStream outStream;
    private InputStreamReader inStreamReader = null;
    private BufferedReader inStreamBuffer = null;

    // Is end
    private boolean isDone;

    /**
     * Get CommandExecutor
     *
     * @param process Process
     */
    private CommandExecutor(Process process, int timeout) {
        // Init
        this.timeout = timeout;
        this.startTime = System.currentTimeMillis();
        this.process = process;
        // Get
        outStream = process.getOutputStream();
        inStream = process.getInputStream();
        errStream = process.getErrorStream();

        // In
        if (inStream != null) {
            inStreamReader = new InputStreamReader(inStream);
            inStreamBuffer = new BufferedReader(inStreamReader, BUFFER_LENGTH);
        }

        result = new StringBuilder();

        if (inStream != null) {
            // Start read thread
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
     * Read
     */
    private void read() {
        String str;
        // Read data
        try {
            while ((str = inStreamBuffer.readLine()) != null) {
                result.append(str);
                result.append(BREAK_LINE);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Run thread
     */
    private void startRead() {
        // While to end
        while (true) {
            try {
                process.exitValue();
                //read last
                read();
                break;
            } catch (IllegalThreadStateException e) {
                read();
            }
            Tools.sleepIgnoreInterrupt(50);
        }

        // Read end
        int len;
        if (inStream != null) {
            try {
                while (true) {
                    len = inStream.read(BUFFER);
                    if (len <= 0)
                        break;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Close destroy and done the read
        close();
        destroy();

        isDone = true;

    }

    /**
     * Close
     */
    private void close() {
        // Out
        if (outStream != null) {
            try {
                outStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            outStream = null;
        }
        // Err
        if (errStream != null) {
            try {
                errStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            errStream = null;
        }
        // In
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
     * Run
     *
     * @param param param eg: "/system/bin/ping -c 4 -s 100 www.qiujuer.net"
     */
    protected static CommandExecutor create(int timeout, String param) {
        String[] params = param.split(" ");
        CommandExecutor processModel = null;
        try {
            LOCK.lock();
            Process process = PRC.command(params)
                    .redirectErrorStream(true)
                    .start();
            processModel = new CommandExecutor(process, timeout);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Sleep 10 to create next
            Tools.sleepIgnoreInterrupt(10);
            LOCK.unlock();
        }
        return processModel;
    }

    /**
     * Get is Time Out
     *
     * @return Time Out
     */
    protected boolean isTimeOut() {
        return ((System.currentTimeMillis() - startTime) >= timeout);
    }

    /**
     * Get Result
     *
     * @return Result
     */
    protected String getResult() {
        // Until read end
        while (!isDone) {
            Tools.sleepIgnoreInterrupt(500);
        }

        // Get return value
        if (result.length() == 0)
            return null;
        else
            return result.toString();
    }

    /**
     * Destroy
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
