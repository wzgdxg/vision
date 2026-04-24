/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.nettytion.plainnio
 *
 *    Filename:    PlainNioServer
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
 *    Create at:   2026/4/7 17:56
 *
 *    Revision:
 *
 *    2026/4/7 17:56
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.nettytion.plain;

import ch.qos.logback.core.net.server.Client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * PlainNioServer
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/4/7 17:56
 */
public class PlainNioServer {

    public static void main(String[] args) throws IOException {
        new PlainNioServer().serve(8080);
    }


    public void serve(int port) throws IOException {
        ServerSocketChannel serverChannel = ServerSocketChannel.open();
        serverChannel.configureBlocking(false);
        ServerSocket ss = serverChannel.socket();
        InetSocketAddress address = new InetSocketAddress(port);
        ss.bind(address);
        Selector selector = Selector.open();
        serverChannel.register(selector, SelectionKey.OP_ACCEPT);
        final ByteBuffer msg = ByteBuffer.wrap("Hi!\r\n".getBytes());
        for (;; ) {
            selector.select();
            Set<SelectionKey> readKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterable = readKeys.iterator();
            while (iterable.hasNext()) {
                SelectionKey key = iterable.next();
                iterable.remove();
                try{
                    if (key.isAcceptable()) {
                        ServerSocketChannel sever = (ServerSocketChannel) key.channel();
                        SocketChannel client = sever.accept();
                        client.configureBlocking(false);
                        client.register(selector, SelectionKey.OP_WRITE|SelectionKey.OP_READ, msg.duplicate());
                        System.out.println("Accepted connection from " + client);
                    }

                    if(key.isWritable()){
                        SocketChannel client = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        while (buffer.hasRemaining()) {
                            if (client.write(buffer) == 0) {
                                break;
                            }
                        }
                        buffer.clear();
                        client.close();
                    }
                }catch (IOException e){
                    e.printStackTrace();
                    key.cancel();
                    key.channel().close();
                }finally {

                }

            }
        }
    }

}