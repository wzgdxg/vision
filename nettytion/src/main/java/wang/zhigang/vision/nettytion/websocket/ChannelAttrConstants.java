/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.nettytion.websocket
 *
 *    Filename:    ChannelAttrConstants
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
 *    Create at:   2026/4/21 17:28
 *
 *    Revision:
 *
 *    2026/4/21 17:28
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.nettytion.websocket;

import io.netty.util.AttributeKey;

/**
 * ChannelAttrConstants
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/4/21 17:28
 */
public class ChannelAttrConstants {
    public static final AttributeKey<Boolean> HANDSHAKE_COMPLETED = AttributeKey.valueOf("handshakeCompleted");
}