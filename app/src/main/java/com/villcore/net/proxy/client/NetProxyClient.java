package com.villcore.net.proxy.client;

import com.villcore.net.proxy.util.ConfigUtil;

import java.util.Properties;

/**
 * create by WangTao on 2019/1/25
 */
public class NetProxyClient {

    public static final String LISTEN_PORT_KEY = "client.listen.port";
    public static final String REMOTE_ADDR_KEY = "client.remote.addr";
    public static final String REMOTE_PORT_KEY = "client.remote.port";
    public static final String PASSWORD_KEY = "client.password";

    private final int listenPort;
    private final String remoteAddr;
    private final int remotePort;
    private final String password;

    private SocketServer socketServer;

    public NetProxyClient(Properties prop) {
        // TODO parser argument form config
        this.listenPort = ConfigUtil.getInt(prop, LISTEN_PORT_KEY);
        this.remoteAddr = ConfigUtil.get(prop, REMOTE_ADDR_KEY);
        this.remotePort = ConfigUtil.getInt(prop, REMOTE_PORT_KEY);
        this.password = ConfigUtil.get(prop, PASSWORD_KEY);
    }

    public void startup() {
        // TODO start socket server listen
        socketServer = new SocketServer(listenPort, remoteAddr, remotePort, password);
        socketServer.startup();
    }

    public void shutdown() {
        socketServer.shutdown();
    }

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: <client_config>");
            Runtime.getRuntime().halt(1);
            return;
        }

        final NetProxyClient client = new NetProxyClient(ConfigUtil.loadConfig(args[0]));
        client.startup();

        // add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                client.shutdown();
            }
        }));
    }
}
