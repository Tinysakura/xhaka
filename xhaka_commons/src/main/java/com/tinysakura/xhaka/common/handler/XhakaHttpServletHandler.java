package com.tinysakura.xhaka.common.handler;

import com.tinysakura.xhaka.common.filter.XhakaFilterChainFactory;
import com.tinysakura.xhaka.common.gateway.config.XhakaGateWayConfig;
import com.tinysakura.xhaka.common.gateway.constant.XhakaHttpHeaderConstant;
import com.tinysakura.xhaka.common.gateway.discovery.core.GatewaySlaveChannelPool;
import com.tinysakura.xhaka.common.gateway.future.XhakaFuture;
import com.tinysakura.xhaka.common.gateway.remote.route.ServerDispatcher;
import com.tinysakura.xhaka.common.gateway.remote.util.GlobalCounterUtil;
import com.tinysakura.xhaka.common.servlet.request.XhakaHttpServletRequest;
import com.tinysakura.xhaka.common.servlet.response.XhakaHttpServletResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpVersion;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

/**
 * 处理XhakaHttpServletRequest(filterChain)
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/21
 */

public class XhakaHttpServletHandler extends SimpleChannelInboundHandler<XhakaHttpServletRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, XhakaHttpServletRequest xhakaHttpServletRequest) throws Exception {
        FilterChain filterChain = XhakaFilterChainFactory.createFilterChain(xhakaHttpServletRequest);
        filterChain.doFilter(xhakaHttpServletRequest, xhakaHttpServletRequest.getHttpServletResponse());

        // todo 网关转发部分逻辑
        String dispatcherServerName = ServerDispatcher.getDispatcherServerName(xhakaHttpServletRequest);
        Channel slaveChannel = GatewaySlaveChannelPool.getInstance().getSlaveChannelByLoadBalanceStrategy(XhakaGateWayConfig.getInstance().getLoadBalance(), dispatcherServerName);

        if (slaveChannel == null) {
            ctx.writeAndFlush(new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND));
        }

        Long xhakaRequestSequence = GlobalCounterUtil.getXhakaRequestSequence();
        xhakaHttpServletRequest.addHeader(XhakaHttpHeaderConstant.HTTP_HEADER_XHAKA_ID, xhakaRequestSequence);
        slaveChannel.writeAndFlush(xhakaHttpServletRequest);

        XhakaFuture future = new XhakaFuture(xhakaRequestSequence);
        /*
         * thread block util gateway slave return response
         * this channel run in standard business thread group, not affect io thread group
         */
        FullHttpResponse fullHttpResponse = future.get(XhakaGateWayConfig.getInstance().getSlaveResponseTimeout());
        HttpServletResponse httpServletResponse = new XhakaHttpServletResponse(xhakaHttpServletRequest, fullHttpResponse, ctx);

        httpServletResponse.getOutputStream().flush();
    }
}