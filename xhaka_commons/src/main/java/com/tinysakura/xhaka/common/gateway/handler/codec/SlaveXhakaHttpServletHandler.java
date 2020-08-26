package com.tinysakura.xhaka.common.gateway.handler.codec;

import com.tinysakura.xhaka.common.servlet.request.XhakaHttpServletRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 让被代理方的servlet容器处理请求
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/26
 */

public class SlaveXhakaHttpServletHandler extends SimpleChannelInboundHandler<XhakaHttpServletRequest> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, XhakaHttpServletRequest xhakaHttpServletRequest) throws Exception {

    }
}