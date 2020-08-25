package com.tinysakura.xhaka.common.handler;

import com.tinysakura.xhaka.common.filter.FilterChainFactory;
import com.tinysakura.xhaka.common.servlet.request.XhakaHttpServletRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import javax.servlet.FilterChain;

/**
 * 处理XhakaHttpServletRequest(filterChain)
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/21
 */

public class XhakaHttpServletHandler extends SimpleChannelInboundHandler<XhakaHttpServletRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, XhakaHttpServletRequest xhakaHttpServletRequest) throws Exception {
        FilterChain filterChain = FilterChainFactory.createFilterChain(xhakaHttpServletRequest);
        filterChain.doFilter(xhakaHttpServletRequest, xhakaHttpServletRequest.getHttpServletResponse());

        // todo 网关转发部分逻辑
    }
}