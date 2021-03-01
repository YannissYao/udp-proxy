package com.demo.gateway.udp;

import com.demo.gateway.client.CustomClientAsyncHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.DatagramChannel;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContentDecompressor;
import io.netty.handler.codec.http.HttpObjectAggregator;

public class ClientHandleInitializer extends ChannelInitializer<DatagramChannel> {

    @Override
    protected void initChannel(DatagramChannel ch) {
        ChannelPipeline pipeline = ch.pipeline();

        pipeline.addLast("clientHandler", new ReceiveRealAddressHandle(null, null, null));
    }
}