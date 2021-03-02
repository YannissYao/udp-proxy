package com.demo.gateway.udp;


import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.DatagramPacket;

public class ReceiveRealAddressHandle extends SimpleChannelInboundHandler<DatagramPacket> {


    private final ForwardHandle forwardHandle;
    private final String key;
    private final Integer channelHashCode;
    private final Channel clientChannel;

    public ReceiveRealAddressHandle(ForwardHandle forwardHandle, String key, Channel clientChannel, Integer channelHashCode) {
        this.forwardHandle = forwardHandle;
        this.key = key;
        this.channelHashCode = channelHashCode;
        this.clientChannel = clientChannel;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, DatagramPacket datagramPacket) throws Exception {
//        System.out.println(datagramPacket.sender().getHostName() + ":" + datagramPacket.sender().getPort());
        ByteBuf buff = clientChannel.alloc().buffer();
        buff.writeBytes(datagramPacket.content());
        forwardHandle.writeBack(key, buff, channelHashCode);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}