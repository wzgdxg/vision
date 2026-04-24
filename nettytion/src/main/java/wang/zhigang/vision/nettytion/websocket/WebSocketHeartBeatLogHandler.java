/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.nettytion.websocket
 *
 *    Filename:    WebSocketHeartBeatLogHandler
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
 *    Create at:   2026/4/22 11:40
 *
 *    Revision:
 *
 *    2026/4/22 11:40
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.nettytion.websocket;

import io.netty.channel.ChannelHandlerAdapter;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import lombok.extern.slf4j.Slf4j;

/**
 * WebSocketHeartBeatLogHandler
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/4/22 11:40
 */
@Slf4j
public class WebSocketHeartBeatLogHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object frame) {
        // 打印日志，不做应答，应答交给后面的官方处理器
        if (frame instanceof PingWebSocketFrame) {
            System.out.println("收到客户端 Ping");
        }
        if (frame instanceof PongWebSocketFrame) {
            System.out.println("收到客户端 Pong");
        }
        // 必须向下传递
        ctx.fireChannelRead(frame);
    }

}