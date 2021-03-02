package com.demo.gateway.udp;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.DatagramPacket;
import io.netty.channel.socket.nio.NioDatagramChannel;

import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

public class ForwardHandle extends SimpleChannelInboundHandler<DatagramPacket> {

    private ConcurrentHashMap<Integer, Channel> clientChannelMap = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ArrayBlockingQueue<Channel>> remoteFreeChannels = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, ConcurrentHashMap<Integer, Channel>> remoteBusyChannels = new ConcurrentHashMap<>();

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        cause.printStackTrace();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
        String h = datagramPacket.sender().getHostName();
        Integer port = datagramPacket.sender().getPort();
        String key = h + ":" + port;
        Integer channelHashCode = ctx.channel().hashCode();


        //初始化remote real server receive
        Channel remoteChannel = getRemoteChannel(key, channelHashCode);
        ByteBuf buff = remoteChannel.alloc().buffer();
        buff.writeBytes(datagramPacket.content());
        ReceiveRealAddressHandle handler = new ReceiveRealAddressHandle(this, key, ctx.channel(), channelHashCode);
        remoteChannel.pipeline().replace("clientHandler", "clientHandler", handler);

        try {
            //完成后回调
            remoteChannel.writeAndFlush(buff).addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
                    if (!clientChannelMap.containsKey(channelHashCode)) {
                        clientChannelMap.put(channelHashCode, ctx.channel());
                    }
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
            repayChannel(key, channelHashCode);
        }
    }


    private Channel getRemoteChannel(String key, Integer channelHashCode) throws InterruptedException {
        Channel channel;

        int freeSize = remoteFreeChannels.get(key) == null ? 0 : remoteFreeChannels.get(key).size();
        int busySize = remoteBusyChannels.get(key) == null ? 0 : remoteBusyChannels.get(key).size();
        System.out.println(freeSize + "==================" + busySize);


        if (!remoteFreeChannels.containsKey(key)) {
            //TODO 动态获取
            channel = createRemoteChannel("3.94.200.129", 8000);

//            remoteFreeChannels.put(key, new ArrayBlockingQueue<Channel>(2));
//            remoteFreeChannels.get(key).add(channel);

        } else {
            if (remoteFreeChannels.get(key).isEmpty()) {
                //                channel = remoteFreeChannels.get(key).take();
                System.out.println("==============================================" + remoteFreeChannels.get(key).size());
                channel = createRemoteChannel("3.94.200.129", 8000);
            }
//            else if (remoteBusyChannels.get(key).size() < 4) {
//                //TODO 动态获取
//                channel = createRemoteChannel("3.94.200.129", 8000);
//            }
            else {
                channel = remoteFreeChannels.get(key).take();
            }
        }

        ConcurrentHashMap<Integer, Channel> codeChannelMap = remoteBusyChannels.getOrDefault(key, new ConcurrentHashMap<>(5));
        codeChannelMap.put(channelHashCode, channel);
        remoteBusyChannels.put(key, codeChannelMap);

        return channel;
    }

    private Channel createRemoteChannel(String address, int port) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        ThreadFactory serverBoos = new ThreadFactoryBuilder().setNameFormat("server udp boos-%d").build();
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(3, serverBoos);
        bootstrap.group(bossGroup)
                .channel(NioDatagramChannel.class)
//                .option(ChannelOption.AUTO_CLOSE, false)
//                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .handler(new ClientHandleInitializer());
        Channel channel = bootstrap.connect(address, port).sync().channel();
        return channel;
    }


    public void writeBack(String key, ByteBuf byteBuf, Integer channelCode) {
        Channel channel = clientChannelMap.get(channelCode);
        String clientIp = key.split(":")[0];
        Integer clientPort = Integer.valueOf(key.split(":")[1]);
        DatagramPacket datagramPacket = new DatagramPacket(byteBuf, new InetSocketAddress(clientIp, clientPort));
        try {
            channel.writeAndFlush(datagramPacket);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //归还channel
            this.repayChannel(key, channelCode);
        }
    }

    /**
     * 归还channel到缓冲池
     * 缓存池添加，繁忙池删除
     */
    private void repayChannel(String key, int channelHashCode) {
        //客户端长链接
//        clientChannelMap.remove(channelHashCode);

        //真实服务器长链接
        Channel remoteChannel = remoteBusyChannels.get(key).get(channelHashCode);//双get 获取到channel
        if (!remoteFreeChannels.containsKey(key)) {
            remoteFreeChannels.put(key, new ArrayBlockingQueue<>(5));
        }

        remoteFreeChannels.get(key).offer(remoteChannel);


//        remoteFreeChannels.get(key).clear();
//        remoteBusyChannels.get(key).remove(channelHashCode);
//        System.out.println("aaaaaaaaaaaaaaaaaaa"+remoteFreeChannels.get(key).size());
    }

}
