package com.demo.gateway.common;

import io.netty.buffer.Unpooled;
import io.netty.handler.codec.http.*;

import java.util.Map;

/**
 * @author Yannis
 */
public class CreateRequest {

    public static FullHttpRequest create(FullHttpRequest fullHttpRequest) {
        return new DefaultFullHttpRequest(
                HttpVersion.HTTP_1_1, HttpMethod.GET, fullHttpRequest.uri(), Unpooled.EMPTY_BUFFER);
    }

    public static FullHttpRequest create(String method, String uri, String version) {
        HttpMethod httpMethod = HttpMethod.valueOf(method);
        HttpVersion httpVersion = HttpVersion.valueOf(version);
        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, httpMethod, uri, Unpooled.EMPTY_BUFFER);
    }

    public static FullHttpRequest create(String method, String uri, String version, String content, Map<String, String> headers) {
        HttpMethod httpMethod = HttpMethod.valueOf(method);
        HttpVersion httpVersion = HttpVersion.valueOf(version);
        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpHeaders.set(entry.getKey(), entry.getValue());
        }
        DefaultFullHttpRequest request = null;
        try {
//            request = new DefaultFullHttpRequest(httpVersion, httpMethod, uri, Unpooled.EMPTY_BUFFER);
            request = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, httpMethod, uri, Unpooled.wrappedBuffer(content.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }
        request.headers().add(httpHeaders);
//        request.headers().set(HttpHeaders.Names.HOST, "127.0.0.1");
//        request.headers().set(HttpHeaders.Names.CONNECTION, HttpHeaders.Values.KEEP_ALIVE);
//        request.headers().set(HttpHeaders.Names.CONTENT_LENGTH, request.content().readableBytes());
//        request.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED);
        return request;
    }


}
