package com.demo.gateway.udp;

import com.demo.gateway.client.CustomClientAsyncHandler;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ThreadFactory;

public class ForwardHandle extends SimpleChannelInboundHandler<DatagramPacket> {

    private final Map<String, Channel> remoteChannelMap = new HashMap<>();
    private final Map<String, Channel> clientChannelMap = new HashMap<>();

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
        //利用ByteBuf的toString()方法获取请求消息
//        String req = datagramPacket.content().toString(CharsetUtil.UTF_8);
        String h = datagramPacket.sender().getHostName();
        Integer port = datagramPacket.sender().getPort();
        String key = h + ":" + port;
        if (!clientChannelMap.containsKey("")) {
            clientChannelMap.put(key, ctx.channel());
        }

        Channel channel = createChannel(key, "3.94.200.129", 8000);
        ByteBuf buff = channel.alloc().buffer();
        buff.writeBytes(datagramPacket.content());

        //初始化remote real server receive
        ReceiveRealAddressHandle handler = new ReceiveRealAddressHandle(this, key, ctx.channel());
        channel.pipeline().replace("clientHandler", "clientHandler", handler);

        channel.writeAndFlush(buff);


//                .addListener(new ChannelFutureListener() {
//            @Override
//            public void operationComplete(ChannelFuture future) {
//                ctx.close();
//            }
//        });
    }


    private Channel createChannel(String key, String address, int port) throws InterruptedException {
        if (remoteChannelMap.containsKey(key)) {
            return remoteChannelMap.get(key);
        }

        ThreadFactory serverBoos = new ThreadFactoryBuilder().setNameFormat("server udp boos-%d").build();

        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(3, serverBoos);
        bootstrap.group(bossGroup)
                .channel(NioDatagramChannel.class)
//                .option(ChannelOption.AUTO_CLOSE, false)
//                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .handler(new ClientHandleInitializer());
        Channel channel = bootstrap.connect(address, port).sync().channel();
        remoteChannelMap.put(key, channel);
        return channel;
    }

    public void writeBack(String key, ByteBuf byteBuf) {
//        ByteBuf buff = channel.alloc().buffer();
//        buff.writeBytes(datagramPacket.content());
        Channel channel = clientChannelMap.get(key);
        String clientIp = key.split(":")[0];
        Integer clientPort = Integer.valueOf(key.split(":")[1]);
        DatagramPacket datagramPacket = new DatagramPacket(byteBuf, new InetSocketAddress(clientIp, clientPort));
        channel.writeAndFlush(datagramPacket);
    }
}
