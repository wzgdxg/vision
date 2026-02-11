/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.common
 *
 *    Filename:    Packet
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
 *    Create at:   2026/2/11 11:15
 *
 *    Revision:
 *
 *    2026/2/11 11:15
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.protocol;

import lombok.Data;

/**
 * Packet
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/2/11 11:15
 */
@Data
public abstract class Packet {

    /**
     * 协议版本
     */
    private Byte version = 1;

    /**
     * 指令
     */
    public abstract Byte getCommand();
}