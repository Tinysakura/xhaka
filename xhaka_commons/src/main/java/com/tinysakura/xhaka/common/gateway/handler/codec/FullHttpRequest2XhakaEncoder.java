package com.tinysakura.xhaka.common.gateway.handler.codec;

import com.tinysakura.xhaka.common.gateway.config.XhakaGateWayConfig;
import com.tinysakura.xhaka.common.gateway.constant.XhakaHttpHeaderConstant;
import com.tinysakura.xhaka.common.gateway.remote.util.GlobalCounterUtil;
import com.tinysakura.xhaka.common.gateway.serialize.XhakaBodySerializeFacade;
import com.tinysakura.xhaka.common.protocal.Xhaka;
import com.tinysakura.xhaka.common.protocal.constant.XhakaHeaderConstant;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.FullHttpRequest;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 将发送到网关的请求转换成xhaka协议发送到代理服务
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */
@Slf4j
public class FullHttpRequest2XhakaEncoder extends MessageToMessageEncoder<FullHttpRequest> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, FullHttpRequest fullHttpRequest, List<Object> list) {
        log.info("FullHttpRequest2XhakaEncoder fullHttpRequest:{}", fullHttpRequest);

        Xhaka xhaka = new Xhaka();
        xhaka.setEventType(XhakaHeaderConstant.XHAKA_EVENT_TYPE_REQUEST);
        xhaka.setXhakaId(Long.valueOf(fullHttpRequest.headers().get(XhakaHttpHeaderConstant.HTTP_HEADER_XHAKA_ID)));
        xhaka.setPackType(XhakaHeaderConstant.XHAKA_PACK_TYPE_NORMAL);
        xhaka.setXhakaStatus(XhakaHeaderConstant.XHAKA_STATUS_OK);
        xhaka.setSerialization(XhakaHeaderConstant.getXhakaSerializationTypeByDesc(XhakaGateWayConfig.getInstance().getSerialization()));

        byte[] body = XhakaBodySerializeFacade.findRequestSerializer(xhaka.getSerialization()).serialize(fullHttpRequest);
        fullHttpRequest.content().resetReaderIndex();
        xhaka.setBodyLength(fullHttpRequest.content().readableBytes());
        xhaka.setBody(body);

        list.add(xhaka);
    }
}