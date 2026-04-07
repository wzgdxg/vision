/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.flash.protocol.response
 *
 *    Filename:    LoginResponsePacket
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
 *    Create at:   2026/3/24 14:48
 *
 *    Revision:
 *
 *    2026/3/24 14:48
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.flash.protocol.response;

import lombok.Data;
import wang.zhigang.vision.flash.protocol.Packet;
import wang.zhigang.vision.flash.protocol.command.Command;

/**
 * LoginResponsePacket
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/3/24 14:48
 */
@Data
public class LoginResponsePacket extends Packet {

    private boolean success;

    private String reason;

    private String userId;

    private String userName;


    @Override
    public Command getCommand() {
        return Command.LOGIN_RESPONSE;
    }
}