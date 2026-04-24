/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.nettytion.websocket.client
 *
 *    Filename:    ClientWebSocketStatusAndEventHandler
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
 *    Create at:   2026/4/24 09:59
 *
 *    Revision:
 *
 *    2026/4/24 09:59
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.nettytion.websocket.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.handler.timeout.IdleStateEvent;
import lombok.extern.slf4j.Slf4j;
import wang.zhigang.vision.nettytion.websocket.ChannelAttrConstants;
import wang.zhigang.vision.nettytion.websocket.HttpRequestHandler;

/**
 * ClientWebSocketStatusAndEventHandler
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/4/24 09:59
 */
@Slf4j
public class ClientWebSocketStatusAndEventHandler extends ChannelInboundHandlerAdapter {
    private static final int MAX_UN_PONG_COUNT = 3; // 【关键】连续3次Ping无响应 → 关闭
    // 连续未收到Pong的计数器
    private int unPongCount = 0;

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
        if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
            ctx.channel().attr(ChannelAttrConstants.HANDSHAKE_COMPLETED).set(true);
        } else if (evt instanceof IdleStateEvent && Boolean.TRUE.equals(ctx.channel().attr(ChannelAttrConstants.HANDSHAKE_COMPLETED).get())) {
            if (unPongCount < MAX_UN_PONG_COUNT) {
                unPongCount++;
                ctx.writeAndFlush(new PingWebSocketFrame());
                log.warn("【客户端】空闲超时，主动发送 Ping");
            } else {
                log.error("【客户端】连续 {} 次Ping无响应，主动关闭连接！", MAX_UN_PONG_COUNT);
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