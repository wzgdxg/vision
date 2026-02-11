/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.protocol.request
 *
 *    Filename:    LoginRequestPacket
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
 *    Create at:   2026/2/11 11:31
 *
 *    Revision:
 *
 *    2026/2/11 11:31
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.protocol.request;

import wang.zhigang.vision.protocol.Packet;

import static wang.zhigang.vision.protocol.command.Command.LOGIN_REQUEST;

/**
 * LoginRequestPacket
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/2/11 11:31
 */
public class LoginRequestPacket extends Packet {

    private Integer userId;

    private String username;

    private String password;

    @Override
    public Byte getCommand() {
        return LOGIN_REQUEST;
    }
}