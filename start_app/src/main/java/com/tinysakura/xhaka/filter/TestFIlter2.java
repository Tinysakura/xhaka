package com.tinysakura.xhaka.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import java.io.IOException;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/10/12
 */
@Slf4j
public class TestFIlter2 implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        log.info("TestFilter2:{} before", request);

        chain.doFilter(request, response);

        log.info("TestFilter2:{} before", request);
    }

    @Override
    public void destroy() {

    }
}