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
    public static final byte XHAKA_SERIALIZATION_JSON = 1;
    public static final byte XHAKA_SERIALIZATION_PROROBUF = 2;

    public static final byte XHAKA_STATUS_OK = 0;
    public static final byte XHAKA_STATUS_TIMEOUT = 1;
    public static final byte XHAKA_STATUS_BAD_HEADER = 2;
    public static final byte XHAKA_STATUS_BAD_REQUEST_BODY = 3;
}