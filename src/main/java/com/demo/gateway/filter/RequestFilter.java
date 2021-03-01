package com.demo.gateway.filter;

import io.netty.handler.codec.http.HttpRequest;

/**
 * @author Yannis
 */
public interface RequestFilter {

    /**
     * 对请求进行处理
     * @param request 请求
     */
    void filter(HttpRequest request);
}
