package com.demo.gateway.aspect;

import com.demo.gateway.route.RouteTable;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.util.internal.StringUtil;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * 路由处理切面
 * 将转化得到的后台服务器请求地址直接设置在request头中
 *
 * @author Yannis
 */
@Aspect
@Component
public class RouteAspect {

    @Pointcut("@annotation(com.demo.gateway.annotation.RouteAnnotation)")
    public void clientExecute() {
    }

    @Before("clientExecute()")
    public void convert(JoinPoint point) {
        Object[] args = point.getArgs();
        FullHttpRequest request = (FullHttpRequest) args[0];
        String uri = request.uri();
        String real = RouteTable.getTargetUrl(uri);
        if (StringUtil.isNullOrEmpty(real)) {
            return;
        }
        request.setUri(real);
    }
}
