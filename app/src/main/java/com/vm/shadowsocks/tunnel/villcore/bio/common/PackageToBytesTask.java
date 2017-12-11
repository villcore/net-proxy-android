package com.vm.shadowsocks.tunnel.villcore.bio.common;

import com.vm.shadowsocks.tunnel.villcore.bio.handler.Handler;
import com.vm.shadowsocks.tunnel.villcore.bio.pkg2.Package;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by villcore on 2017/7/18.
 */
public class PackageToBytesTask implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(PackageToBytesTask.class);
    private static final String TAG = PackageToBytesTask.class.getSimpleName();

    private volatile boolean running = false;
    private Map<String, Handler> handlers = new LinkedHashMap<>();

    private Connection connection;
    private InputStream inputStream;
    private OutputStream outputStream;

    public PackageToBytesTask(Connection connection, InputStream inputStream, OutputStream outputStream) {
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
            try {
                Package pkg = new Package();
                pkg.readPackageWithHeader(inputStream);

                for (Map.Entry<String, Handler> entry : handlers.entrySet()) {
                    pkg = entry.getValue().handle(pkg);
                }

                String resp2 = new String(pkg.getBody(), "utf-8");
                //Log.d(TAG, "get resp = " + resp2);
                if(connection.https && !connection.connectRespGet) {
                    String resp = new String(pkg.getBody(), "utf-8");
                    if(resp.contains("Connection Established")) {
                        connection.connectRespGet = true;
                    }
                } else {
                    pkg.writePackageWithoutHeader(outputStream);
                }
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
