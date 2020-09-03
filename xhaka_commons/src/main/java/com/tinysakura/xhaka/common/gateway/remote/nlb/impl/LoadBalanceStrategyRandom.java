package com.tinysakura.xhaka.common.gateway.remote.nlb.impl;

import com.tinysakura.xhaka.common.gateway.remote.nlb.LoadBalanceStrategy;
import com.tinysakura.xhaka.common.gateway.remote.nlb.constant.LoadBalanceStrategyConstant;
import io.netty.channel.Channel;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Map;
import java.util.Random;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */

public class LoadBalanceStrategyRandom implements LoadBalanceStrategy {

    private final Random random = new Random();

    @Override
    public String getType() {
        return LoadBalanceStrategyConstant.LOAD_BALANCE_STRATEGY_RANDOM;
    }

    @Override
    public Channel selectChannel(Map<String, Channel> channelMap) {
        if (CollectionUtils.isEmpty(channelMap)) {
            return null;
        }

        ArrayList<Channel> channelList = new ArrayList<>(channelMap.values());

        int i = random.nextInt(channelList.size());
        return channelList.get(i);
    }


}