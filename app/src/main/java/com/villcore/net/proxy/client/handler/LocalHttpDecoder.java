package com.villcore.net.proxy.client.handler;

import com.villcore.net.proxy.client.HostPort;
import com.villcore.net.proxy.dns.DNS;
import com.villcore.net.proxy.packet.Package;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;

public class LocalHttpDecoder extends ByteToMessageDecoder {

    private boolean connected = false;
    private int maxBatchSize;
    private ByteArrayOutputStream requestBatch;

    // TODO metric
    public LocalHttpDecoder(int maxBatchSize) {
        this.requestBatch = new ByteArrayOutputStream();
        this.maxBatchSize = maxBatchSize;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (connected) {
            int readableBytesSize = in.readableBytes();
            byte[] newBytes = new byte[readableBytesSize];
            in.readBytes(newBytes, 0, readableBytesSize);
            Package pkg = Package.buildPackage(Package.EMPTY_BYTE_ARRAY, newBytes);
            out.add(pkg);
            return;
        }

        int readableBytesSize = in.readableBytes();
        if (readableBytesSize < 4) {
            return;
        }
        byte[] newBytes = new byte[readableBytesSize];
        in.readBytes(newBytes, 0, newBytes.length);
        requestBatch.write(newBytes);
        if (isValid(newBytes)) {
            byte[] fullHttpBytes = requestBatch.toByteArray();
            requestBatch.close();
            // LOG.info("valid http \n {}\n", new String(fullHttpBytes));
            HostPort hostPort = parseHostPort(fullHttpBytes);
            boolean localForward = DNS.isAccessable(hostPort);
            if (HostPort.isValid(hostPort)) {
                ctx.channel().attr(AttributeKey.valueOf(ChannelAttrKeys.LOCAL_FORWARD)).set(localForward);
                ctx.channel().attr(AttributeKey.valueOf(ChannelAttrKeys.HOST_PORT)).set(hostPort);

                if (localForward && hostPort.getPort() == 443) {
                    ctx.channel().writeAndFlush(Unpooled.wrappedBuffer("HTTP/1.0 200 Connection Established\r\n\r\n".getBytes(StandardCharsets.UTF_8)));
                    connected = true;
                    return;
                }
            }

            Package pkg = Package.buildPackage(Package.EMPTY_BYTE_ARRAY, fullHttpBytes);
            out.add(pkg);
            requestBatch = null;
            connected = true;
        }
    }

    public boolean isValid(byte[] bytes) {
        int len = bytes.length;
        return requestBatch.size() >= maxBatchSize || (bytes[len - 4] == 13 && bytes[len - 3] == 10 && bytes[len - 2] == 13 && bytes[len - 1] == 10);
    }

    private HostPort parseHostPort(byte[] bytes) {
        StringReader stringReader = new StringReader(new String(bytes, StandardCharsets.UTF_8));
        try(BufferedReader br = new BufferedReader(stringReader)) {
            String line = null;
            while ((line = br.readLine()) != null) {
                if (line.startsWith("Host")) {
                    line = line.replace("Host:", "").trim();
                    String[] splits = line.split(":");
                    if (splits.length == 2) {
                        return new HostPort(splits[0], Integer.valueOf(splits[1]));
                    } else if (splits.length == 1){
                        return new HostPort(splits[0], 80);
                    } else {
                        return HostPort.INVALID_HOST_PORT;
                    }
                }
            }
        } catch (IOException e) {
           return HostPort.INVALID_HOST_PORT;
        }
        return HostPort.INVALID_HOST_PORT;
    }

    private String connectMethod(String host, int port) {
        return String.format("CONNECT %s:%d HTTP/1.1\r\n"
                + "Host: %s:%d\r\n"
                + "Proxy-Connection: keep-alive\r\n"
                + "\r\n\r\n", host, port, host, port);
    }
}
