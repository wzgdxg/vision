/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.flash.server
 *
 *    Filename:    NettyServer
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
 *    Create at:   2026/3/24 15:05
 *
 *    Revision:
 *
 *    2026/3/24 15:05
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.flash.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import wang.zhigang.vision.flash.handler.server.ServerHandler;

import java.util.Date;

/**
 * NettyServer
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/3/24 15:05
 */
public class NettyServer {
    private static NioEventLoopGroup bossGroup;
    private static NioEventLoopGroup workerGroup;


    public static void main(String[] args) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        // 保存EventLoopGroup引用，以便后续关闭,ru，如果不关闭，无法立马释放资源，会导致端口未及时释放，被占用
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                //TCP 连接关闭时，主动关闭连接的一方会进入 TIME_WAIT 状态（中文叫 “时间等待”），这个状态默认会持续 2MSL（MSL 是报文最大生存时间，Linux 默认约 1 分钟，Windows 约 2 分钟）。
                .option(ChannelOption.SO_REUSEADDR, true) // 允许端口复用
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) {
                        socketChannel.pipeline()
//                                .addLast(new LifeCycleHandler())
                                // 空闲检测
//                                .addLast(new MyIdleStateHandler())
                                // 解决粘包和半包问题
//                                .addLast(new SplitHandler())
                                .addLast(new ServerHandler());
//                                .addLast(LOGIN_HANDLER, HEART_BEAT_HANDLER, AUTH_HANDLER)
//                                .addLast(SERVER_HANDLER);
//                                .addLast(new PacketEncoder());
                    }
                });

        bind(serverBootstrap, 8080);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("正在关闭服务器...");
            shutdownGracefully();
        }));
    }

    /**
     * 绑定端口号，绑定失败后重试
     */
    private static void bind(ServerBootstrap serverBootstrap, int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println(new Date() + ": 端口[" + port + "]绑定成功!");
            } else {
                System.err.println("端口[" + port + "]绑定失败! ");
                bind(serverBootstrap, port);
            }
        });
    }

    /**
     * 优雅关闭服务器
     */
    private static void shutdownGracefully() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        System.out.println("服务器已关闭");
    }
}