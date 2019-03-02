package com.villcore.net.proxy.client.handler;

import com.villcore.net.proxy.packet.Package;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;

public class RemotePackageDecoder extends ByteToMessageDecoder {

    // TODO metric
    private int packageSize;
    private int headerLen;
    private int bodyLen;
    private boolean headerRead;

    public RemotePackageDecoder() {
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        boolean localForward = ctx.channel().attr(AttributeKey.<Boolean>valueOf(ChannelAttrKeys.LOCAL_FORWARD)).get();
        if (localForward) {
            int readableBytesSize = in.readableBytes();
            byte[] newBytes = new byte[readableBytesSize];
            in.readBytes(newBytes, 0, readableBytesSize);
            Package pkg = Package.buildPackage(Package.EMPTY_BYTE_ARRAY, newBytes);
            out.add(pkg);
            return;
        }

        if (!headerRead && in.readableBytes() >= 12) {
            packageSize = in.readInt();
            headerLen = in.readInt();
            bodyLen = in.readInt();
            headerRead = true;
        }

        if (in.readableBytes() >= headerLen + bodyLen) {
            byte[] header = new byte[headerLen];
            byte[] body = new byte[bodyLen];
            in.readBytes(header).readBytes(body);

            packageSize = 0;
            headerLen = 0;
            bodyLen = 0;
            headerRead = false;
            Package pkg = Package.buildPackage(Package.EMPTY_BYTE_ARRAY, body);
            out.add(pkg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
    }
}
