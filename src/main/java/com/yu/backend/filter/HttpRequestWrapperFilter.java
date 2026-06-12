package com.yu.backend.filter;

import cn.hutool.http.ContentType;
import cn.hutool.http.Header;
import com.yu.backend.context.SpaceUserAuthContextHolder;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * JSON 请求体包装，支持拦截器与 Controller 重复读取 Body
 */
@Order(-1)
@Component
public class HttpRequestWrapperFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (!(request instanceof HttpServletRequest)) {
            chain.doFilter(request, response);
            return;
        }
        HttpServletRequest servletRequest = (HttpServletRequest) request;
        String contentType = servletRequest.getHeader(Header.CONTENT_TYPE.getValue());
        String uri = servletRequest.getRequestURI();
        boolean apiRequest = uri.contains("/api/") || uri.matches(".*/(picture|space|spaceUser|user)/.*");
        if (apiRequest && contentType != null && contentType.contains(ContentType.JSON.getValue())) {
            try {
                chain.doFilter(new RequestWrapper(servletRequest), response);
            } finally {
                SpaceUserAuthContextHolder.clear();
            }
        } else {
            chain.doFilter(request, response);
        }
    }
}
