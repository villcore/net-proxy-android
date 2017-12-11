package com.vm.shadowsocks.tunnel.villcore.bio.common;

import android.util.Log;

import com.vm.shadowsocks.tunnel.villcore.bio.handler.Handler;
import com.vm.shadowsocks.tunnel.villcore.bio.pkg2.Package;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import java.io.*;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by villcore on 2017/7/18.
 */
public class BytesToPackageTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(PackageToBytesTask.class);
    private static final String TAG = BytesToPackageTask.class.getSimpleName();

    private volatile boolean running = false;
    private Map<String, Handler> handlers = new LinkedHashMap<>();

    private Connection connection;
    private InputStream inputStream;
    private OutputStream outputStream;

    public BytesToPackageTask(Connection connection, InputStream inputStream, OutputStream outputStream) {
        this.connection = connection;
        this.inputStream = inputStream;
        this.outputStream = outputStream;
    }

    public void addHandler(String name, Handler handler) {
        handlers.put(name, handler);
    }

    public void removeHandler(String name) {
        handlers.remove(name);
    }

    @Override
    public void run() {
        while (running) {
            if(!connection.socket.isConnected() || !connection.socket2.isConnected()) {
                try {
                    if(!running) {
                        break;
                    }
                    Thread.sleep(500L);
                    Log.d(TAG, "socket not connected ...");
                    continue;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            try {
                Package pkg = new Package();
                pkg.readPackageWithoutHeader(inputStream);

                //LOG.debug("encryt read pkg...");
                //LOG.debug("\n>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n");
//                LOG.debug("read to encrypting request = >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>\n{}\n", new String(pkg.getBody()));
                //LOG.debug("origin size = {}, header = {}, body = {}", pkg.getSize(), pkg.getHeaderLen(), pkg.getBodyLen());

                Log.d(TAG, "send content = " + new String(pkg.getBody()));

                for (Map.Entry<String, Handler> entry : handlers.entrySet()) {
                    pkg = entry.getValue().handle(pkg);
                    //LOG.debug("encrypt [{}] handle package size = {}, header = {}, body = {}", new Object[]{entry.getKey(), pkg.getSize(), pkg.getHeaderLen(), pkg.getBodyLen()});
                }

                Log.d(TAG, "need write to os ... = ");
                pkg.writePackageWithHeader(outputStream);
                //LOG.debug("encryt write pkg ...");
                Log.d(TAG, "write to os  finished ... = ");

            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
                stop();
            }
        }
        close();
    }

    public void start() {
        running = true;
        new Thread(this).start();
    }

    public void stop() {
        running = false;
    }

    public void close() {
        handlers.clear();
        connection.close();
    }
}
