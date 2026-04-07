/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.flash.handler
 *
 *    Filename:    ClientHandler
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
 *    Create at:   2026/3/17 11:17
 *
 *    Revision:
 *
 *    2026/3/17 11:17
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.flash.handler.client;

import cn.hutool.core.date.DateUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import wang.zhigang.vision.flash.protocol.Packet;
import wang.zhigang.vision.flash.protocol.request.LoginRequestPacket;
import wang.zhigang.vision.flash.protocol.response.LoginResponsePacket;
import wang.zhigang.vision.flash.serialize.PacketCodeC;

import java.util.UUID;

/**
 * ClientHandler
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/3/17 11:17
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println(DateUtil.date() + ": 客户端开始登录");

        LoginRequestPacket loginRequestPacket = new LoginRequestPacket();
        loginRequestPacket.setUserId(UUID.randomUUID().toString());
        loginRequestPacket.setUsername("flash");
        loginRequestPacket.setPassword("pwd");

        ByteBuf buffer = ctx.alloc().ioBuffer();
        PacketCodeC.INSTANCE.encode(buffer, loginRequestPacket);
        ctx.channel().writeAndFlush(buffer);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf) msg;
        Packet packet = PacketCodeC.INSTANCE.decode(byteBuf);
        if (packet instanceof LoginResponsePacket) {
            LoginResponsePacket loginResponsePacket = (LoginResponsePacket) packet;
            if(loginResponsePacket.isSuccess()) {
                System.out.println(DateUtil.date() + ": 客户端登录成功");
            } else {
                System.out.println(DateUtil.date() + ": 客户端登录失败，原因：" + loginResponsePacket.getReason());
            }
        }
    }
}