package org.example.netty.http;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.QueryStringDecoder;

/**
 * @author romic
 * @date 2022-01-08
 * @description
 */
public class MyRequest {

    private ChannelHandlerContext ctx;
    private HttpRequest req;

    public MyRequest(ChannelHandlerContext ctx, HttpRequest req) {
        this.ctx = ctx;
        this.req = req;
    }

    public String getUrl() {
        return this.req.uri();
    }

    public String getMethod() {
        return this.req.method().name();
    }

    /**
     * 获取请求参数
     * 
     * @return java.util.Map<java.lang.String,java.util.List<java.lang.String>>
     * @author romic
     * @date 2022-01-08 17:10
     */
    public Map<String, List<String>> getParameters() {
        QueryStringDecoder decoder = new QueryStringDecoder(req.uri());
        return decoder.parameters();
    }

    public String getParameter(String name) {
        Map<String, List<String>> params = getParameters();
        List<String> param = params.get(name);
        if (Objects.isNull(param)) {
            return null;
        }
        return param.get(0);

    }

}
