package com.tinysakura.xhaka.common.filter;

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
import io.netty.handler.codec.http.*;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import java.io.IOException;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/9/21
 */
@Slf4j
public class XhakaServlet implements Servlet {

    private ChannelHandlerContext ctx;

    public XhakaServlet(ChannelHandlerContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {

    }

    @Override
    public ServletConfig getServletConfig() {
        return null;
    }

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
        XhakaHttpServletRequest xhakaHttpServletRequest = (XhakaHttpServletRequest) req;

        // 网关转发部分逻辑
        String dispatcherServerName = ServerDispatcher.getDispatcherServerName(xhakaHttpServletRequest);
        Channel slaveChannel = GatewaySlaveChannelPool.getInstance().getSlaveChannelByLoadBalanceStrategy(XhakaGateWayConfig.getInstance().getLoadBalance(), dispatcherServerName);
        log.info("thread:{} obtain slaveChannel time : {}", Thread.currentThread().getName(), System.currentTimeMillis());

        if (slaveChannel == null) {
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
            HttpUtil.setContentLength(response, 0);
            ctx.writeAndFlush(response);
            return;
        }

        Long xhakaRequestSequence = GlobalCounterUtil.getXhakaRequestSequence();
        xhakaHttpServletRequest.addHeader(XhakaHttpHeaderConstant.HTTP_HEADER_XHAKA_ID, xhakaRequestSequence);
        slaveChannel.writeAndFlush(xhakaHttpServletRequest.getOriginalRequest().copy());

        XhakaFuture future = new XhakaFuture(xhakaRequestSequence);
        /*
         * thread block util gateway slave return response
         * this channel run in standard business thread group, not affect io thread group
         */
        FullHttpResponse fullHttpResponse = null;
        try {
            String xhakaId = xhakaHttpServletRequest.getHeader("xhaka-id");
            log.info("threadName:{}", Thread.currentThread().getName());
            log.info("future wait to get xhaka-id:{} request, now:{}", xhakaId, System.currentTimeMillis());
            fullHttpResponse = future.get(XhakaGateWayConfig.getInstance().getSlaveResponseTimeout());
            log.info("future already get xhaka-id:{} request, now:{}", xhakaId, System.currentTimeMillis());
        } catch (XhakaSlaveTimeoutException e) {
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.REQUEST_TIMEOUT);
            HttpUtil.setContentLength(response, 0);
            ctx.writeAndFlush(response);
            return;
        } catch (Exception e) {
            DefaultFullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.INTERNAL_SERVER_ERROR);
            HttpUtil.setContentLength(response, 0);
            ctx.writeAndFlush(response);
            return;
        }

        // 用经由代理服务处理得到的response代替原来的response
        assert res instanceof XhakaHttpServletResponse;
        ((XhakaHttpServletResponse) res).replaceOriginResponse(fullHttpResponse);
    }

    @Override
    public String getServletInfo() {
        return null;
    }

    @Override
    public void destroy() {

    }
}