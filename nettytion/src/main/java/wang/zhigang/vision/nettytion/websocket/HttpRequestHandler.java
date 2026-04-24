/******************************************************************
 *
 *    Powered By hhdd.com.
 *
 *    Copyright (c) 2001-2023
 *    https://kada.hhdd.com/
 *
 *    Package:     wang.zhigang.vision.nettytion.websocket
 *
 *    Filename:    HttpRequestHandler
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
 *    Create at:   2026/4/17 10:00
 *
 *    Revision:
 *
 *    2026/4/17 10:00
 *        - first revision
 *
 *****************************************************************/
package wang.zhigang.vision.nettytion.websocket;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.handler.ssl.NotSslRecordException;
import lombok.extern.slf4j.Slf4j;

import javax.net.ssl.SSLHandshakeException;
import java.nio.charset.StandardCharsets;

/**
 * HttpRequestHandler
 *
 * @author wangzg
 * @version 1.0.0
 * @create 2026/4/17 10:00
 */
@Slf4j
public class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    private static String WS_URI;

    private final static String INDEX_CONTENT = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "<meta charset=\"UTF-8\">\n" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "<title>Document</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<h1>Hello World</h1>\n" +
            "</body>\n" +
            "</html>";
    private final static String CONTENT_404_HTML = "<!DOCTYPE html>\n" +
            "<html lang=\"en\">\n" +
            "<head>\n" +
            "<meta charset=\"UTF-8\">\n" +
            "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
            "<title>Document</title>\n" +
            "</head>\n" +
            "<body>\n" +
            "<h1>404 Not Found</h1>\n" +
            "</body>\n" +
            "</html>";

    private final static byte[] INDEX_CONTENT_BYTES = INDEX_CONTENT.getBytes(StandardCharsets.UTF_8);
    public static final int INDEX_CONTENT_BYTES_LEN = INDEX_CONTENT_BYTES.length;
    public static final ByteBuf INDEX_CONTENT_BYTE_BUF = Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(INDEX_CONTENT_BYTES));
    private final static byte[] CONTENT_404_HTML_BYTES = CONTENT_404_HTML.getBytes(StandardCharsets.UTF_8);
    public static final int CONTENT_404_HTML_BYTES_LEN = CONTENT_404_HTML_BYTES.length;
    public static final ByteBuf CONTENT_404_HTML_BYTE_BUF = Unpooled.unreleasableBuffer(Unpooled.wrappedBuffer(CONTENT_404_HTML_BYTES));

    public HttpRequestHandler(String wsUri) {
        WS_URI = wsUri;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) throws Exception {
        if(request.uri().equalsIgnoreCase(WS_URI)){
            ctx.fireChannelRead(request.retain());
            return;
        }

        int contentLength = CONTENT_404_HTML_BYTES_LEN;
        ByteBuf contentByteBuf = CONTENT_404_HTML_BYTE_BUF;
        HttpResponseStatus statusCode = HttpResponseStatus.NOT_FOUND;
        if (request.uri().equalsIgnoreCase("/") || request.uri().equalsIgnoreCase("/index.html")) {
            contentLength = INDEX_CONTENT_BYTES_LEN;
            contentByteBuf = INDEX_CONTENT_BYTE_BUF;
            statusCode = HttpResponseStatus.OK;
        }

        FullHttpResponse response = new DefaultFullHttpResponse(
                request.getProtocolVersion(),
                statusCode,
                contentByteBuf.duplicate()
        );
        response.headers()
                .set(HttpHeaderNames.CONTENT_TYPE, "text/html; charset=UTF-8")
                .set(HttpHeaderNames.CONTENT_LENGTH, contentLength);

        boolean keepAlive = HttpUtil.isKeepAlive(request);
        if (keepAlive) {
            response.headers().set(HttpHeaderNames.CONNECTION, HttpHeaderValues.KEEP_ALIVE);
            ctx.writeAndFlush(response);
        } else {
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE); //短链接发完关闭
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        if (containsCause(cause, NotSslRecordException.class)) {
            log.warn("拒绝非SSL/TLS明文连接");
        } else if(containsCause(cause, SSLHandshakeException.class)) {
            log.warn(" Received fatal alert: certificate_unknown");
        } else {
            log.error("通道异常关闭", cause);
            ctx.close();
        }

    }

    private boolean containsCause(Throwable cause, Class<? extends Throwable> targetClass) {
        for (Throwable ex = cause; ex != null; ex = ex.getCause()) {
            if (targetClass.isInstance(ex)) {
                return true;
            }
        }
        return false;
    }
}