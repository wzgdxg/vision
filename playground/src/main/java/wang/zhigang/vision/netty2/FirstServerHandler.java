/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.netty2
 *
 *    Filename:    FirstServerHandler
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
 *    Create at:   2026/2/10 10:52
 *
 *    Revision:
 *
 *    2026/2/10 10:52
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.netty2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * FirstServerHandler
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/2/10 10:52
 */
public class FirstServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes("你已连接到服务器，请说话！".getBytes(StandardCharsets.UTF_8));
        ctx.channel().writeAndFlush(buffer);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println(new Date()+": 服务端读到数据 -> " + byteBuf.toString(StandardCharsets.UTF_8));

        System.out.println(new Date() + ": 服务端写出数据");
        ByteBuf out = getByteBuf(ctx);
        ctx.channel().writeAndFlush(out);
    }

    private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
        byte[] bytes = "你好，欢迎关注我的微信公众号，《大帅哥博客》！".getBytes(StandardCharsets.UTF_8);
        ByteBuf buffer = ctx.alloc().buffer();
        buffer.writeBytes(bytes);
        return buffer;
    }
}