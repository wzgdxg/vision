/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.serialize
 *
 *    Filename:    PacketCodeC
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
 *    Create at:   2026/3/17 10:24
 *
 *    Revision:
 *
 *    2026/3/17 10:24
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.flash.serialize;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import wang.zhigang.vision.flash.protocol.Packet;
import wang.zhigang.vision.flash.protocol.command.Command;
import wang.zhigang.vision.flash.protocol.request.LoginRequestPacket;

/**
 * PacketCodeC
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/3/17 10:24
 */
public class PacketCodeC {

    public static final PacketCodeC INSTANCE = new PacketCodeC();

    private static final int MAGIC_NUMBER = 0x12345678;

    /**
     * 使用全局静态默认分配器ByteBufAllocator分配ByteBuf，编码数据包
     * @param packet
     * @return
     */
    public ByteBuf encode(Packet packet) {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer();
        writeByteBufInfo(byteBuf, packet);
        return byteBuf;
    }

    /**
    * 使用指定的ByteBuf编码数据包
     * 比如当前 Channel 的分配器
     * @param byteBuf
     * @param packet
     */
    public void encode(ByteBuf byteBuf, Packet packet) {
        writeByteBufInfo(byteBuf, packet);
    }

    private void writeByteBufInfo(ByteBuf byteBuf, Packet packet) {
        byte[] bytes = Serializer.DEFAULT.serialize(packet);
        byteBuf.writeInt(MAGIC_NUMBER);
        byteBuf.writeByte(packet.getVersion());
        byteBuf.writeByte(Serializer.DEFAULT.getSerializerAlgorithm());
        byteBuf.writeByte(packet.getCommand().getValue());
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
    }

    public Packet decode(ByteBuf byteBuf) {
        //跳过魔法数
        byteBuf.skipBytes(4);
        //跳过版本号
        byteBuf.skipBytes(1);
        //跳过序列化算法
        byte serializerAlgorithm = byteBuf.readByte();
        //指令
        byte command = byteBuf.readByte();
        //数据包长度
        int length = byteBuf.readInt();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
        Class<? extends Packet> requestType = getRequestType(command);
        Serializer serializer = getSerializer(serializerAlgorithm);
        if (requestType != null && serializer != null) {
            return serializer.deserialize(requestType, bytes);
        }

        return null;
    }

    private Class<? extends Packet> getRequestType(byte command) {
        if (command == Command.LOGIN_REQUEST.getValue()) {
            return LoginRequestPacket.class;
        }
        return null;
    }

    private Serializer getSerializer(byte serializerAlgorithm) {
        switch (serializerAlgorithm) {
            case SerializerAlgorithm.JSON:
                return Serializer.DEFAULT;
            default:
                return null;
        }
    }
}