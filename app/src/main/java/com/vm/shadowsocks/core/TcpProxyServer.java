package com.vm.shadowsocks.core;

import android.util.Log;

import com.vm.shadowsocks.tcpip.CommonMethods;
import com.vm.shadowsocks.tunnel.Tunnel;
import com.vm.shadowsocks.tunnel.villcore.bio.client.ClientConnection;
import com.vm.shadowsocks.tunnel.villcore.bio.common.Connection;
import com.vm.shadowsocks.tunnel.villcore.bio.util.SocketUtil;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * NIO ServerSocket
 */
public class TcpProxyServer implements Runnable {
    private static final String TAG = TcpProxyServer.class.getSimpleName();

    //是否停止
    public volatile boolean stopped;

    //端口
    public short Port;

    Thread m_ServerThread;

    private final List<Connection> connections = Collections.synchronizedList(new LinkedList<Connection>());

    public TcpProxyServer(int port) throws IOException {
        this.Port = (short) port;
        System.out.printf("AsyncTcpServer listen on %d success.\n", this.Port & 0xFFFF);
    }

    public void start() {
        m_ServerThread = new Thread(this);
        m_ServerThread.setName("TcpProxyServerThread");
        m_ServerThread.start();
    }

    public void stop() {
        this.stopped = true;
    }

    @Override
    public void run() {

        //int listenPort = Integer.valueOf("50082");
        String remoteAddr = "207.246.98.97";
        int remotePort = Integer.valueOf("60082");

        InetSocketAddress remoteAddress = new InetSocketAddress(remoteAddr, remotePort);

        ServerSocket serverSocket = null;

        final ServerSocket finalServerSocket = serverSocket;

        try {
            serverSocket = new ServerSocket(Port & 0xFFFF);
            while (!stopped) {
                Socket localSocket = serverSocket.accept();
                Socket remoteSocket = SocketUtil.connect(remoteAddress);
                if(remoteSocket == null) {
                    Log.d(TAG, String.format("can not connect remote server [%s:%s] ...", remoteAddr, remotePort));
                    localSocket.close();
                    continue;
                }

                SocketUtil.configSocket(localSocket);
                SocketUtil.configSocket(remoteSocket);

                ClientConnection connection = new ClientConnection(this, localSocket, remoteSocket, "villcore");
                connection.start();
                connections.add(connection);
            }

            if(finalServerSocket != null) {
                try {
                    finalServerSocket.close();
                } catch (IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            }

            for(Connection connection : connections) {
                connection.stop();
            }

        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void remoteConnection(Connection connection) {
        connections.remove(connection);
    }
}
