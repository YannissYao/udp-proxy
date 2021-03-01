package com.demo.gateway.udp;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioDatagramChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.concurrent.ThreadFactory;


/**
 * 服务端配置启动
 *
 * @author Yannis
 */
@Component
public class UdpServer implements ApplicationRunner, DisposableBean {

    private static final Logger logger = LoggerFactory.getLogger(UdpServer.class);

    private EventLoopGroup bossGroup;
//    private EventLoopGroup serverGroup;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        ThreadFactory serverBoos = new ThreadFactoryBuilder().setNameFormat("server boos-%d").build();
        bossGroup = new NioEventLoopGroup(3, serverBoos);
        Bootstrap b=new Bootstrap();
        b.group(bossGroup)
                .channel(NioDatagramChannel.class)
//                .option(ChannelOption.AUTO_CLOSE, false)
//                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .handler(new ForwardHandle());
        b.bind(28080).sync().channel().closeFuture().await();

    }

    @Override
    public void destroy() {
        bossGroup.shutdownGracefully();
//        serverGroup.shutdownGracefully();
    }
}
