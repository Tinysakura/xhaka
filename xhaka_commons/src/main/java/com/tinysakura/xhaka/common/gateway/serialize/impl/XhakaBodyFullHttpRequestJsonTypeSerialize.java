package com.tinysakura.xhaka.common.gateway.serialize.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tinysakura.xhaka.common.gateway.constant.XhakaHttpHeaderConstant;
import com.tinysakura.xhaka.common.gateway.serialize.XhakaBodySerialize;
import com.tinysakura.xhaka.common.protocal.Xhaka;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.http.*;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/26
 */

public class XhakaBodyFullHttpRequestJsonTypeSerialize implements XhakaBodySerialize<FullHttpRequest> {

    @Override
    public byte[] serialize(FullHttpRequest originalBody) {
        JSONObject jsonObject = new JSONObject();
        Map<String, String> headers = new HashMap<>();
        for (Map.Entry<String, String> headerEntry : originalBody.headers()) {
            headers.put(headerEntry.getKey(), headerEntry.getValue());
        }

        jsonObject.put("uri", originalBody.uri());
        jsonObject.put("method", originalBody.method().name());
        jsonObject.put("headers", headers);

        originalBody.content().resetReaderIndex();
        byte[] body = new byte[originalBody.content().readableBytes()];
        originalBody.content().readBytes(body);

        jsonObject.put("body", body);
        return JSON.toJSONBytes(jsonObject);
    }

    @Override
    public FullHttpRequest deserialize(Xhaka xhaka) {
        byte[] serializeBody = xhaka.getBody();
        JSONObject jsonObject = JSON.parseObject(new String(serializeBody, Charset.forName("UTF-8")));

        String uri = jsonObject.getString("uri");
        String methodName = jsonObject.getString("method");
        Map<String, String> headers = jsonObject.getObject("headers", Map.class);

        HttpHeaders httpHeaders = new DefaultHttpHeaders();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            httpHeaders.add(entry.getKey(), entry.getValue());
        }
        httpHeaders.add(XhakaHttpHeaderConstant.HTTP_HEADER_XHAKA_ID, xhaka.getXhakaId());

        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(xhaka.getBodyLength());
        byteBuf.writeBytes(jsonObject.getBytes("body"));

        return new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.valueOf(methodName), uri, byteBuf,null, httpHeaders);
    }
}