package com.tinysakura.xhaka.common.gateway.handler.codec;

import com.tinysakura.xhaka.common.gateway.config.XhakaGateWayConfig;
import com.tinysakura.xhaka.common.gateway.constant.XhakaHttpHeaderConstant;
import com.tinysakura.xhaka.common.gateway.serialize.XhakaBodySerializeFacade;
import com.tinysakura.xhaka.common.protocal.Xhaka;
import com.tinysakura.xhaka.common.protocal.constant.XhakaHeaderConstant;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.FullHttpResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 被代理服务的response会被包装成xhaka在代理服务与网关间传递
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/26
 */
@Slf4j
public class FullHttpResponse2XhakaEncoder extends MessageToMessageEncoder<FullHttpResponse> {

    @Override
    protected void encode(ChannelHandlerContext ctx, FullHttpResponse fullHttpResponse, List<Object> list) throws Exception {
        log.info("FullHttpResponse2XhakaEncoder, fullHttpResponse:{}", fullHttpResponse);

        Xhaka xhaka = new Xhaka();
        xhaka.setEventType(XhakaHeaderConstant.XHAKA_EVENT_TYPE_RESPONSE);
        xhaka.setXhakaId(Long.valueOf(fullHttpResponse.headers().get(XhakaHttpHeaderConstant.HTTP_HEADER_XHAKA_ID)));
        xhaka.setPackType(XhakaHeaderConstant.XHAKA_PACK_TYPE_NORMAL);
        xhaka.setXhakaStatus(XhakaHeaderConstant.XHAKA_STATUS_OK);
        xhaka.setSerialization(XhakaHeaderConstant.getXhakaSerializationTypeByDesc(XhakaGateWayConfig.getInstance().getSerialization()));

        byte[] body = XhakaBodySerializeFacade.findResponseSerializer(xhaka.getSerialization()).serialize(fullHttpResponse);
        fullHttpResponse.content().resetReaderIndex();
        xhaka.setBodyLength(fullHttpResponse.content().readableBytes());
        xhaka.setBody(body);

        ctx.channel().writeAndFlush(xhaka);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error("FullHttpResponse2XhakaEncoder occur error", cause);
    }
}