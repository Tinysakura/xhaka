package com.tinysakura.xhaka.common.gateway.discovery.nlb.impl;

import com.tinysakura.xhaka.common.gateway.discovery.nlb.LoadBalanceStrategy;
import com.tinysakura.xhaka.common.gateway.discovery.nlb.constant.LoadBalanceStrategyConstant;
import io.netty.channel.Channel;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */

public class LoadBalanceStrategyRandom implements LoadBalanceStrategy {
    @Override
    public String getType() {
        return LoadBalanceStrategyConstant.LOAD_BALANCE_STRATEGY_RANDOM;
    }

    @Override
    public Channel selectChannel(String serverName) {
        return null;
    }
}