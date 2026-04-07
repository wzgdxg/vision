/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.nettytion.echo
 *
 *    Filename:    EchoServer
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
 *    Create at:   2026/3/25 10:34
 *
 *    Revision:
 *
 *    2026/3/25 10:34
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.nettytion.echo;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * EchoServer
 * 入站处理器：异常从链的头部向尾部传播（A → B → C）
 * 出站处理器：异常从链的尾部向头部传播（C → B → A）
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/3/25 10:34
 */
@Slf4j
public class EchoServer {
    private static NioEventLoopGroup bossGroup;
    private static NioEventLoopGroup workGroup;

    public static void main(String[] args) throws Exception{
        ServerBootstrap bootstrap = new ServerBootstrap();
        try{
            bossGroup = new NioEventLoopGroup();
            workGroup = new NioEventLoopGroup();
            bootstrap.group(bossGroup, workGroup)
                    .channel(NioServerSocketChannel.class)
                    .option(ChannelOption.SO_BACKLOG, 1024)
                    .option(ChannelOption.SO_REUSEADDR, true)
                    .childOption(ChannelOption.SO_KEEPALIVE, true)
                    .childOption(ChannelOption.TCP_NODELAY, true)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoServerHandler());
                        }
                    });

            ChannelFuture channelFuture = bootstrap.bind("127.0.0.1", 18181).addListener(future -> {
                if (future.isSuccess()) {
                    System.out.println("EchoServer start success");
                } else {
                    System.out.println("EchoServer start failed");
                    log.error("EchoServer start failed", future.cause());
                }
            });

            Runtime.getRuntime().addShutdownHook(new Thread(EchoServer::shutdown));

            channelFuture.channel().closeFuture().sync();
        } catch (Exception e){
            log.error("EchoServer start failed", e);
        } finally {
            shutdown();
        }
    }

    public synchronized static void shutdown(){
        System.gc();
        try {
            if(!bossGroup.isShutdown() || !workGroup.isShutdown()){
                bossGroup.shutdownGracefully().sync();
                workGroup.shutdownGracefully().sync();

                System.out.println("EchoServer shutdown");
            }
        } catch (InterruptedException e) {
            log.error("EchoServer shutdown failed", e);
        }
    }
}