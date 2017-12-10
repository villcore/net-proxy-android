package com.vm.shadowsocks.tunnel.villcore.bio.util;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class URL {
    private String scheme;
    private String host;
    private String IP;
    private String resource;
    private int port;

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String schemes) {
        this.scheme = schemes;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getIP() {
        java.security.Security.setProperty("networkaddress.cache.ttl", "30");
        try {
            this.IP = InetAddress.getByName(this.host).getHostAddress();
        } catch (UnknownHostException e) {
            return "";
        }
        return this.IP;
    }

    public String getResource() {
        return resource;
    }

    public String toString() {
        return "scheme:" + this.getScheme() + "\nhost:" + this.getHost() + "\nport:" + this.getPort() + "\nIP:" + this.getIP() + "\nResource:" + this.getResource();
    }

    public URL(String url) {
        String scheme = "http";
        String host = "";
        String port = "80";
        int index;
        // 抽取host
        index = url.indexOf("//");
        if (index != -1)
            scheme = url.substring(0, index - 1);
        host = url.substring(index + 2);
        index = host.indexOf('/');
        if (index != -1) {
            this.resource = host.substring(index);
            host = host.substring(0, index);
        }
        index = host.indexOf(':');
        if (index != -1) {
            port = host.substring(index + 1);
            host = host.substring(0, index);
        }
        this.scheme = scheme;
        this.host = host;
        this.port = Integer.parseInt(port);
    }
}