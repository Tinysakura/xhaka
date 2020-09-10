package com.tinysakura.xhaka.common.gateway.serialize;

import com.tinysakura.xhaka.common.gateway.serialize.impl.XhakaBodyFullHttpRequestJsonTypeSerialize;
import com.tinysakura.xhaka.common.gateway.serialize.impl.XhakaBodyFullHttpResponseJsonTypeSerialize;
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

        xhakaResponseSerializerMap = new HashMap<>();
        xhakaResponseSerializerMap.put(XhakaHeaderConstant.XHAKA_SERIALIZATION_JSON, new XhakaBodyFullHttpResponseJsonTypeSerialize());
    }

    public static XhakaBodySerialize findRequestSerializer(byte type) {
        return xhakaRequestSerializerMap.get(type);
    }

    public static XhakaBodySerialize findResponseSerializer(byte type) {
        return xhakaResponseSerializerMap.get(type);
    }
}