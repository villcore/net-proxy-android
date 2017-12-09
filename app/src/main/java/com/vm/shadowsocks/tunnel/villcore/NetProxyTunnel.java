package com.vm.shadowsocks.tunnel.villcore;

import com.vm.shadowsocks.tunnel.Tunnel;
import com.vm.shadowsocks.tunnel.shadowsocks.CryptFactory;
import com.vm.shadowsocks.tunnel.shadowsocks.ICrypt;
import com.vm.shadowsocks.tunnel.shadowsocks.ShadowsocksConfig;

import java.nio.ByteBuffer;
import java.nio.channels.Selector;

import static com.vm.shadowsocks.tunnel.shadowsocks.AesCrypt.CIPHER_AES_128_CFB;

/**
 * Created by villcore on 2017/12/9.
 */

public class NetProxyTunnel extends Tunnel {

    private ICrypt m_Encryptor;
    private NetProxyConfig m_Config;
    private boolean m_TunnelEstablished;

    public NetProxyTunnel(NetProxyConfig config, Selector selector) throws Exception {
        super(config.ServerAddress, selector);
        m_Config = config;
        m_Encryptor = CryptFactory.get(CIPHER_AES_128_CFB, m_Config.password);

    }

    @Override
    protected void onConnected(ByteBuffer buffer) throws Exception {

        buffer.clear();
        // https://shadowsocks.org/en/spec/protocol.html

        buffer.put((byte) 0x03);//domain
        byte[] domainBytes = m_DestAddress.getHostName().getBytes();

        buffer.put((byte) domainBytes.length);//domain length;

        buffer.put(domainBytes);
        buffer.putShort((short) m_DestAddress.getPort());
        buffer.flip();

        byte[] _header = new byte[buffer.limit()];
        buffer.get(_header);

        buffer.clear();
        buffer.put(m_Encryptor.encrypt(_header));
        buffer.flip();

        if (write(buffer, true)) {
            m_TunnelEstablished = true;
            onTunnelEstablished();
        } else {
            m_TunnelEstablished = true;
            this.beginReceive();
        }
    }

    @Override
    protected boolean isTunnelEstablished() {
        return m_TunnelEstablished;
    }

    @Override
    protected void beforeSend(ByteBuffer buffer) throws Exception {

        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);

        byte[] newbytes = m_Encryptor.encrypt(bytes);

        buffer.clear();
        buffer.put(newbytes);
        buffer.flip();
    }

    @Override
    protected void afterReceived(ByteBuffer buffer) throws Exception {
        byte[] bytes = new byte[buffer.limit()];
        buffer.get(bytes);
        byte[] newbytes = m_Encryptor.decrypt(bytes);
        String s = new String(newbytes);
        buffer.clear();
        buffer.put(newbytes);
        buffer.flip();
    }

    @Override
    protected void onDispose() {
        m_Config = null;
        m_Encryptor = null;
    }
}
