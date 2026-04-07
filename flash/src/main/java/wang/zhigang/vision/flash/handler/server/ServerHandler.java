/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.flash.handler.server
 *
 *    Filename:    ServerHandler
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
 *    Create at:   2026/3/24 14:38
 *
 *    Revision:
 *
 *    2026/3/24 14:38
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.flash.handler.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import wang.zhigang.vision.flash.protocol.Packet;
import wang.zhigang.vision.flash.protocol.request.LoginRequestPacket;
import wang.zhigang.vision.flash.protocol.response.LoginResponsePacket;
import wang.zhigang.vision.flash.serialize.PacketCodeC;

/**
 * ServerHandler
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/3/24 14:38
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf requestByteBuf = (ByteBuf) msg;
        Packet packet = PacketCodeC.INSTANCE.decode(requestByteBuf);

        if (packet instanceof LoginRequestPacket) {
            LoginRequestPacket loginRequestPacket = (LoginRequestPacket) packet;
            LoginResponsePacket loginResponsePacket = new LoginResponsePacket();
            loginResponsePacket.setVersion(packet.getVersion());
            if(valid(loginRequestPacket)) {
                //校验成功
                loginResponsePacket.setSuccess(true);
            } else {
                //校验失败
                loginResponsePacket.setReason("账号密码校验失败");
                loginResponsePacket.setSuccess(false);
            }
            ByteBuf responseByteBuf = ctx.alloc().ioBuffer();
            PacketCodeC.INSTANCE.encode(responseByteBuf, loginResponsePacket);
            ctx.channel().writeAndFlush(responseByteBuf);
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        // 连接关闭时的处理
        System.out.println("客户端连接关闭: " + ctx.channel().remoteAddress());
        // 可以在这里清理相关资源
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        // 异常处理
        System.err.println("服务器异常: " + cause.getMessage());
        ctx.close(); // 关闭连接
    }

    private boolean valid(LoginRequestPacket loginRequestPacket) {
        return true;
    }
}