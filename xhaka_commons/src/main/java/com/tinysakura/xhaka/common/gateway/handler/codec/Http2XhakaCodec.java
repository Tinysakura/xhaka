package com.tinysakura.xhaka.common.gateway.handler.codec;

import com.tinysakura.xhaka.common.protocal.Xhaka;
import com.tinysakura.xhaka.common.servlet.request.XhakaHttpServletRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageCodec;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * 将入站的http请求解码为Xhaka协议在框架内部处理，出站时将xhaka协议转换成为FullHttpResponse交给HttpResponseEncoder
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/20
 */
@Slf4j
public class Http2XhakaCodec extends MessageToMessageCodec<XhakaHttpServletRequest, Xhaka> {

    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Xhaka xhaka, List<Object> list) throws Exception {

    }

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, XhakaHttpServletRequest xhakaHttpServletRequest, List<Object> list) throws Exception {
        Xhaka xhaka = new Xhaka();
    }
}