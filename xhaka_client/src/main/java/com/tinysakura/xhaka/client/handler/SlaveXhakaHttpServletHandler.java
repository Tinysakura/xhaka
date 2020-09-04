package com.tinysakura.xhaka.client.handler;

import com.tinysakura.xhaka.client.context.TomcatServletContext;
import com.tinysakura.xhaka.common.servlet.request.XhakaHttpServletRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.AsyncDispatcher;

/**
 * 让被代理方的servlet容器处理请求
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/26
 */
@Slf4j
public class SlaveXhakaHttpServletHandler extends SimpleChannelInboundHandler<XhakaHttpServletRequest> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, XhakaHttpServletRequest xhakaHttpServletRequest) throws Exception {
        log.info("SlaveXhakaHttpServletHandler, xhakaHttpServletRequest:{}", xhakaHttpServletRequest);

        // 根据被代理服务的servlet容器类型处理请求
        // 目前只支持tomcat
        if (TomcatServletContext.getInstance().isCurrentWebServer()) {
            AsyncDispatcher dispatch = TomcatServletContext.getInstance().getDispatch(xhakaHttpServletRequest.getRequestURI());
            dispatch.dispatch(xhakaHttpServletRequest, xhakaHttpServletRequest.getHttpServletResponse());

            channelHandlerContext.channel().writeAndFlush(xhakaHttpServletRequest.getHttpServletResponse());
        }
    }
}