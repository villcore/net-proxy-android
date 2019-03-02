package com.villcore.net.proxy.client;

import java.io.Serializable;
import java.util.Objects;

public class HostPort implements Serializable {

    public static final HostPort INVALID_HOST_PORT = new HostPort();

    public static boolean isValid(HostPort hostPort) {
        return hostPort != INVALID_HOST_PORT;
    }

    private String host;
    private int port;

    public HostPort() {}

    public HostPort(String host, int port) {
        this.host = host;
        this.port = port;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HostPort hostPort = (HostPort) o;
        return port == hostPort.port &&
                Objects.equals(host, hostPort.host);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, port);
    }

    @Override
    public String toString() {
        return "HostPort{" +
                "host='" + host + '\'' +
                ", port=" + port +
                '}';
    }
}