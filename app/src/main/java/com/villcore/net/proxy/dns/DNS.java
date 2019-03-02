package com.villcore.net.proxy.dns;

import com.villcore.net.proxy.client.HostPort;

import javax.net.SocketFactory;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;

public class DNS {

    private static final Map<String, Boolean> ADDRESS_ACCESSABLITY = new LinkedHashMap<String, Boolean>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<String, Boolean> eldest) {
            if (this.size() >= 1000) {
                return true;
            }
            return false;
        }
    };

    public static boolean isAccessable(HostPort hostPort) {
        return isAccessable(hostPort.getHost(), hostPort.getPort());
    }

    public static boolean isAccessable(String address, int port) {
        Boolean accessablity = getAccessablity(address);
        if (accessablity == Boolean.TRUE) {
            return true;
        }

        if (accessablity == null) {
            accessablity = connect(address, port);
            updateAccessablity(address, Boolean.valueOf(accessablity));
        }
        return accessablity;
    }

    private static boolean connect(String address, int port) {
        try (Socket socket = SocketFactory.getDefault().createSocket()) {
            socket.setReuseAddress(true);
            socket.connect(new InetSocketAddress(address, port), 100);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    private static synchronized void updateAccessablity(String address, Boolean accessablity) {
        ADDRESS_ACCESSABLITY.put(address, accessablity);
    }

    private static synchronized Boolean getAccessablity(String address) {
        return ADDRESS_ACCESSABLITY.get(address);
    }

    public static void main(String[] args) {
        for (int i = 0; i < 100; i++) {
            System.out.println(isAccessable("www.youtube.com", 80));
        }
    }
}
