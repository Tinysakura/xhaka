package com.tinysakura.xhaka.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.Charset;

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
        log.info("TestFilter:{} before", request);

        //response.getOutputStream().write("test filter".getBytes(Charset.forName("UTF-8")));
        //response.getOutputStream().flush();
        ((HttpServletResponse) response).addHeader("yys", "tql");
        Cookie cookie = new Cookie("memberId", "12345");
        cookie.setDomain("localhost:8989");
        cookie.setMaxAge(60 * 60 * 24);
        ((HttpServletResponse) response).addCookie(cookie);
        chain.doFilter(request, response);
        log.info("TestFilter:{} after", request);
    }

    @Override
    public void destroy() {

    }
}