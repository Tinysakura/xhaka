package com.tinysakura.xhaka.common.handler;

import com.tinysakura.xhaka.common.gateway.constant.XhakaHttpHeaderConstant;
import com.tinysakura.xhaka.common.servlet.request.XhakaHttpServletRequest;
import com.tinysakura.xhaka.common.servlet.response.XhakaHttpServletResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

/**
 * 将FullHttp Request包装成HttpServletRequest实现
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/21
 */
@Slf4j
public class FullHttpRequest2HttpServletHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final FullHttpResponse DEFAULT_CONTINUE_FULLHTTP_RESPONSE = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) {
        log.info("FullHttpRequest2HttpServletHandler, fullHttpRequest:{}", fullHttpRequest);
        XhakaHttpServletRequest xhakaHttpServletRequest = new XhakaHttpServletRequest(fullHttpRequest, ctx);

        XhakaHttpServletResponse xhakaHttpServletResponse = new XhakaHttpServletResponse(xhakaHttpServletRequest, ctx);
        // 设置xhakaid（slave端逻辑，salve和gateway复用了同一个ChannelHandler）
        if (StringUtils.isNotEmpty(xhakaHttpServletRequest.getHeader(XhakaHttpHeaderConstant.HTTP_HEADER_XHAKA_ID))) {
            xhakaHttpServletResponse.addHeader(XhakaHttpHeaderConstant.HTTP_HEADER_XHAKA_ID, fullHttpRequest.headers().get(XhakaHttpHeaderConstant.HTTP_HEADER_XHAKA_ID));
        }
        xhakaHttpServletRequest.setHttpServletResponse(xhakaHttpServletResponse);

        if (HttpUtil.is100ContinueExpected(fullHttpRequest)) {
            ctx.write(DEFAULT_CONTINUE_FULLHTTP_RESPONSE, ctx.voidPromise());
        }

        fullHttpRequest.retain();
        ctx.fireChannelRead(xhakaHttpServletRequest);
    }


}