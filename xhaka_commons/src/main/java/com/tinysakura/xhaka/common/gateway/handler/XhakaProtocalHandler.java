package com.tinysakura.xhaka.common.gateway.handler;

import com.tinysakura.xhaka.common.protocal.Xhaka;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/20
 */

public class XhakaProtocalHandler extends SimpleChannelInboundHandler<Xhaka> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Xhaka xhaka) throws Exception {

    }
}