/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.byteBuf
 *
 *    Filename:    ByteBufTest
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
 *    Create at:   2026/2/10 14:37
 *
 *    Revision:
 *
 *    2026/2/10 14:37
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.byteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

/**
 * ByteBufTest
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/2/10 14:37
 */
public class ByteBufTest {
    public static void main(String[] args) {
        ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(9, 100);
        buffer.writeBytes(new byte[]{1, 2, 3, 4});

        print("writeBytes(1,2,3,4)", buffer);

        buffer.writeInt(12);
        print("writeInt(12)", buffer);

        //写完后，满capacity，buffer不可写
        buffer.writeBytes(new byte[]{5});
        print("writeBytes(5)", buffer);

        //写的时候，发现满capacity，buffer不可写则扩容，扩容之后capacity改变
        buffer.writeBytes(new byte[]{6});
        print("writeBytes(6)", buffer);

        //get方法不改变读写指针
        System.out.println("getByte(3) return: " + buffer.getByte(3));
        System.out.println("getShort(3) return: " + buffer.getShort(3));
        System.out.println("getInt(3) return: " + buffer.getInt(3));
        print("getByte()", buffer);

        //set方法不改变读写指针
        buffer.setByte(buffer.readableBytes() + 1, 0);
        print("setByte()", buffer);

        //read方法改变读指针
        byte[] dst = new byte[buffer.readableBytes()];
        buffer.readBytes(dst);
        print("readBytes(" + dst.length + ")", buffer);

    }


    private static void print(String action, ByteBuf byteBuf) {
        System.out.println("after ===========" + action + "============");
        System.out.println("capacity(): " + byteBuf.capacity());
        System.out.println("maxCapacity(): " + byteBuf.maxCapacity());
        System.out.println("readerIndex(): " + byteBuf.readerIndex());
        System.out.println("readableBytes(): " + byteBuf.readableBytes());
        // 读写指针重合不可读
        System.out.println("isReadable(): " + byteBuf.isReadable());
        System.out.println("writerIndex(): " + byteBuf.writerIndex());
        System.out.println("writableBytes(): " + byteBuf.writableBytes());
        // capacity 和 写指针重合不可写 但是扩容之后还可写
        System.out.println("isWritable(): " + byteBuf.isWritable());
        System.out.println("maxWritableBytes(): " + byteBuf.maxWritableBytes());
        System.out.println();
    }
}