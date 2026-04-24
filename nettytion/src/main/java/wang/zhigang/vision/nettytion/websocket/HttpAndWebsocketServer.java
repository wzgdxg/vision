/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.nettytion.websocket
 *
 *    Filename:    WebsocketServer
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
 *    Create at:   2026/4/17 11:05
 *
 *    Revision:
 *
 *    2026/4/17 11:05
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.nettytion.websocket;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.ssl.*;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.ImmediateEventExecutor;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.KeyManagerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.concurrent.TimeUnit;

/**
 * WebsocketServer
 *
 *  keytool -delete -alias localhost -keystore localhost.p12 -storepass 123456
 *  keytool -genkey -alias localhost -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -dname "CN=localhost,OU=test,O=test,L=test,ST=test,C=CN" -ext "SAN=IP:127.0.0.1,DNS:localhost" -validity 3650 -storetype PKCS12 -keystore localhost.p12 -storepass 123456 -keypass 123456
 *  keytool -export -alias localhost -keystore localhost.p12 -file localhost.crt -storepass 123456
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/4/17 11:05
 */
@Slf4j
public class HttpAndWebsocketServer {

    public static final String WS_URI = "/ws";
    private final static SslContext context;
    public static final NioEventLoopGroup bossGroup;
    public static final NioEventLoopGroup workerGroup;
    private final static ChannelGroup channelGroup = new DefaultChannelGroup(ImmediateEventExecutor.INSTANCE);//1
    static {
        bossGroup = new NioEventLoopGroup();
        workerGroup = new NioEventLoopGroup();
        try {
//            SelfSignedCertificate cert = new SelfSignedCertificate();
//            context = SslContext.newServerContext(cert.certificate(), cert.privateKey());

            // 加载 PKCS12 证书
            KeyStore keyStore = KeyStore.getInstance("PKCS12");
            try (InputStream inputStream = HttpAndWebsocketServer.class.getClassLoader().getResourceAsStream("localhost.p12")){
                if (inputStream == null) {
                    throw new RuntimeException("证书文件 localhost.p12 未找到");
                }
                keyStore.load(inputStream, "123456".toCharArray());
            }

            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(keyStore, "123456".toCharArray());

            // 创建 SSL 上下文
            context = SslContextBuilder.forServer(kmf)
//                    .sslProvider(SslProvider.BORINGSSL)  // 关键
                    // 兼容现代浏览器 TLS1.2 + TLS1.3
                    .protocols(SslProtocols.TLS_v1_2, SslProtocols.TLS_v1_3)
//                    .protocols(SslProtocols.TLS_v1_2)
                    // Session ID 内存会话缓存（解决刷新bug + 性能提速）
                    .sessionCacheSize(4096)  //最多多少个 Session ID 缓存由 Netty/JDK 全自动管理，你不需要写任何代码干预；  达到 4096 上限不会拒绝连接，而是自动淘汰最久没用的会话（LRU 算法）
                    .sessionTimeout(7200) // 2小时，对齐系统TCP保活默认时长  只要成功复用一次，服务端内存里的时间自动重置回 2 小时。
                    .build();

        } catch (UnrecoverableKeyException | CertificateException | KeyStoreException | IOException |
                 NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                //可以给服务端channel设置一些TCP参数，比如so_backlog,表示系统用于
                //临时存放已完成三次握手的请求队列的最大长度，如果连接建立频繁，服务器
                //处理创建新连接较慢，则可以适当调大这个参数
                //简单来说，就是新创建出的全连接，再交给boss之前的一个缓冲
                //和操作系统somaxconn协同产生最终数量    实际生效的全连接队列长度 = min(Netty设置的SO_BACKLOG值, 系统somaxconn值)
                .option(ChannelOption.SO_BACKLOG, 1024)
                //TCP 连接关闭时，主动关闭连接的一方会进入 TIME_WAIT 状态（中文叫 “时间等待”），这个状态默认会持续 2MSL（MSL 是报文最大生存时间，Linux 默认约 1 分钟，Windows 约 2 分钟）。
                .option(ChannelOption.SO_REUSEADDR, true) // 允许端口复用
                //长时间连接是否存活检测，默认是2小时，会尝试多次，已经间隔多久，发空包
                //Netty 设置SO_KEEPALIVE=true的作用是启用当前 Socket 的保活机制，这个 “启用开关” 100% 生效（操作系统不会拒绝）；但启用后，保活机制的具体行为（超时时间、重试间隔、次数） 由系统内核参数控制，Netty 无法直接修改。
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                //小数据包是否延迟发送
                //ChannelOption.TCP_NODELAY：Netty 设置即生效，几乎无系统限制
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //以下三行是错误的，导致ssl配置的参数全部丢失
//                        SSLEngine engine = context.newEngine(ch.alloc());
//                        engine.setUseClientMode(false);
//                        ch.pipeline().addLast(new SslHandler(engine))
                        ch.pipeline() //.addLast(context.newHandler(ch.alloc()))
                                .addLast(new IdleStateHandler(6, 0, 0, TimeUnit.SECONDS))
                                .addLast(new HttpServerCodec())
                                // 👇 就加这一行，自动处理 10处理 100 Continue
                                .addLast(new HttpServerExpectContinueHandler())
                                .addLast(new HttpObjectAggregator(1024 * 1024 * 10))
                                .addLast(new ChunkedWriteHandler())    //大文件分块写出
                                .addLast(new HttpRequestHandler(WS_URI))
                                .addLast(new WebSocketHeartBeatLogHandler())
                                .addLast(new WebSocketServerProtocolHandler(
                                        WS_URI,           // /ws
                                        null,             // subprotocols，可选
                                        true,             // allowExtensions
                                        1024 * 1024 * 10,            // maxFrameSize
                                        false,            // allowMaskMismatch
                                        true,              // checkStartsWith（匹配 /ws 开头）
                                        false,             //dropPongFrames，传递pong帧
                                        3000                // 握手超时时间3秒
                                ))
                                .addLast(new WebSocketStatusAndEventHandler(channelGroup))
                                .addLast(new TextWebSocketFrameHandler(channelGroup));


                    }
                });
        bind(bootstrap, 8080, 3);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.warn("正在关闭服务器...");
            shutdownGracefully();
        }));
    }

    private static void bind(ServerBootstrap serverBootstrap, int port, int failTryTotal) {
        bind(serverBootstrap, port, failTryTotal, failTryTotal);
    }

    private static void bind(ServerBootstrap serverBootstrap, int port,  int failTryTotal, int failTryNum) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                log.info("Server started and listening on port {}", port);
            } else {
                log.error("Server start failed", future.cause());
                if (failTryNum > 0) {
                    bind(serverBootstrap, port, failTryNum - 1);
                } else {
                    log.error("Server start failed after {} times", failTryTotal);
                }
            }
        });
    }

    public static void shutdownGracefully() {
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        System.out.println("服务器已关闭");
    }

}