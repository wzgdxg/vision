/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.nettytion.websocket.client
 *
 *    Filename:    WebsocketClient
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
 *    Create at:   2026/4/23 14:17
 *
 *    Revision:
 *
 *    2026/4/23 14:17
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.nettytion.websocket.client;

import cn.hutool.core.thread.ThreadUtil;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import lombok.extern.slf4j.Slf4j;
import wang.zhigang.vision.nettytion.websocket.WebSocketStatusAndEventHandler;

import javax.net.ssl.SSLException;
import java.net.URI;
import java.util.concurrent.TimeUnit;

/**
 * WebsocketClient
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/4/23 14:17
 */
@Slf4j
public class WebsocketClient {

    private static final String host = "127.0.0.1";
    private static final int port = 8080;
    private static final String WS_SERVER_URL = "ws://" + host + ":" + port + "/ws";

    /**
     * 最多尝试发起重连1次
     */
    private static boolean canReconnect = true;

    private static boolean isShutdown = false;

    private static Bootstrap bootstrap;
    private static NioEventLoopGroup group;
    private static SslContext sslContext;
    static {
        group = new NioEventLoopGroup();
        // 添加SSL处理（如果需要）
        try {
            sslContext = SslContextBuilder.forClient()
                    .trustManager(InsecureTrustManagerFactory.INSTANCE)
                    .build();
        } catch (SSLException e) {
            throw new RuntimeException(e);
        }
    }

    public static void main(String[] args) {

        bootstrap = new Bootstrap();
        // 创建WebSocket握手处理器
        WebSocketClientHandshaker handshaker = WebSocketClientHandshakerFactory.newHandshaker(
                URI.create(WS_SERVER_URL), // WebSocket服务端地址
                WebSocketVersion.V13, // WebSocket版本
                null, // 子协议
                true, // 允许扩展
                new DefaultHttpHeaders() // HTTP头
        );
        bootstrap.group(group).option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
//                        ch.pipeline().addLast(sslContext.newHandler(ch.alloc()));
                        ch.pipeline().addLast(new IdleStateHandler(5, 0, 0, TimeUnit.SECONDS))
                                .addLast(new HttpClientCodec())
                                .addLast(new HttpObjectAggregator(1024 * 1024 * 10))
                                .addLast(new ChunkedWriteHandler())    //大文件分块写出
                                .addLast(new WebSocketClientProtocolHandler(handshaker, true, false))
                                .addLast(new ClientWebSocketStatusAndEventHandler())
                                .addLast(new WebsocketClientHandler());

                    }
                });
        connect(3);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            isShutdown = true;
            log.warn("正在关闭客户端...");
            shutdownGracefully();
        }));
    }

    public static void connect(int retryMaxCount) {
        connect(bootstrap, retryMaxCount, 0);
    }

    public static void connect(Bootstrap bootstrap, int retryMaxCount, int retryCount) {
        bootstrap.connect("localhost", 8080).addListener(future -> {
            if (future.isSuccess()) {
                System.out.println("连接成功");
            } else {
                if (retryCount < retryMaxCount) {
                    log.warn("连接失败，开始第{}重试，最大重试次数：{}", retryCount + 1, retryMaxCount);
                    ThreadUtil.sleep(3, TimeUnit.SECONDS);
                    connect(bootstrap, retryMaxCount, retryCount + 1);
                } else {
                    log.error("连接失败，重试次数超过最大重试次数");
                    System.exit(1);
                }
            }
        });
    }

    public static void reconnect() {
        if (isShutdown) {
            return;
        }
        if (canReconnect) {
            log.warn("❌ 连接已断开，开始自动重连...");
            canReconnect = false;
            connect(3);
        } else {
            log.warn("已重连且失败，退出程序");
            System.exit(1);
        }
    }

    public static void shutdownGracefully() {
        if (group != null) {
            group.shutdownGracefully();
        }
        log.warn("客户端已关闭");
    }
}