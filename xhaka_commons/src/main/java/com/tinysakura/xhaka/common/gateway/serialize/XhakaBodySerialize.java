package com.tinysakura.xhaka.common.gateway.serialize;

import com.tinysakura.xhaka.common.protocal.Xhaka;

/**
 * xhaka协议body序列化接口
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/26
 */
public interface XhakaBodySerialize<T> {

    /**
     * 序列化方法
     * @param originalBody
     * @return
     */
     byte[] serialize(T originalBody);

    /**
     * 反序列化方法
     * @param
     * @return
     */
     T deserialize(Xhaka xhaka);
}
