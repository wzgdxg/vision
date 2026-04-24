/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.nettytion.websocket
 *
 *    Filename:    WebSocketStatusAndEventHandler
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
 *    Create at:   2026/4/21 17:50
 *
 *    Revision:
 *
 *    2026/4/21 17:50
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.nettytion.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.group.ChannelGroup;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocketStatusAndEventHandler
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/4/21 17:50
 */
@Slf4j
public class WebSocketStatusAndEventHandler extends ChannelInboundHandlerAdapter {
    private static final int MAX_UN_PONG_COUNT = 3; // 【关键】连续3次Ping无响应 → 关闭
    private final ChannelGroup group;

    // 连续未收到Pong的计数器
    private int unPongCount = 0;

    public WebSocketStatusAndEventHandler(ChannelGroup group) {
        this.group = group;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof PongWebSocketFrame) {
            unPongCount = 0;
            ((PongWebSocketFrame) msg).release();
            return;
        }
        super.channelRead(ctx, msg);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt == WebSocketServerProtocolHandler.ServerHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            ctx.pipeline().remove(HttpRequestHandler.class);
            ctx.channel().attr(ChannelAttrConstants.HANDSHAKE_COMPLETED).set(true);
            group.writeAndFlush(new TextWebSocketFrame("Client " + ctx.channel() + " joined"));
            group.add(ctx.channel());
        } else if (evt instanceof IdleStateEvent && Boolean.TRUE.equals(ctx.channel().attr(ChannelAttrConstants.HANDSHAKE_COMPLETED).get())) {
            if (unPongCount < MAX_UN_PONG_COUNT) {
                unPongCount++;
                ctx.writeAndFlush(new PingWebSocketFrame());
                log.warn("【服务端】空闲超时，主动发送 Ping");
            } else {
                log.error("【服务端】连续 {} 次Ping无响应，主动关闭连接！", MAX_UN_PONG_COUNT);
                ctx.close(); // 代码主动关闭，不等系统RST
            }

        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        log.warn(ctx.channel() + " 断开连接");
        super.channelInactive(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        log.warn(ctx.channel() + " 连上了");
        super.channelActive(ctx);
    }
}