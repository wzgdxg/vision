/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.protocol.command
 *
 *    Filename:    Command
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
 *    Create at:   2026/2/11 11:21
 *
 *    Revision:
 *
 *    2026/2/11 11:21
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.flash.protocol.command;

/**
 * Command
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/2/11 11:21
 */
public enum Command {

    /**
     * 登录
     */
    LOGIN_REQUEST(Byte.parseByte("1")),
    LOGIN_RESPONSE(Byte.parseByte("2"));

    private final Byte value;

    Command(Byte command) {
        this.value = command;
    }

    public Byte getValue() {
        return value;
    }
}