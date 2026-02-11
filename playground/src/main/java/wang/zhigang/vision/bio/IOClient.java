/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision
 *
 *    Filename:    IOClient
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
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;

/**
 * IOClient
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/2/6 16:49
 */
public class IOClient {

    public static void main(String[] args) {
        new Thread(()->{
            try (Socket socket = new Socket("127.0.0.1", 18100)) {
                while (true) {
                    try {
                        socket.getOutputStream().write((new Date() + ": hello world").getBytes());
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }

            } catch (UnknownHostException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).start();
    }

}