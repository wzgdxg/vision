/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.netty2
 *
 *    Filename:    FirstClientHandler
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
 *    Create at:   2026/2/10 10:46
 *
 *    Revision:
 *
 *    2026/2/10 10:46
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.netty2;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * FirstClientHandler
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/2/10 10:46
 */
public class FirstClientHandler extends ChannelInboundHandlerAdapter {

    //连接成功后调用
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        System.out.println(new Date() + ": 客户端写出数据");
        ByteBuf buffer = getByteBuf(ctx);
        ctx.channel().writeAndFlush(buffer);
    }

    private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
        ByteBuf buffer = ctx.alloc().buffer();
        byte[] bytes = "你好，大帅哥！".getBytes(StandardCharsets.UTF_8);
        //填充数据
        buffer.writeBytes(bytes);
        return buffer;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        System.out.println(new Date() + ": 客户端读到数据 -> " + byteBuf.toString(StandardCharsets.UTF_8));

    }
}