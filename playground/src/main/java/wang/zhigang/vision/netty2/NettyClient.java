/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.netty
 *
 *    Filename:    NettyClient
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
 *    Create at:   2026/2/9 11:16
 *
 *    Revision:
 *
 *    2026/2/9 11:16
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.netty2;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.AttributeKey;

import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * NettyClient
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/2/9 11:16
 */
public class NettyClient {

    private static final AttributeKey<String> CLIENT_NAME_KEY = AttributeKey.newInstance("clientName");

    public static final int MAX_RETRY = 8;

    public static void main(String[] args) throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.attr(CLIENT_NAME_KEY, "clientSay");
        bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
        bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.option(ChannelOption.TCP_NODELAY, true);
        NioEventLoopGroup group = new NioEventLoopGroup();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new FirstClientHandler());
                    }
                });
        CompletableFuture<Channel> resultFuture = new CompletableFuture<>();
        connect(bootstrap, "127.0.0.1", 18100, MAX_RETRY, resultFuture);

        resultFuture.whenComplete((channel, throwable) -> {
            if (throwable != null) {
                System.out.println("连接失败：" + throwable.getMessage());
                group.shutdownGracefully();
                return;
            }
            System.out.println("连接成功，Channel：" + channel);
        });


    }

    private static CompletableFuture<Channel> connect(final Bootstrap bootstrap, final String host, final int port, final int retry, final CompletableFuture<Channel> resultFuture) {

        bootstrap.connect(host, port).addListener((ChannelFuture future) -> {
            if (future.isSuccess()) {
                System.out.println("连接成功！");
                resultFuture.complete(future.channel());
            } else if (retry == 0) {
                System.out.println("重试次数已用完，放弃连接！");
                resultFuture.completeExceptionally(new RuntimeException("重试耗尽", future.cause()));
            } else {
                int order = (MAX_RETRY - retry) + 1;
                int delay = 1 << order;
                System.out.println(new Date() + " : 连接失败，第" + order + "次重连......");
                bootstrap.config().group().schedule(() -> connect(bootstrap, host, port, retry-1, resultFuture), delay, TimeUnit.SECONDS);
            }
        });
        return resultFuture;
    }

}