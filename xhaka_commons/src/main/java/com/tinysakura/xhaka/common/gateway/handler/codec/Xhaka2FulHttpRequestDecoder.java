package com.tinysakura.xhaka.common.gateway.handler.codec;

import com.tinysakura.xhaka.common.gateway.serialize.XhakaBodySerializeFacade;
import com.tinysakura.xhaka.common.protocal.Xhaka;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/26
 */

public class Xhaka2FulHttpRequestDecoder extends MessageToMessageDecoder<Xhaka> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, Xhaka xhaka, List<Object> list) throws Exception {
        list.add(XhakaBodySerializeFacade.findRequestSerializer(xhaka.getSerialization()).deserialize(xhaka));
    }
}