package com.villcore.net.proxy.client.handler;

import com.villcore.net.proxy.client.HostPort;
import com.villcore.net.proxy.crypt.Crypt;
import com.villcore.net.proxy.packet.Package;
import com.villcore.net.proxy.util.SocketUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

@ChannelHandler.Sharable
public class RemotePackageForwarder extends SimpleChannelInboundHandler<Package> {

    private final String proxyServerAddress;
    private final int proxyServerPort;

    private EventLoopGroup eventLoopGroup;
    private Bootstrap bootstrap;

    private final ConcurrentMap<Channel, Channel> channelMap = new ConcurrentHashMap<>();

    public RemotePackageForwarder(String proxyServerAddress, int proxyServerPort) {
        this.proxyServerAddress = proxyServerAddress;
        this.proxyServerPort = proxyServerPort;
        this.eventLoopGroup = new NioEventLoopGroup();
        initBoostrap(this.proxyServerAddress, this.proxyServerPort);
    }

    private void initBoostrap(String proxyServerAddress, int proxyServerPort) {
        // TODO just debug
        // LoggingHandler loggingHandler = new LoggingHandler(LogLevel.INFO);

        this.eventLoopGroup = new NioEventLoopGroup(3);
        this.bootstrap = new Bootstrap();
        this.bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_RCVBUF, 1 * 1024 * 1024)
                .option(ChannelOption.SO_SNDBUF, 1 * 1024 * 1024)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<NioSocketChannel>() {
                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new LoggingHandler());
                        pipeline.addLast(new RemotePackageDecoder());
                        pipeline.addLast(new PackageDecipher());
                        pipeline.addLast(new SimpleChannelInboundHandler<Package>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, Package pkg) throws Exception {
                                Channel channel = ctx.channel();
                                Attribute<Object> sourceChannelAttr = channel.attr(AttributeKey.valueOf(ChannelAttrKeys.SOURCE_CHANNEL));
                                Channel localChannel = (Channel) sourceChannelAttr.get();
                                localChannel.writeAndFlush(Unpooled.wrappedBuffer(pkg.getBody()));
                            }
                        });
                    }
                });
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        Channel localChannel = ctx.channel();
        Channel remoteChannel = channelMap.remove(localChannel);
        if (remoteChannel == null) {
            return;
        }
        remoteChannel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
            }
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Package pkg) throws Exception {
        if (!channelMap.containsKey(ctx.channel())) {
            initRemoteChannel(ctx);
        }
        Channel localChannel = ctx.channel();
        Channel remoteChannel = channelMap.get(localChannel);
        boolean localForward = ctx.channel().attr(AttributeKey.<Boolean>valueOf(ChannelAttrKeys.LOCAL_FORWARD)).get();
        if (remoteChannel != null) {
            byte[] bytes = Package.toBytes(pkg);
            bytes = localForward ? pkg.getBody() : bytes;
            remoteChannel.writeAndFlush(Unpooled.wrappedBuffer(bytes));
        }
        ReferenceCountUtil.release(pkg);
    }

    private void initRemoteChannel(ChannelHandlerContext ctx) throws InterruptedException {
        final Channel localChannel = ctx.channel();
        boolean localForward = ctx.channel().attr(AttributeKey.<Boolean>valueOf(ChannelAttrKeys.LOCAL_FORWARD)).get();
        HostPort hostPort = ctx.channel().attr(AttributeKey.<HostPort>valueOf(ChannelAttrKeys.HOST_PORT)).get();

        Channel remoteChannel = null;
        if (localForward) {
            remoteChannel = this.bootstrap.connect(hostPort.getHost(), hostPort.getPort()).sync().channel();
        } else {
            remoteChannel = this.bootstrap.connect(proxyServerAddress, proxyServerPort).sync().channel();
        }
        SocketUtils.protectChannel(remoteChannel);
        Attribute<Channel> sourceChannelAttr = remoteChannel.attr(AttributeKey.<Channel>valueOf(ChannelAttrKeys.SOURCE_CHANNEL));
        sourceChannelAttr.set(localChannel);
        Attribute<Object> cryptAttribute = localChannel.attr(AttributeKey.valueOf(ChannelAttrKeys.CRYPT));
        Crypt crypt = (Crypt) cryptAttribute.get();
        remoteChannel.attr(AttributeKey.valueOf(ChannelAttrKeys.CRYPT)).set(crypt);
        remoteChannel.attr(AttributeKey.valueOf(ChannelAttrKeys.LOCAL_FORWARD)).set(localForward);

        channelMap.put(localChannel, remoteChannel);
        remoteChannel.closeFuture().addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                channelMap.remove(localChannel);
                localChannel.close();
            }
        });
    }
}
