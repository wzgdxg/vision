/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.nettytion.websocket.client
 *
 *    Filename:    WebsocketClientHandler
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
 *    Create at:   2026/4/23 15:42
 *
 *    Revision:
 *
 *    2026/4/23 15:42
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.nettytion.websocket.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * WebsocketClientHandler
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/4/23 15:42
 */
@Slf4j
public class WebsocketClientHandler extends SimpleChannelInboundHandler<Object> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 2. 接收普通文本消息
        if (msg instanceof TextWebSocketFrame frame) {
            System.out.println("📩 收到服务端消息：" + frame.text());
        }
    }

    /**
     * 连接断开 → 触发自动重连
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        WebsocketClient.reconnect();
        super.channelInactive(ctx);
    }

    /**
     * 异常处理
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        System.err.println("🔥 客户端异常：" + cause.getMessage());
        ctx.close();
    }
}