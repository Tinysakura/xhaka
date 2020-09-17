package com.tinysakura.xhaka.common.gateway.handler;

import com.tinysakura.xhaka.common.gateway.context.client.HeartbeatPacemaker;
import com.tinysakura.xhaka.common.gateway.handler.codec.XhakaEncoder;
import com.tinysakura.xhaka.common.protocal.Xhaka;
import com.tinysakura.xhaka.common.protocal.constant.XhakaHeaderConstant;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/20
 */
@Slf4j
public class XhakaProtocolHandler extends SimpleChannelInboundHandler<Xhaka> {
    ScheduledExecutorService scheduleExecutor;
    // 心跳包发送周期，暂定一分钟
    private static final Integer HEART_PACK_SEND_PERIOD = 1000 * 60;

    private boolean sendHeartPackPeriod;

    private String serverName;

    public XhakaProtocolHandler() {
        this.sendHeartPackPeriod = false;
    }

    public XhakaProtocolHandler(Boolean sendHeartPackPeriod) {
        this.sendHeartPackPeriod = sendHeartPackPeriod;
    }

    public XhakaProtocolHandler(Boolean sendHeartPackPeriod, String serverName) {
        this.sendHeartPackPeriod = sendHeartPackPeriod;
        this.serverName = serverName;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Xhaka xhaka) throws Exception {
        log.info("XhakaProtocolHandler, xhaka:{}", xhaka);

        // 心跳响应不做后续处理
        if (XhakaHeaderConstant.XHAKA_PACK_TYPE_HEART == xhaka.getPackType() && XhakaHeaderConstant.XHAKA_EVENT_TYPE_RESPONSE == xhaka.getEventType()) {
            log.info("receive gateway heart echo:{}", ctx.channel());
            return;
        }

        // 心跳请求发送心跳响应
        if (XhakaHeaderConstant.XHAKA_PACK_TYPE_HEART == xhaka.getPackType() && XhakaHeaderConstant.XHAKA_EVENT_TYPE_REQUEST == xhaka.getEventType()) {
            // 收到心跳请求，在心跳起搏器中增加当前连接活跃度
            String serverName = new String(xhaka.getBody(), Charset.forName("UTF-8"));
            InetSocketAddress socketAddress = (InetSocketAddress) ctx.channel().remoteAddress();

            HeartbeatPacemaker.getInstance().pacemaker(serverName, socketAddress.getAddress().getHostAddress() + ":" + socketAddress.getPort(), ctx.channel());

            Xhaka xhakaHeartRes = new Xhaka();
            xhakaHeartRes.setPackType(XhakaHeaderConstant.XHAKA_PACK_TYPE_HEART);
            xhakaHeartRes.setEventType(XhakaHeaderConstant.XHAKA_EVENT_TYPE_RESPONSE);
            ctx.pipeline().get(XhakaEncoder.class).write(ctx, xhakaHeartRes, ctx.voidPromise());
            ctx.flush();
            return;
        }

        // 正常请求就透传给后续handler处理
        if (XhakaHeaderConstant.XHAKA_STATUS_OK == xhaka.getXhakaStatus()) {
            ctx.fireChannelRead(xhaka);
            return;
        }

        // 其它状态的请求记录日志
        // @todo 根据状态处理
        log.error("receive abnormal xhaka pack:{}", xhaka);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (sendHeartPackPeriod) {
            scheduleExecutor = Executors.newScheduledThreadPool(1);

            Xhaka xhakaHeartReq = new Xhaka();
            xhakaHeartReq.setPackType(XhakaHeaderConstant.XHAKA_PACK_TYPE_HEART);
            xhakaHeartReq.setEventType(XhakaHeaderConstant.XHAKA_EVENT_TYPE_REQUEST);
            xhakaHeartReq.setBody(serverName.getBytes(Charset.forName("UTF-8")));
            xhakaHeartReq.setBodyLength(xhakaHeartReq.getBody().length);

            scheduleExecutor.scheduleAtFixedRate(() -> {
                log.info("slave begin send heart pack,period:{}", HEART_PACK_SEND_PERIOD);
                ctx.pipeline().writeAndFlush(xhakaHeartReq);
            }, 0L, HEART_PACK_SEND_PERIOD, TimeUnit.MILLISECONDS);
        }
    }
}