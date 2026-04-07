/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.nettytion.plainoio
 *
 *    Filename:    PlainOioServer
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
 *    Create at:   2026/4/7 17:49
 *
 *    Revision:
 *
 *    2026/4/7 17:49
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.nettytion.plain;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * PlainOioServer
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/4/7 17:49
 */
public class PlainOioServer {

    public static void main(String[] args) throws IOException {
        new PlainOioServer().serve(8080);
    }

    public void serve(int port) throws IOException {
        final ServerSocket socket = new ServerSocket(port);
        for (; ; ) {
            final Socket clientSocket = socket.accept();
            System.out.println("Accepted connection from " + clientSocket);

            new Thread(new Runnable() {
                @Override
                public void run() {
                    OutputStream out = null;
                    try {
                        out = clientSocket.getOutputStream();
                        out.write("Hello, World!".getBytes());
                        out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }

                }
            }).start();

        }

    }

}