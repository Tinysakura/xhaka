package com.tinysakura.xhaka.server.handler;

import com.tinysakura.xhaka.common.filter.XhakaFilterChainFactory;
import com.tinysakura.xhaka.common.gateway.config.XhakaGateWayConfig;
import com.tinysakura.xhaka.common.gateway.constant.XhakaHttpHeaderConstant;
import com.tinysakura.xhaka.common.gateway.context.client.GatewaySlaveChannelPool;
import com.tinysakura.xhaka.common.gateway.exception.XhakaSlaveTimeoutException;
import com.tinysakura.xhaka.common.gateway.future.XhakaFuture;
import com.tinysakura.xhaka.common.gateway.remote.route.ServerDispatcher;
import com.tinysakura.xhaka.common.gateway.remote.util.GlobalCounterUtil;
import com.tinysakura.xhaka.common.servlet.request.XhakaHttpServletRequest;
import com.tinysakura.xhaka.common.servlet.response.XhakaHttpServletResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

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
        FilterChain filterChain = XhakaFilterChainFactory.createFilterChain(xhakaHttpServletRequest, ctx);
        filterChain.doFilter(xhakaHttpServletRequest, xhakaHttpServletRequest.getHttpServletResponse());

//        // 网关转发部分逻辑
//        String dispatcherServerName = ServerDispatcher.getDispatcherServerName(xhakaHttpServletRequest);
//        Channel slaveChannel = GatewaySlaveChannelPool.getInstance().getSlaveChannelByLoadBalanceStrategy(XhakaGateWayConfig.getInstance().getLoadBalance(), dispatcherServerName);
//
//        if (slaveChannel == null) {
//            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
//            HttpUtil.setContentLength(response, 0);
//            ctx.writeAndFlush(response);
//            return;
//        }
//
//        Long xhakaRequestSequence = GlobalCounterUtil.getXhakaRequestSequence();
//        xhakaHttpServletRequest.addHeader(XhakaHttpHeaderConstant.HTTP_HEADER_XHAKA_ID, xhakaRequestSequence);
//        slaveChannel.writeAndFlush(xhakaHttpServletRequest.getOriginalRequest().copy());
//
//        XhakaFuture future = new XhakaFuture(xhakaRequestSequence);
//        /*
//         * thread block util gateway slave return response
//         * this channel run in standard business thread group, not affect io thread group
//         */
//        FullHttpResponse fullHttpResponse = null;
//        try {
//            fullHttpResponse = future.get(XhakaGateWayConfig.getInstance().getSlaveResponseTimeout());
//        } catch (XhakaSlaveTimeoutException e) {
//            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_TIMEOUT);
//            HttpUtil.setContentLength(response, 0);
//            ctx.writeAndFlush(response);
//            return;
//        }

        //fullHttpResponse.headers().set("transfer-encoding", "chunked");
        HttpServletResponse httpServletResponse = xhakaHttpServletRequest.getHttpServletResponse();

        httpServletResponse.getOutputStream().flush();
    }
}