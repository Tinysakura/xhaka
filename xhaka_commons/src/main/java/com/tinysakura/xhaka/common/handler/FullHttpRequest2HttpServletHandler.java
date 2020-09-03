package com.tinysakura.xhaka.common.handler;

import com.tinysakura.xhaka.common.servlet.request.XhakaHttpServletRequest;
import com.tinysakura.xhaka.common.servlet.response.XhakaHttpServletResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.Charset;

/**
 * 将FullHttp Request包装成HttpServletRequest实现
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/21
 */
@Slf4j
public class FullHttpRequest2HttpServletHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

    public static final FullHttpResponse DEFAULT_CONTINUE_FULLHTTP_RESPONSE = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.CONTINUE);


    @Override
    protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest fullHttpRequest) throws Exception {
        log.info("FullHttpRequest2HttpServletHandler, fullHttpRequest:{}", fullHttpRequest);
        XhakaHttpServletRequest xhakaHttpServletRequest = new XhakaHttpServletRequest(fullHttpRequest, ctx);

//        byte[] a = new byte[fullHttpRequest.content().readableBytes()];
//        fullHttpRequest.content().readBytes(a);
//        System.out.println(new String(a, Charset.forName("UTF-8")));

        XhakaHttpServletResponse xhakaHttpServletResponse = new XhakaHttpServletResponse(xhakaHttpServletRequest, ctx);
        xhakaHttpServletRequest.setHttpServletResponse(xhakaHttpServletResponse);

        if (HttpUtil.is100ContinueExpected(fullHttpRequest)) {
            ctx.write(DEFAULT_CONTINUE_FULLHTTP_RESPONSE, ctx.voidPromise());
        }

        ctx.fireChannelRead(xhakaHttpServletRequest);
    }


}