package com.tinysakura.xhaka.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import java.io.IOException;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/25
 */
@Slf4j
public class TestFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("TestFilter:{}", request);
        chain.doFilter(request, response);
    }

    @Override
    public void destroy() {

    }
}