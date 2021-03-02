package com.demo.gateway.udp;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;

public class ClientHandleInitializer extends ChannelInitializer<DatagramChannel> {

    @Override
    protected void initChannel(DatagramChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast("clientHandler", new ReceiveRealAddressHandle(null, null, null, null));
    }
}