package com.tinysakura.xhaka.common.filter;

import io.netty.channel.ChannelHandlerContext;

import javax.servlet.*;
import java.io.IOException;
import java.util.LinkedList;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/25
 */
public class FilterChainImpl implements FilterChain {

    private LinkedList<Filter> filterLinkedList;

    private Servlet servlet;

    private volatile boolean servletInvoked = false;

//    private static ThreadLocal<ServletRequest> LastHttpServletRequest;
//
//    private static ThreadLocal<ServletResponse> LastHttpServletResponse;
//
//    static {
//        LastHttpServletRequest = new ThreadLocal<>();
//        LastHttpServletResponse = new ThreadLocal<>();
//    }

    public FilterChainImpl(ChannelHandlerContext ctx) {
        this.filterLinkedList = new LinkedList<>();
        this.servlet = new XhakaServlet(ctx);
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        Filter filter = filterLinkedList.poll();

        if (filter != null) {
            filter.doFilter(request, response, this);
        }

        if (!servletInvoked) {
            servlet.service(request, response);
            servletInvoked = true;
        }
    }

    public void addFilter(Filter filter) {
        this.filterLinkedList.addLast(filter);
    }

//    public void setLastServletRequest(ServletRequest servletRequest) {
//
//    }
//
//    public void setLastServletResponse() {
//
//    }
}