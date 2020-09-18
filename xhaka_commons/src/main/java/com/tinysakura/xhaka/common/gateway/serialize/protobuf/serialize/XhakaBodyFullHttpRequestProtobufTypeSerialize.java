package com.tinysakura.xhaka.common.gateway.serialize.protobuf.serialize;

import com.google.protobuf.ByteString;
import com.tinysakura.xhaka.common.gateway.constant.XhakaHttpHeaderConstant;
import com.tinysakura.xhaka.common.gateway.serialize.XhakaBodySerialize;
import com.tinysakura.xhaka.common.gateway.serialize.protobuf.HeaderOuterClass;
import com.tinysakura.xhaka.common.gateway.serialize.protobuf.HttpRequestOuterClass;
import com.tinysakura.xhaka.common.protocal.Xhaka;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/9/18
 */
@Slf4j
public class XhakaBodyFullHttpRequestProtobufTypeSerialize implements XhakaBodySerialize<FullHttpRequest> {

    @Override
    public byte[] serialize(FullHttpRequest originalBody) {
        HttpRequestOuterClass.HttpRequest.Builder builder = HttpRequestOuterClass.HttpRequest.newBuilder();

        builder.setMethod(originalBody.method().name());
        builder.setUri(originalBody.uri());

        // builder headers
        List<HeaderOuterClass.Header> headers = new ArrayList<>();
        for (String headerName : originalBody.headers().names()) {
            HeaderOuterClass.Header.Builder headerBuilder = HeaderOuterClass.Header.newBuilder();
            headerBuilder.setName(headerName);
            headerBuilder.setValue(originalBody.headers().get(headerName));

            headers.add(headerBuilder.build());
        }

        builder.addAllHeaders(headers);
        byte[] body = new byte[originalBody.content().readableBytes()];
        originalBody.content().readBytes(body);
        builder.setBody(ByteString.copyFrom(body));

        return builder.build().toByteArray();
    }

    @Override
    public FullHttpRequest deserialize(Xhaka xhaka) {
        byte[] serializedBody = xhaka.getBody();

        try {
            HttpRequestOuterClass.HttpRequest httpRequest = HttpRequestOuterClass.HttpRequest.parseFrom(serializedBody);

            ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(xhaka.getBodyLength());
            byteBuf.writeBytes(httpRequest.getBody().toByteArray());

            DefaultFullHttpRequest fullHttpRequest = new DefaultFullHttpRequest(HttpVersion.HTTP_1_0, HttpMethod.valueOf(httpRequest.getMethod()), httpRequest.getUri(), byteBuf);

            if (CollectionUtils.isEmpty(httpRequest.getHeadersList())) {
                for (HeaderOuterClass.Header header : httpRequest.getHeadersList()) {
                    fullHttpRequest.headers().add(header.getName(), header.getValue());
                }
            }

            fullHttpRequest.headers().add(XhakaHttpHeaderConstant.HTTP_HEADER_XHAKA_ID, xhaka.getXhakaId());
            return fullHttpRequest;
        } catch (Exception e) {
            log.error("request deserialize failed", e);
            return null;
        }
    }
}