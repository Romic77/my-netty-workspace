package org.example.netty.http;

import java.nio.charset.StandardCharsets;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

/**
 * @author romic
 * @date 2022-01-08
 * @description
 */
public class MyResponse {
    private ChannelHandlerContext ctx;
    private HttpRequest req;

    public MyResponse(ChannelHandlerContext ctx, HttpRequest req) {
        this.ctx = ctx;
        this.req = req;
    }

    public void write(String s) throws Exception {
        if (s == null || s.length() == 0) {
            return;
        }

        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK,
            Unpooled.wrappedBuffer(s.getBytes(StandardCharsets.UTF_8)));

        response.headers().set("Content-Type", "text/html");
        ctx.writeAndFlush(response);
        ctx.close();
    }

}
