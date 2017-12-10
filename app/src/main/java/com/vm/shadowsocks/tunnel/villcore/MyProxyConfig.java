package com.vm.shadowsocks.tunnel.villcore;

import android.net.Uri;
import android.util.Base64;

import com.vm.shadowsocks.tunnel.Config;
import com.vm.shadowsocks.tunnel.shadowsocks.CryptFactory;

import java.net.InetSocketAddress;

public class MyProxyConfig extends Config {
    public String Password;

    public static MyProxyConfig parse(String proxyInfo) throws Exception {
        MyProxyConfig config = new MyProxyConfig();

        proxyInfo = proxyInfo.replace("my://", "");
        String[] info = proxyInfo.split("@");
        String password = info[0];
        String remoteAddrAndPort = info[1];

        String remoteAddr = remoteAddrAndPort.split(":")[0];
        String remotePort = remoteAddrAndPort.split(":")[1];

        config.Password = password;
        config.ServerAddress = new InetSocketAddress(remoteAddr, Integer.valueOf(remotePort));

        System.out.println("password = " + password);
        System.out.println("remoteAddr = " + remoteAddr);
        System.out.println("remotePort = " + remotePort);

        return config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MyProxyConfig that = (MyProxyConfig) o;

        return Password != null ? Password.equals(that.Password) : that.Password == null;
    }

    @Override
    public int hashCode() {
        return Password != null ? Password.hashCode() : 0;
    }
}
