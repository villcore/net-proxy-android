package com.vm.shadowsocks.tunnel.v1;

import android.util.Log;

import com.vm.shadowsocks.tunnel.Config;
import com.vm.shadowsocks.tunnel.Tunnel;
import com.vm.shadowsocks.tunnel.v1.crypt.Crypt;
import com.vm.shadowsocks.tunnel.v1.pkg.Package;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.Selector;

/**
 * Created by villcore on 2017/12/9.
 */

public class NetProxyTunnel extends Tunnel {
    private static final String TAG = NetProxyTunnel.class.getSimpleName();

    private Crypt crypt;
    private Config m_Config;
    private boolean m_TunnelEstablished;

    private boolean ivSend = false;

    private static InetSocketAddress remoteAddr = new InetSocketAddress(NetProxyConfig.remoteAddr, Integer.valueOf(NetProxyConfig.remotePort));

    public NetProxyTunnel(Config config, Selector selector) throws Exception {
        super(remoteAddr, selector);
        m_Config = config;

        crypt = new Crypt();
        byte[] key = crypt.generateKey("villcore");
        byte[] iv = crypt.generateIv();

        crypt.setIv(iv);
        crypt.setKey(key);
        crypt.initDecrypt();
        crypt.initEncrypt();
    }

    @Override
    protected void onConnected(ByteBuffer buffer) throws Exception {
        Log.d(TAG, "connect ...");
        onTunnelEstablished();
    }

    @Override
    protected boolean isTunnelEstablished() {
        return m_TunnelEstablished;
    }

    @Override
    protected void beforeSend(ByteBuffer buffer) throws Exception {
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);

        if(!ivSend) {
            byte[] iv = crypt.getIv();
            byte[] encryptHeader;
            byte[] encryptBody = crypt.encrypt(bytes);

            ByteBuffer tmp = ByteBuffer.wrap(new byte[4 + iv.length]);
            tmp.putInt(iv.length);
            tmp.put(iv);

            encryptHeader = tmp.array();

            Package pkg = new Package();
            pkg.valueOf(encryptHeader, encryptBody);

            buffer.clear();
            buffer.put(pkg.toBytes());
            buffer.flip();
            ivSend = true;
        } else {
            byte[] encryptHeader = new byte[0];
            byte[] encryptBody = crypt.encrypt(bytes);

            Package pkg = new Package();
            pkg.valueOf(encryptHeader, encryptBody);

            buffer.clear();
            buffer.put(pkg.toBytes());
            buffer.flip();
        }
    }

    @Override
    protected void afterReceived(ByteBuffer buffer) throws Exception {
//        int pos = buffer.position();
//        byte[] bytes = new byte[buffer.limit()];
//        buffer.get(bytes);
//
//        buffer.position(pos);
//        int len = buffer.getInt();
//        int headerLen = buffer.getInt();
//        int bodyLen = buffer.getInt();
//
//        byte[] body = new byte[bodyLen];
//        System.arraycopy(bytes, 4 + 4 + 4 + headerLen, body, 0, bodyLen);
//        Log.d(TAG, "recv, " + bytes.length);
//        byte[] decrypt = crypt.decrypt(body);
//
//        Log.d(TAG, "recv string =  " + new String(decrypt));

        buffer.clear();
        buffer.put(Html404.RESP.getBytes());
        buffer.flip();
    }

    @Override
    protected void onDispose() {
        m_Config = null;
        crypt= null;
    }
}
