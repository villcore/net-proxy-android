package com.villcore.net.proxy.client.handler;

import com.villcore.net.proxy.packet.Package;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class LocalPackageDecoder extends ByteToMessageDecoder {

    // TODO metric
    public LocalPackageDecoder() {
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        int readableBytesSize = in.readableBytes();
        byte[] newBytes = new byte[readableBytesSize];
        in.readBytes(newBytes, 0, readableBytesSize);
        Package pkg = Package.buildPackage(Package.EMPTY_BYTE_ARRAY, newBytes);
        out.add(pkg);

        // TODO record bytes.
    }
}
