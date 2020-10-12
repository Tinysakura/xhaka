package com.tinysakura.xhaka.client.handler;

import com.tinysakura.xhaka.client.context.TomcatServletContext;
import com.tinysakura.xhaka.common.servlet.request.XhakaHttpServletRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.AsyncDispatcher;

import javax.servlet.ServletException;
import java.io.IOException;

/**
 * 让被代理方的servlet容器处理请求
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/26
 */
@Slf4j
public class SlaveXhakaHttpServletHandler extends SimpleChannelInboundHandler<XhakaHttpServletRequest> {


    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, XhakaHttpServletRequest xhakaHttpServletRequest) {
        //log.info("SlaveXhakaHttpServletHandler, xhakaHttpServletRequest:{}", xhakaHttpServletRequest);
        channelHandlerContext.executor().parent().execute(() -> {
            String xhakaId = xhakaHttpServletRequest.getHeader("xhaka-id");
            log.info("threadName:{}", Thread.currentThread().getName());
            log.info("SlaveXhakaHttpServletHandler begin xhaka-id:{}, now:{}", xhakaId, System.currentTimeMillis());

            // 根据被代理服务的servlet容器类型处理请求
            // 目前只支持tomcat
            if (TomcatServletContext.getInstance().isCurrentWebServer()) {
                AsyncDispatcher dispatch = (AsyncDispatcher) TomcatServletContext.getInstance().getDispatch(xhakaHttpServletRequest.getRequestURI());
                try {
                    dispatch.dispatch(xhakaHttpServletRequest, xhakaHttpServletRequest.getHttpServletResponse());
                } catch (Exception e) {
                    xhakaHttpServletRequest.getHttpServletResponse().setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR.code());
                }

                xhakaHttpServletRequest.getOriginalRequest().release();

                log.info("SlaveXhakaHttpServletHandler end xhaka-id:{}, now:{}", xhakaId, System.currentTimeMillis());
                channelHandlerContext.channel().eventLoop().execute(() -> {
                    channelHandlerContext.channel().writeAndFlush(xhakaHttpServletRequest.getHttpServletResponse().getOriginResponse());
                });
            }
        });
    }
}