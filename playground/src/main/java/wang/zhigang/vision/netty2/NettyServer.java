/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.netty
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
 *    Create at:   2026/2/9 10:27
 *
 *    Revision:
 *
 *    2026/2/9 10:27
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.netty2;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.util.AttributeKey;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * NettyServer
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/2/9 10:27
 */
public class NettyServer {

    // 1. 定义全局的AttributeKey常量（核心：设置和获取必须用同一个Key）
    private static final AttributeKey<String> SERVER_NAME_KEY = AttributeKey.newInstance("serverName");

    private static final AttributeKey<String> CLIENT_HELLO_KEY = AttributeKey.newInstance("clientKey");

    public static void main(String[] args) {
        ServerBootstrap serverBootStrap = new ServerBootstrap();
        serverBootStrap.attr(SERVER_NAME_KEY, "nettyServer");  //设置服务端channel属性
        serverBootStrap.childAttr(CLIENT_HELLO_KEY, "你好！");//设置客户度channel属性

        //可以给服务端channel设置一些TCP参数，比如so_backlog,表示系统用于
        //临时存放已完成三次握手的请求队列的最大长度，如果连接建立频繁，服务器
        //处理创建新连接较慢，则可以适当调大这个参数
        //简单来说，就是新创建出的全连接，再交给boss之前的一个缓冲
        //和操作系统somaxconn协同产生最终数量    实际生效的全连接队列长度 = min(Netty设置的SO_BACKLOG值, 系统somaxconn值)
        serverBootStrap.option(ChannelOption.SO_BACKLOG, 1024);

        //长时间连接是否存活检测，默认是2小时，会尝试多次，已经间隔多久，发空包
        //Netty 设置SO_KEEPALIVE=true的作用是启用当前 Socket 的保活机制，这个 “启用开关” 100% 生效（操作系统不会拒绝）；但启用后，保活机制的具体行为（超时时间、重试间隔、次数） 由系统内核参数控制，Netty 无法直接修改。
        serverBootStrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        //小数据包是否延迟发送
        //ChannelOption.TCP_NODELAY：Netty 设置即生效，几乎无系统限制
        serverBootStrap.childOption(ChannelOption.TCP_NODELAY, true);

        NioEventLoopGroup boss = new NioEventLoopGroup();
        NioEventLoopGroup worker = new NioEventLoopGroup();
        serverBootStrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .handler(new ChannelInitializer<NioServerSocketChannel>() {
                    @Override
                    protected void initChannel(NioServerSocketChannel ch) throws Exception {
                        System.out.println(ch.attr(SERVER_NAME_KEY) + " 服务端启动中");
                    }
                })
                .childHandler(new ChannelInitializer<NioSocketChannel>() {

                    @Override
                    protected void initChannel(NioSocketChannel ch) throws Exception {
                        ch.pipeline().addLast(new FirstServerHandler());
                    }
                });
        bind(serverBootStrap, 18100);
    }

    private static void bind(final ServerBootstrap serverBootstrap, final int port) {
        serverBootstrap.bind(port).addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if (future.isSuccess()) {
                    System.out.println("端口绑定成功！port：" + port);
                } else {
                    System.out.println("端口绑定失败！");
                    bind(serverBootstrap, port + 1);
                }
            }
        });
    }
}