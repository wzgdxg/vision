/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.nettytion.echo
 *
 *    Filename:    EchoClient
 *
 *    Description: 
 *
 *    Copyright:   Copyright (c) 2001-2023
 *
 *    Company:     hhdd.com
 *
 *    @author: 王志刚
 *
 *    @version: 1.0.0
 *
 *    Create at:   2026/3/25 11:26
 *
 *    Revision:
 *
 *    2026/3/25 11:26
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.nettytion.echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * EchoClient
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/3/25 11:26
 */
@Slf4j
public class EchoClient {
    private static NioEventLoopGroup eventLoopGroup;

    public static void main(String[] args) throws Exception{
        Bootstrap bootstrap = new Bootstrap();
        try{
            eventLoopGroup = new NioEventLoopGroup();
            bootstrap.group(eventLoopGroup)
                    .channel(NioSocketChannel.class)
                    .option(ChannelOption.SO_KEEPALIVE, true)
                    .option(ChannelOption.TCP_NODELAY, true)
                    .handler(new ChannelInitializer<SocketChannel>() {

                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoClientHandler());
                            ch.pipeline().addLast(new EchoClientOutHandler());
                        }
                    });
            ChannelFuture channelFuture = bootstrap.connect("127.0.0.1", 18181).addListener(future -> {
                if (future.isSuccess()) {
                    System.out.println("EchoClient connect success");
                } else {
                    System.out.println("EchoClient connect failed");
                }
            });
            Runtime.getRuntime().addShutdownHook(new Thread(EchoClient::shutdown));
            channelFuture.channel().closeFuture().sync();
        } catch (Exception e){
            log.error("EchoClient connect failed", e);
        } finally {
            shutdown();
        }
    }

    public static void shutdown(){
        System.gc();
        System.runFinalization();
        try {
            if(!eventLoopGroup.isShutdown()){
                eventLoopGroup.shutdownGracefully().sync();
                System.out.println("EchoClient shutdown");
            }
        } catch (InterruptedException e) {
            log.error("EchoClient shutdown failed", e);
        }
    }
}