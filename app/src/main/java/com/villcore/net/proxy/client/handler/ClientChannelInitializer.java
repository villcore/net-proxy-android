package com.villcore.net.proxy.client.handler;

import com.villcore.net.proxy.crypt.Crypt;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

public class ClientChannelInitializer extends ChannelInitializer<NioSocketChannel> {

    private final String remoteAddress;
    private final int remotePort;
    private final String password;

    private final RemotePackageForwarder remotePackageForwarder;
    private final Crypt crypt;

    public ClientChannelInitializer(String remoteAddress, int remotePort, String password) {
        this.remoteAddress = remoteAddress;
        this.remotePort = remotePort;
        this.password = password;

        this.remotePackageForwarder = new RemotePackageForwarder(remoteAddress, remotePort);
        this.crypt = createCrypt(this.password);
    }

    private Crypt createCrypt(String password) {
        Crypt crypt = new Crypt();
        byte[] key = crypt.generateKey(password);
        byte[] iv = crypt.generateIv();
        crypt.setIv(iv);
        crypt.setKey(key);
        crypt.initEncrypt();
        crypt.initDecrypt();
        return crypt;
    }

    @Override
    protected void initChannel(NioSocketChannel ch) throws Exception {
        ch.attr(AttributeKey.valueOf(ChannelAttrKeys.CRYPT)).set(createCrypt(password));
        ChannelPipeline channelPipeline = ch.pipeline();
        channelPipeline.addLast(new LocalHttpDecoder(4096));
//        channelPipeline.addLast(new LocalPackageDecoder());
        channelPipeline.addLast(new PackageEncipher());
        channelPipeline.addLast(remotePackageForwarder);
    }
}
