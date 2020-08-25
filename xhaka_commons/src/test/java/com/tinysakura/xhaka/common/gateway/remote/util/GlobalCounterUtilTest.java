package com.tinysakura.xhaka.common.gateway.remote.util;

import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/25
 */

public class GlobalCounterUtilTest {

    @Test
    public void testGetAndUpdate() {
        AtomicInteger i = new AtomicInteger();

        for (int k = 0; k < 5; k++) {
            System.out.println(i.getAndUpdate((f) -> f >= Integer.MAX_VALUE ? 0 : f + 1));
        }
    }
}