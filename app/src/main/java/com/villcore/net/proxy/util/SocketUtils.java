package com.villcore.net.proxy.util;

import com.vm.shadowsocks.core.LocalVpnService;

import java.lang.reflect.Method;
import java.nio.channels.SocketChannel;

import io.netty.channel.Channel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class SocketUtils {

    private static Method getJavaSocketChannelMethod;

    static {
        try {
            getJavaSocketChannelMethod = NioSocketChannel.class.getDeclaredMethod("javaChannel");
            getJavaSocketChannelMethod.setAccessible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    public static void protectChannel(Channel channel) {
        try {
            NioSocketChannel nioSocketChannel = (NioSocketChannel) channel;
            SocketChannel javaSocketChannel = (SocketChannel) getJavaSocketChannelMethod.invoke(nioSocketChannel);
            LocalVpnService.Instance.protect(javaSocketChannel.socket());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
