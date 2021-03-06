package com.tinysakura.xhaka.common.gateway.serialize.json.serialize;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.tinysakura.xhaka.common.gateway.serialize.XhakaBodySerialize;
import com.tinysakura.xhaka.common.protocal.Xhaka;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.http.*;
import org.springframework.util.CollectionUtils;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/26
 */

public class XhakaBodyFullHttpResponseJsonTypeSerialize implements XhakaBodySerialize<FullHttpResponse> {

    @Override
    public byte[] serialize(FullHttpResponse fullHttpResponse) {
        JSONObject jsonObject = new JSONObject();
        Map<String, String> responseHeaders = new HashMap<>();
        for (Map.Entry<String, String> headerEntry : fullHttpResponse.headers()) {
            responseHeaders.put(headerEntry.getKey(), headerEntry.getValue());
        }

        jsonObject.put("status", fullHttpResponse.status().code());
        jsonObject.put("headers", responseHeaders);

        fullHttpResponse.content().resetReaderIndex();
        byte[] body = new byte[fullHttpResponse.content().readableBytes()];
        fullHttpResponse.content().readBytes(body);

        jsonObject.put("body", body);
        return JSON.toJSONBytes(jsonObject);

    }

    @Override
    public FullHttpResponse deserialize(Xhaka xhaka) {
        byte[] serializeBody = xhaka.getBody();
        JSONObject jsonObject = JSON.parseObject(new String(serializeBody, Charset.forName("UTF-8")));

        Integer statusCode = jsonObject.getInteger("status");
        Map<String, String> headers = jsonObject.getObject("headers", Map.class);


        ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(xhaka.getBodyLength());
        byteBuf.retain();
        byteBuf.writeBytes(jsonObject.getBytes("body"));

        DefaultFullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.valueOf(statusCode), byteBuf);
        if (CollectionUtils.isEmpty(headers)) {
            return fullHttpResponse;
        }
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            HttpHeaders httpHeaders = fullHttpResponse.headers();
            httpHeaders.add(entry.getKey(), entry.getValue());
        }

        return fullHttpResponse;
    }
}