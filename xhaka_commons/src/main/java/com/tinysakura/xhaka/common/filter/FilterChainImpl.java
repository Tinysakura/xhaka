package com.tinysakura.xhaka.common.filter;

import javax.servlet.*;
import java.io.IOException;
import java.util.LinkedList;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/25
 */
public class FilterChainImpl implements FilterChain {

    private LinkedList<Filter> filterLinkedList;

    public FilterChainImpl() {
        this.filterLinkedList = new LinkedList<>();
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
        Filter filter = filterLinkedList.poll();
        if (filter != null) {
            filter.doFilter(request, response, this);
        }
    }

    public void addFilter(Filter filter) {
        this.filterLinkedList.addLast(filter);
    }
}