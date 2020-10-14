package com.tinysakura.xhaka.common.gateway.context.client;

import com.tinysakura.xhaka.common.gateway.remote.nlb.LoadBalanceStrategy;
import com.tinysakura.xhaka.common.gateway.remote.nlb.constant.LoadBalanceStrategyConstant;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * 网关维护的和被代理服务的连接池
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/28
 */
@Component
@Slf4j
public class GatewaySlaveChannelPool implements ApplicationContextAware, InitializingBean {
    private static GatewaySlaveChannelPool instance;

    /**
     * 绝大多数情况下是读场景使用读写锁提高并发性能
     */
    private ReentrantReadWriteLock reentrantReadWriteLock;

    private Map<String, Map<String, Channel>> slaveChannelPoolMap;

    private ApplicationContext applicationContext;

    private Map<String, LoadBalanceStrategy> loadBalanceStrategyMap;

    private GatewaySlaveChannelPool() {
        this.slaveChannelPoolMap = new HashMap<>();
        reentrantReadWriteLock = new ReentrantReadWriteLock();
        instance = this;
    }

    public static GatewaySlaveChannelPool getInstance() {
        return instance;
    }

    public void addSlaveChannelIntoPool(String serverName, String remoteHost, Integer remotePort, Channel channel) {
        reentrantReadWriteLock.writeLock().lock();

        try {
            Map<String, Channel> channelMap = this.slaveChannelPoolMap.get(serverName);
            if (channelMap == null) {
                channelMap = new HashMap<>();
            }

            String channelTag = remoteHost + ":" + remotePort;
            channelMap.put(channelTag, channel);

            this.slaveChannelPoolMap.put(serverName, channelMap);
        } catch (Exception e) {
            log.error("add slave channel into pool occur exception", e);
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    public void removeSlaveChannelFromPool(String serverName, String remoteHost, Integer remotePort) {
        log.debug("removeSlaveChannelFromPool serverName:{}, ip:{}", serverName, remoteHost + remotePort);
        removeSlaveChannelFromPool(serverName, remoteHost + ":" + remotePort);
    }

    void removeSlaveChannelFromPool(String serverName, String channelTag) {
        reentrantReadWriteLock.writeLock().lock();

        try {
            Map<String, Channel> channelMap = this.slaveChannelPoolMap.get(serverName);
            if (channelMap == null) {
                log.warn("removeSlaveChannelFromPool may invalid serverName:{}", serverName);
                return;
            }

            channelMap.remove(channelTag);
            XhakaGatewayClientThreadPool.removeIpSet("XhakaGatewayClient_" + serverName + "_" + channelTag);
        } catch (Exception e) {
            log.error("remove slave channel from pool occur exception", e);
        } finally {
            reentrantReadWriteLock.writeLock().unlock();
        }
    }

    public Channel getSlaveChannelByLoadBalanceStrategy(String strategyName, String serverName) {
        LoadBalanceStrategy loadBalanceStrategy = loadBalanceStrategyMap.get(strategyName);
        if (loadBalanceStrategy == null) {
            loadBalanceStrategy = loadBalanceStrategyMap.get(LoadBalanceStrategyConstant.LOAD_BALANCE_STRATEGY_RANDOM);
        }

        if (loadBalanceStrategy == null) {
            return null;
        }

        this.reentrantReadWriteLock.readLock().lock();

        try {
            return loadBalanceStrategy.selectChannel(this.slaveChannelPoolMap.get(serverName));
        } catch (Exception e) {
            log.error("get slave channel from pool occur exception", e);
        } finally {
            this.reentrantReadWriteLock.readLock().unlock();
        }

        return null;
    }

    public Map<String, LoadBalanceStrategy> getLoadBalanceStrategyMap() {
        return loadBalanceStrategyMap;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        loadBalanceStrategyMap = new HashMap<>();
        Map<String, LoadBalanceStrategy> loadBalanceStrategyBeanMap = this.applicationContext.getBeansOfType(LoadBalanceStrategy.class);
        for (LoadBalanceStrategy loadBalanceStrategy : loadBalanceStrategyBeanMap.values()) {
            loadBalanceStrategyMap.put(loadBalanceStrategy.getType(), loadBalanceStrategy);
        }
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }
}