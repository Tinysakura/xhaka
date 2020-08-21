package com.tinysakura.xhaka.common.handler;

import com.tinysakura.xhaka.common.servlet.request.XhakaHttpServletRequest;
import com.tinysakura.xhaka.common.servlet.response.XhakaHttpServletResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;

/**
 * 将FullHttp Request包装成HttpServletRequest实现
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/21
 */

public class FullHttpRequest2HttpServletHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final FullHttpResponse DEFAULT_CONTINUE_FULLHTTP_RESPONSE = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {

        XhakaHttpServletRequest xhakaHttpServletRequest = new XhakaHttpServletRequest(fullHttpRequest, ctx);

        XhakaHttpServletResponse xhakaHttpServletResponse = new XhakaHttpServletResponse(xhakaHttpServletRequest, ctx);
        xhakaHttpServletRequest.setHttpServletResponse(xhakaHttpServletResponse);

        if (HttpUtil.is100ContinueExpected(fullHttpRequest)) {
            ctx.write(DEFAULT_CONTINUE_FULLHTTP_RESPONSE, ctx.voidPromise());
        }

        ctx.fireChannelRead(xhakaHttpServletRequest);
    }


}