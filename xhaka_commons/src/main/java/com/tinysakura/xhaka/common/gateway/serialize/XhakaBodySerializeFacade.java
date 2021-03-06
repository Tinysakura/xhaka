package com.tinysakura.xhaka.common.gateway.serialize;

import com.tinysakura.xhaka.common.gateway.serialize.json.serialize.XhakaBodyFullHttpRequestJsonTypeSerialize;
import com.tinysakura.xhaka.common.gateway.serialize.json.serialize.XhakaBodyFullHttpResponseJsonTypeSerialize;
import com.tinysakura.xhaka.common.gateway.serialize.protobuf.serialize.XhakaBodyFullHttpRequestProtobufTypeSerialize;
import com.tinysakura.xhaka.common.gateway.serialize.protobuf.serialize.XhakaBodyFullHttpResponseProtobufTypeSerialize;
import com.tinysakura.xhaka.common.protocal.constant.XhakaHeaderConstant;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/26
 */

public class XhakaBodySerializeFacade {

    private static Map<Byte, XhakaBodySerialize> xhakaRequestSerializerMap;
    private static Map<Byte, XhakaBodySerialize> xhakaResponseSerializerMap;

    static {
        xhakaRequestSerializerMap = new HashMap<>();
        xhakaRequestSerializerMap.put(XhakaHeaderConstant.XHAKA_SERIALIZATION_JSON, new XhakaBodyFullHttpRequestJsonTypeSerialize());
        xhakaRequestSerializerMap.put(XhakaHeaderConstant.XHAKA_SERIALIZATION_PROTOBUF, new XhakaBodyFullHttpRequestProtobufTypeSerialize());

        xhakaResponseSerializerMap = new HashMap<>();
        xhakaResponseSerializerMap.put(XhakaHeaderConstant.XHAKA_SERIALIZATION_JSON, new XhakaBodyFullHttpResponseJsonTypeSerialize());
        xhakaResponseSerializerMap.put(XhakaHeaderConstant.XHAKA_SERIALIZATION_PROTOBUF, new XhakaBodyFullHttpResponseProtobufTypeSerialize());

    }

    public static XhakaBodySerialize findRequestSerializer(byte type) {
        return xhakaRequestSerializerMap.get(type);
    }

    public static XhakaBodySerialize findResponseSerializer(byte type) {
        return xhakaResponseSerializerMap.get(type);
    }
}