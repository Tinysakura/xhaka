package com.tinysakura.xhaka.common.protocal.constant;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/20
 */

public class XhakaHeaderConstant {

    public static final byte XHAKA_EVENT_TYPE_REQUEST = 1;
    public static final byte XHAKA_EVENT_TYPE_RESPONSE = 0;

    public static final byte XHAKA_PACK_TYPE_NORMAL = 1;
    public static final byte XHAKA_PACK_TYPE_HEART = 0;

    public static final byte XHAKA_SERIALIZATION_JAVA = 0;
    public static final String XHAKA_SERIALIZATION_JAVA_DESC = "java";
    public static final byte XHAKA_SERIALIZATION_JSON = 1;
    public static final String XHAKA_SERIALIZATION_JSON_DESC = "json";
    public static final byte XHAKA_SERIALIZATION_PROTOBUF = 2;
    public static final String XHAKA_SERIALIZATION_PROTOBUF_DESC = "protobuf";

    public static final byte XHAKA_STATUS_OK = 0;
    public static final byte XHAKA_STATUS_TIMEOUT = 1;
    public static final byte XHAKA_STATUS_BAD_HEADER = 2;
    public static final byte XHAKA_STATUS_BAD_REQUEST_BODY = 3;
    public static final byte XHAKA_STATUS_FAIELD = 4;

    public static byte getXhakaSerializationTypeByDesc(String desc) {
        if (desc.equalsIgnoreCase(XHAKA_SERIALIZATION_JAVA_DESC)) {
           return XHAKA_SERIALIZATION_JAVA;
        }

        if (desc.equalsIgnoreCase(XHAKA_SERIALIZATION_JSON_DESC)) {
            return XHAKA_SERIALIZATION_JSON;
        }

        if (desc.equalsIgnoreCase(XHAKA_SERIALIZATION_PROTOBUF_DESC)) {
            return XHAKA_SERIALIZATION_PROTOBUF;
        }

        // 默认使用原生序列化工具
        return 0;
    }
}