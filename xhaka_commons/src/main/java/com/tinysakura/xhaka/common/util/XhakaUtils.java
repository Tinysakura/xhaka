package com.tinysakura.xhaka.common.util;

import com.tinysakura.xhaka.common.protocal.Xhaka;
import com.tinysakura.xhaka.common.protocal.constant.XhakaHeaderConstant;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/28
 */

public class XhakaUtils {

    /**
     * 构建一个xhaka心跳包
     * @return
     */
    public static Xhaka builtRequestXhakaHeart() {
        Xhaka xhaka = new Xhaka();
        xhaka.setEventType(XhakaHeaderConstant.XHAKA_EVENT_TYPE_REQUEST);
        xhaka.setPackType(XhakaHeaderConstant.XHAKA_PACK_TYPE_HEART);
        xhaka.setXhakaStatus(XhakaHeaderConstant.XHAKA_STATUS_OK);

        return xhaka;
    }

    public static Xhaka builtResponseXhakaHeart() {
        Xhaka xhaka = new Xhaka();
        xhaka.setEventType(XhakaHeaderConstant.XHAKA_EVENT_TYPE_RESPONSE);
        xhaka.setPackType(XhakaHeaderConstant.XHAKA_PACK_TYPE_HEART);
        xhaka.setXhakaStatus(XhakaHeaderConstant.XHAKA_STATUS_OK);

        return xhaka;
    }
}