package com.vm.shadowsocks.core;

import com.villcore.net.proxy.client.NetProxyClient;

import java.util.Properties;

/**
 * NIO ServerSocket
 */
public class TcpProxyServerV2 implements Runnable {
    private static final String TAG = TcpProxyServerV2.class.getSimpleName();

    public static String remoteAddr;
    public static String remotePort;
    public static String remotePassword;

    // 端口
    private short listenPort;

    //
    private Thread proxyClientThread;

    //
    private NetProxyClient proxyClient;

    public TcpProxyServerV2(int listenPort) {
        this.listenPort = (short) listenPort;
        System.out.printf("AsyncTcpServer listen on %d success.\n", this.listenPort & 0xFFFF);
    }

    public void start() {
        proxyClientThread = new Thread(this);
        proxyClientThread.setName("TcpProxyServerThread");
        proxyClientThread.start();
    }

    public void stop() {
        proxyClient.shutdown();
    }

    @Override
    public void run() {
        Properties properties = new Properties();
        properties.put(NetProxyClient.LISTEN_PORT_KEY, listenPort);
        properties.put(NetProxyClient.REMOTE_ADDR_KEY, remoteAddr);
        properties.put(NetProxyClient.REMOTE_PORT_KEY, remotePort);
        properties.put(NetProxyClient.PASSWORD_KEY, remotePassword);
        proxyClient = new NetProxyClient(properties);
        proxyClient.startup();

        /*
        ServerSocket serverSocket = null;

        final ServerSocket finalServerSocket = serverSocket;

        try {
            serverSocket = new ServerSocket(listenPort & 0xFFFF);

            while (!stopped) {
                Socket localSocket = serverSocket.accept();
                Log.d(TAG, "accept connection ..." + localSocket.getRemoteSocketAddress().toString());

                Log.d(TAG, "remote addr = " + remoteAddr + ", remote port = " + remotePort + ", remotePassword = " + remotePassword);

                Socket remoteSocket = SocketUtil.connectWithoutVPN(new InetSocketAddress(remoteAddr, Integer.valueOf(remotePort)));

                if (remoteSocket == null) {
                    Log.d(TAG, String.format("can not connect remote server [%s:%s] ...", remoteAddr, remotePort));
                    localSocket.close();
                    continue;
                }

                Connection connection = new ClientConnection(this, localSocket, remoteSocket, remotePassword);
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
        */
    }
}
