//package com.vm.shadowsocks.tunnel.villcore.bio.client;
//
//import com.vm.shadowsocks.tunnel.villcore.bio.common.Connection;
//import com.vm.shadowsocks.tunnel.villcore.bio.util.SocketUtil;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import java.io.IOException;
//import java.net.InetSocketAddress;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.LinkedList;
//import java.util.List;
//
///**
// * Created by villcore on 2017/7/17.
// *
// */
//public class Client {
//    private static final Logger LOG = LoggerFactory.getLogger(Client.class);
//
//    public static void start() throws IOException {
//        final List<Connection> connections = new LinkedList<>();
//
//        int listenPort = Integer.valueOf("50082");
//        String remoteAddr = "207.246.98.97";
//        int remotePort = Integer.valueOf("60082");
//
//        InetSocketAddress remoteAddress = new InetSocketAddress(remoteAddr, remotePort);
//
//        ServerSocket serverSocket = null;
//
//        final ServerSocket finalServerSocket = serverSocket;
//
//        Runtime.getRuntime().addShutdownHook(new Thread(){
//            @Override
//            public void run() {
//                if(finalServerSocket != null) {
//                    try {
//                        finalServerSocket.close();
//                    } catch (IOException e) {
//                        LOG.error(e.getMessage(), e);
//                    }
//                }
//
//                for(Connection connection : connections) {
//                    connection.stop();
//                }
//            }
//        });
//
//        try {
//            serverSocket = new ServerSocket(listenPort);
//            while (true) {
//                Socket localSocket = serverSocket.accept();
//                Socket remoteSocket = SocketUtil.connect(remoteAddress);
//                if(remoteSocket == null) {
//                    LOG.info("can not connect remote server [{}:{}] ...", remoteAddr, remotePort);
//                    localSocket.close();
//                    continue;
//                }
//
//                SocketUtil.configSocket(localSocket);
//                SocketUtil.configSocket(remoteSocket);
//
//                ClientConnection connection = new ClientConnection(localSocket, remoteSocket, "villcore");
//                connection.start();
//                connections.add(connection);
//            }
//        } catch (IOException e) {
//            LOG.error(e.getMessage(), e);
//        }
//    }
//
//
//    public static void start(int listenPort) throws IOException {
//        final List<Connection> connections = new LinkedList<>();
//
//        //int listenPort = Integer.valueOf("50082");
//        String remoteAddr = "207.246.98.97";
//        int remotePort = Integer.valueOf("60082");
//
//        InetSocketAddress remoteAddress = new InetSocketAddress(remoteAddr, remotePort);
//
//        ServerSocket serverSocket = null;
//
//        final ServerSocket finalServerSocket = serverSocket;
//
//        Runtime.getRuntime().addShutdownHook(new Thread(){
//            @Override
//            public void run() {
//                if(finalServerSocket != null) {
//                    try {
//                        finalServerSocket.close();
//                    } catch (IOException e) {
//                        LOG.error(e.getMessage(), e);
//                    }
//                }
//
//                for(Connection connection : connections) {
//                    connection.stop();
//                }
//            }
//        });
//
//        try {
//            serverSocket = new ServerSocket(listenPort);
//            while (true) {
//                Socket localSocket = serverSocket.accept();
//                Socket remoteSocket = SocketUtil.connect(remoteAddress);
//                if(remoteSocket == null) {
//                    LOG.info("can not connect remote server [{}:{}] ...", remoteAddr, remotePort);
//                    localSocket.close();
//                    continue;
//                }
//
//                SocketUtil.configSocket(localSocket);
//                SocketUtil.configSocket(remoteSocket);
//
//                ClientConnection connection = new ClientConnection(localSocket, remoteSocket, "villcore");
//                connection.start();
//                connections.add(connection);
//            }
//        } catch (IOException e) {
//            LOG.error(e.getMessage(), e);
//        }
//    }
//}
