package com.tinysakura.xhaka.common.gateway.remote.util;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 线程安全的全局计数器工具类
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/25
 */

public class GlobalCounterUtil {
    private static final AtomicInteger XHAKA_SEQUENCE_GLOBAL_COUNTER = new AtomicInteger();

    /**
     * 线程安全的生成xhaka协议请求序号
     * @return
     */
    public int getXhakaRequestSequence() {
        // 使用getAndUpdate防止越界
        return XHAKA_SEQUENCE_GLOBAL_COUNTER.getAndUpdate((i) -> i >= Integer.MAX_VALUE ? 0 : i + 1);
    }
}