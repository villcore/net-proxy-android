package com.vm.shadowsocks.tunnel.villcore.bio.common;

import com.vm.shadowsocks.core.TcpProxyServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by villcore on 2017/7/17.
 */
public abstract class Connection {
    private static final Logger LOG = LoggerFactory.getLogger(Connection.class);

    private TcpProxyServer tcpProxyServer;

    protected Socket socket;
    protected Socket socket2;

    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected InputStream inputStream2;
    protected OutputStream outputStream2;

    protected BytesToPackageTask bytesToPackageTask;
    protected PackageToBytesTask packageToBytesTask;

    public Connection(TcpProxyServer tcpProxyServer, Socket socket, Socket socket2) {
        this.tcpProxyServer = tcpProxyServer;
        this.socket = socket;
        this.socket2 = socket2;
    }

    public void init() throws Exception {
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        inputStream2 = socket2.getInputStream();
        outputStream2 = socket2.getOutputStream();

        initTask(inputStream, outputStream, inputStream2, outputStream2, bytesToPackageTask, packageToBytesTask, this);
    }

    public abstract void initTask(
            InputStream inputStream,
            OutputStream outputStream,
            InputStream inputStream2,
            OutputStream outputStream2,
            BytesToPackageTask bytesToPackageTask,
            PackageToBytesTask packageToBytesTask,
            Connection connection) throws Exception;

    //start
    public void start() {
        try {
            init();
            LOG.debug("init finished...");
            bytesToPackageTask.start();
            packageToBytesTask.start();
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            stop();
            close();
            tcpProxyServer.remoteConnection(this);
        }
    }

    public void stop() {
        if(bytesToPackageTask != null) {
            bytesToPackageTask.stop();
        }

        if(packageToBytesTask != null){
            packageToBytesTask.stop();
        }
    }

    protected void closeInputStream(InputStream inputStream) {
        try {
            if (inputStream != null) {
                inputStream.close();
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    protected void closeOutputStream(OutputStream outputStream) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    public void close() {
        closeInputStream(inputStream);
        closeInputStream(inputStream2);

        closeOutputStream(outputStream);
        closeOutputStream(outputStream2);
        closeSocket(socket);
        closeSocket(socket2);
    }

    protected void closeSocket(Socket socket) {
        if (socket != null && !socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException e) {
                LOG.error(e.getMessage(), e);
            }
        }
    }
}
