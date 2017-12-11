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

    public static final String HTTPS_CONNECT =
            "CONNECT %s:443 HTTP/1.1\r\n" +
                    "Host: %s:443\r\n" +
                    "Proxy-Connection: keep-alive\r\n" +
                    "\r\n\r\n";

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
        String remoteAddr = "192.168.8.101";
        int remotePort = Integer.valueOf("60082");

        InetSocketAddress remoteAddress = new InetSocketAddress(remoteAddr, remotePort);

        ServerSocket serverSocket = null;

        final ServerSocket finalServerSocket = serverSocket;

        try {
            serverSocket = new ServerSocket(Port & 0xFFFF);

            while (!stopped) {
                Socket localSocket = serverSocket.accept();
                Log.d(TAG, "accept connection ..." + localSocket.getRemoteSocketAddress().toString());

                Socket remoteSocket = SocketUtil.connectWithoutVPN(remoteAddress);

                if (remoteSocket == null) {
                    Log.d(TAG, String.format("can not connect remote server [%s:%s] ...", remoteAddr, remotePort));
                    localSocket.close();
                    continue;
                }

                Connection connection = new ClientConnection(this, localSocket, remoteSocket, "villcore");
                InetSocketAddress destAddress = getDestAddress(localSocket);
                Log.d(TAG, "dest addr = " + destAddress.toString());
                Log.d(TAG, "dest port = " + destAddress.getPort());

                if (destAddress.getPort() == 443) {
                    //https 需要发送connect请求
                    String httpsConnect = String.format(HTTPS_CONNECT, destAddress.getHostString(), destAddress.getHostString());
                    Log.d(TAG, "----construct https connect = " + httpsConnect);
                    connection.setHttps(true);
                    connection.setHttpsConnectReq(httpsConnect);
                }
                connection.start();
                connections.add(connection);
            }

            if (finalServerSocket != null) {
                try {
                    finalServerSocket.close();
                } catch (IOException e) {
                    Log.d(TAG, e.getMessage());
                }
            }

            for (Connection connection : connections) {
                connection.stop();
            }

        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
    }

    public void remoteConnection(Connection connection) {
        connections.remove(connection);
    }

    private InetSocketAddress getDestAddress(Socket socket) {
        short portKey = (short) socket.getPort();
        NatSession session = NatSessionManager.getSession(portKey);

        if (session != null) {
            if (ProxyConfig.Instance.needProxy(session.RemoteHost, session.RemoteIP)) {
                if (ProxyConfig.IS_DEBUG)
                    System.out.printf("%d/%d:[PROXY] %s=>%s:%d\n", NatSessionManager.getSessionCount(), Tunnel.SessionCount, session.RemoteHost, CommonMethods.ipIntToString(session.RemoteIP), session.RemotePort & 0xFFFF);
                return InetSocketAddress.createUnresolved(session.RemoteHost, session.RemotePort & 0xFFFF);
            } else {
                return new InetSocketAddress(socket.getInetAddress(), session.RemotePort & 0xFFFF);
            }
        }
        return null;
    }


}
