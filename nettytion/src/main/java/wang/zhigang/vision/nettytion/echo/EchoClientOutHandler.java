/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.nettytion.echo
 *
 *    Filename:    EchoClientOutHandler
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
 *    Create at:   2026/3/26 09:51
 *
 *    Revision:
 *
 *    2026/3/26 09:51
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.nettytion.echo;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import lombok.extern.slf4j.Slf4j;

/**
 * EchoClientOutHandler
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/3/26 09:51
 */
@Slf4j
public class EchoClientOutHandler extends ChannelOutboundHandlerAdapter {

    /**
     * handler链 A到B  handler的exceptionCaught只处理当前handler内的异常，除非A的异常调用了
     * fireExceptionCaught方法
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("exceptionCaught", cause);
    }
}