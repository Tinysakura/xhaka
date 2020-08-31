package com.tinysakura.xhaka.common.gateway.handler;

import com.tinysakura.xhaka.common.protocal.Xhaka;
import com.tinysakura.xhaka.common.protocal.constant.XhakaHeaderConstant;
import com.tinysakura.xhaka.common.util.XhakaUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/20
 */
@Slf4j
public class XhakaProtocolHandler extends SimpleChannelInboundHandler<Xhaka> {

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Xhaka xhaka) throws Exception {
        // 心跳包不做后续处理直接返回心跳响应
        if (XhakaHeaderConstant.XHAKA_PACK_TYPE_HEART == xhaka.getPackType()) {
            ctx.writeAndFlush(XhakaUtils.builtResponseXhakaHeart());
        }

        // 如果是网关发起的正常请求就透传给后续handler处理
        if (XhakaHeaderConstant.XHAKA_STATUS_OK == xhaka.getXhakaStatus()) {
            ctx.fireChannelRead(xhaka);
        }

        // 其它状态的请求记录日志
        // @todo 根据状态处理
        log.error("receive abnormal xhaka pack:{}", xhaka);
    }
}