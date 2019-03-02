package com.villcore.net.proxy.client.handler;

import com.villcore.net.proxy.crypt.Crypt;
import com.villcore.net.proxy.packet.Package;

import java.nio.ByteBuffer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.AttributeKey;

public class PackageEncipher extends SimpleChannelInboundHandler<Package> {

    private Crypt crypt;
    private boolean ivSend;

    // TODO metric.
    public PackageEncipher() {
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Package pkg) throws Exception {
        boolean localForward = ctx.channel().attr(AttributeKey.<Boolean>valueOf(ChannelAttrKeys.LOCAL_FORWARD)).get();
        if (localForward) {
            ctx.fireChannelRead(pkg);
            return;
        }

        if (crypt == null) {
            crypt = ctx.channel().attr(AttributeKey.<Crypt>valueOf(ChannelAttrKeys.CRYPT)).get();
        }

        Package newPkg = null;
        byte[] bytes = pkg.getBody();
        if(!ivSend) {
            byte[] iv = crypt.getIv();
            byte[] encryptHeader;
            byte[] encryptBody = crypt.encrypt(bytes);

            ByteBuffer tmp = ByteBuffer.wrap(new byte[4 + iv.length]);
            tmp.putInt(iv.length);
            tmp.put(iv);
            encryptHeader = tmp.array();
            newPkg = Package.buildPackage(encryptHeader, encryptBody);
            ivSend = true;
        } else {
            byte[] encryptHeader = new byte[0];
            byte[] encryptBody = crypt.encrypt(bytes);
            newPkg = Package.buildPackage(encryptHeader, encryptBody);
        }

        // TODO metric.
        // LOG.info("Encipher package {} \n {}", Package.toBytes(newPkg).length, new String(crypt.decrypt(newPkg.getBody())));
        if (newPkg != null) {
            ctx.fireChannelRead(newPkg);
        }
    }
}
