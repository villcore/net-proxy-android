package com.vm.shadowsocks.tunnel;

import android.util.Log;

import com.vm.shadowsocks.tunnel.villcore.NetProxyTunnel;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class RawTunnel extends Tunnel {
    private static final String TAG = RawTunnel.class.getSimpleName();

    public RawTunnel(InetSocketAddress serverAddress, Selector selector) throws Exception {
        super(serverAddress, selector);
    }

    public RawTunnel(SocketChannel innerChannel, Selector selector) {
        super(innerChannel, selector);
    }

    @Override
    protected void onConnected(ByteBuffer buffer) throws Exception {
        onTunnelEstablished();
        Log.d(TAG, ">>>" + "raw connected...");
    }

    @Override
    protected void beforeSend(ByteBuffer buffer) throws Exception {
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        Log.d(TAG, ">>>" + new String(bytes));
    }

    @Override
    protected void afterReceived(ByteBuffer buffer) throws Exception {

    }

    @Override
    protected boolean isTunnelEstablished() {
        return true;
    }

    @Override
    protected void onDispose() {
    }

}
