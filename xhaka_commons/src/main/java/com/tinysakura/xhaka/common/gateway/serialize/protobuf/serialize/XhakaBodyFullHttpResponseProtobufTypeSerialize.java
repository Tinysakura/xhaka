package com.tinysakura.xhaka.common.gateway.serialize.protobuf.serialize;

import com.google.protobuf.ByteString;
import com.google.protobuf.InvalidProtocolBufferException;
import com.tinysakura.xhaka.common.gateway.serialize.XhakaBodySerialize;
import com.tinysakura.xhaka.common.gateway.serialize.protobuf.HeaderOuterClass;
import com.tinysakura.xhaka.common.gateway.serialize.protobuf.HttpResponseOuterClass;
import com.tinysakura.xhaka.common.protocal.Xhaka;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpResponseStatus;
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
public class XhakaBodyFullHttpResponseProtobufTypeSerialize implements XhakaBodySerialize<FullHttpResponse> {

    @Override
    public byte[] serialize(FullHttpResponse originalBody) {
        HttpResponseOuterClass.HttpResponse.Builder builder = HttpResponseOuterClass.HttpResponse.newBuilder();

        builder.setStatus(originalBody.status().code());

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
    public FullHttpResponse deserialize(Xhaka xhaka) {
        try {
            byte[] serializedBody = xhaka.getBody();
            HttpResponseOuterClass.HttpResponse httpResponse = HttpResponseOuterClass.HttpResponse.parseFrom(serializedBody);

            ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.directBuffer(xhaka.getBodyLength());
            byteBuf.retain();
            byteBuf.writeBytes(httpResponse.getBody().toByteArray());

            DefaultFullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.valueOf(httpResponse.getStatus()), byteBuf);

            if (CollectionUtils.isEmpty(httpResponse.getHeadersList())) {
                for (HeaderOuterClass.Header header : httpResponse.getHeadersList()) {
                    fullHttpResponse.headers().add(header.getName(), header.getValue());
                }
            }

            return fullHttpResponse;
        } catch (InvalidProtocolBufferException e) {
            log.error("response deserialize failed", e);
            return null;
        }
    }
}