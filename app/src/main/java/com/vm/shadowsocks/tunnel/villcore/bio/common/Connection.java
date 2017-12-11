package com.vm.shadowsocks.tunnel.villcore.bio.common;

import android.util.Log;

import com.vm.shadowsocks.core.TcpProxyServer;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

/**
 * Created by villcore on 2017/7/17.
 */
public abstract class Connection {
    private static final String TAG = Connection.class.getSimpleName();

    private TcpProxyServer tcpProxyServer;

    protected Socket socket;
    protected Socket socket2;

    protected InputStream inputStream;
    protected OutputStream outputStream;
    protected InputStream inputStream2;
    protected OutputStream outputStream2;

    protected BytesToPackageTask bytesToPackageTask;
    protected PackageToBytesTask packageToBytesTask;

    //在android端https不同于pc代理会发送CONNECT 请求,
    // 所以需要手动构建CONNECT请求, 同时在响应端将Connection Established相应抛弃
    protected boolean https;
    protected String httpsConnectReq;
    protected boolean connectReqSend;
    protected boolean connectRespGet;


    public Connection(TcpProxyServer tcpProxyServer, Socket socket, Socket socket2) {
        this.tcpProxyServer = tcpProxyServer;
        this.socket = socket;
        this.socket2 = socket2;
    }

    public void setHttps(boolean https) {
        this.https = https;
    }

    public void setHttpsConnectReq(String httpsConnectReq) {
        this.httpsConnectReq = httpsConnectReq;
    }

    public void setConnectReqSend(boolean connectReqSend) {
        this.connectReqSend = connectReqSend;
    }

    public void setConnectRespGet(boolean connectRespGet) {
        this.connectRespGet = connectRespGet;
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
            bytesToPackageTask.start();
            packageToBytesTask.start();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
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
            Log.e(TAG, e.getMessage());
        }
    }

    protected void closeOutputStream(OutputStream outputStream) {
        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
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
                Log.e(TAG, e.getMessage());
            }
        }
    }
}
