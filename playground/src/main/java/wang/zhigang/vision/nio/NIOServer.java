/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.nio
 *
 *    Filename:    NIOServer
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
 *    Create at:   2026/2/6 17:47
 *
 *    Revision:
 *
 *    2026/2/6 17:47
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.nio;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.Iterator;
import java.util.Set;

/**
 * NIOServer
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/2/6 17:47
 */
public class NIOServer {

    public static void main(String[] args) throws IOException {
        Selector serverSelector = Selector.open();
        Selector clientSelector = Selector.open();

        new Thread(()->{
            try{
                ServerSocketChannel listenerChannel = ServerSocketChannel.open();
                listenerChannel.socket().bind(new InetSocketAddress(18100));
                listenerChannel.configureBlocking(false);
                listenerChannel.register(serverSelector, SelectionKey.OP_ACCEPT);
                while (true) {
                    //检测是否有新连接，1值阻塞1ms
                    if (serverSelector.select(1) > 0) {
                        Set<SelectionKey> set = serverSelector.selectedKeys();
                        Iterator<SelectionKey> keyIterator = set.iterator();
                        while (keyIterator.hasNext()) {
                            SelectionKey key = keyIterator.next();
                            if (key.isAcceptable()) {
                                try{
                                    //每来一个新连接，不需要创建一个线程，而是直接注册到clientSelector
                                    SocketChannel clientChannel = ((ServerSocketChannel) key.channel()).accept();
                                    clientChannel.configureBlocking(false);
                                    clientChannel.register(clientSelector, SelectionKey.OP_READ);
                                }finally {
                                    //关键：移除已处理的Key，避免下次select()重复处理
                                    keyIterator.remove();
                                }
                            }

                        }
                    }
                }

            } catch (ClosedChannelException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();

        new Thread(()->{
            try{
                while (true) {
                    if (clientSelector.select(1) > 0) {
                        Set<SelectionKey> set = clientSelector.selectedKeys();
                        Iterator<SelectionKey> keyIterator = set.iterator();
                        while (keyIterator.hasNext()) {
                            SelectionKey key = keyIterator.next();

                            if (key.isReadable()) {
                                try{
                                    SocketChannel clientChannel = (SocketChannel) key.channel();
                                    ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                                    clientChannel.read(byteBuffer);
                                    byteBuffer.flip();
                                    System.out.println(Charset.defaultCharset().newDecoder().decode(byteBuffer).toString());
                                } catch (CharacterCodingException e) {
                                    throw new RuntimeException(e);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                } finally {
                                    //关键：移除已处理的Key，避免下次select()重复处理
                                    keyIterator.remove();
                                    key.interestOps(SelectionKey.OP_READ);
                                }
                            }

                        }
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }
}