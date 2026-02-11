/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision
 *
 *    Filename:    IOServer
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
 *    Create at:   2026/2/6 16:49
 *
 *    Revision:
 *
 *    2026/2/6 16:49
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.bio;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * IOServer
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/2/6 16:49
 */
public class IOServer {

    public static void main(String[] args) throws IOException {
        ServerSocket serverSocket = new ServerSocket(18100);
        new Thread(()->{
            while (true) {
                try{
                    Socket socket = serverSocket.accept();
                    new Thread(()->{
                        int len;
                        byte[] data = new byte[1024];
                        try (InputStream inputStream = socket.getInputStream();) {
                            while ((len = inputStream.read(data)) != -1) {
                                System.out.println(new String(data, 0, len));
                            }
                        } catch (IOException e) {
//                            throw new RuntimeException(e);
                        }

                    }).start();
                } catch (IOException e) {
//                    throw new RuntimeException(e);
                }
            }
        }).start();

    }

}