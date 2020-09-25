package com.tinysakura.xhaka.common.gateway.handler;

import com.tinysakura.xhaka.common.gateway.future.XhakaFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * 网关侧处理代理侧的响应
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */
@Slf4j
public class SlaveXhakaHttpServletResponsehHandler extends SimpleChannelInboundHandler<FullHttpResponse> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, FullHttpResponse fullHttpResponse) throws Exception {
        // 唤醒网关向代理服务发起请求的业务线程，拿到代理服务的响应结果
        String xhakaId = fullHttpResponse.headers().get("xhaka-id");
        log.info("receive xhaka-id:{} repsonse, now:{}", xhakaId, System.currentTimeMillis());

        XhakaFuture.received(fullHttpResponse);
    }
}