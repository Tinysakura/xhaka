package com.tinysakura.xhaka.common.gateway.context.client;

import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/9/16
 */
@Slf4j
public class HeartbeatPacemaker {

    private static final ScheduledExecutorService exec = Executors.newScheduledThreadPool(1);

    public static final Integer PACEMAKER_PERIOD = 60 * 2;

    private Map<String, Map<String, AtomicInteger>> slaveChannelCounter;

    private AtomicReference<Thread> atomicReference;

    private static HeartbeatPacemaker instance;

    public static HeartbeatPacemaker getInstance() {
        if (instance == null) {
            synchronized (HeartbeatPacemaker.class) {
                if (instance == null) {
                    instance = new HeartbeatPacemaker();
                }
            }
        }

        return instance;
    }

    private HeartbeatPacemaker() {
        this.slaveChannelCounter = new HashMap<>();
        this.atomicReference = new AtomicReference<>(null);
        start();
    }

    public void pacemaker(String serverName, String ip, Channel channel) {
        // 自旋
        while (atomicReference.compareAndSet(null, Thread.currentThread())) {
            log.debug("spin in thread:{}", Thread.currentThread());
        }

        if (!slaveChannelCounter.containsKey(serverName)) {
            slaveChannelCounter.put(serverName, new HashMap<>());
        }

        if (!slaveChannelCounter.get(serverName).containsKey(ip) && channel != null) {
            String[] split = ip.split(":");
            GatewaySlaveChannelPool.getInstance().addSlaveChannelIntoPool(serverName, split[0], Integer.valueOf(split[1]), channel);
        }
        slaveChannelCounter.get(serverName).putIfAbsent(ip, new AtomicInteger(1));
        slaveChannelCounter.get(serverName).get(ip).incrementAndGet();

        atomicReference.set(null);
    }

    private void start() {
        exec.scheduleAtFixedRate(new Task(), 0, PACEMAKER_PERIOD, TimeUnit.SECONDS);
    }

    private class Task implements Runnable {
        @Override
        public void run() {
            // 自旋
            while (!atomicReference.compareAndSet(null, Thread.currentThread())) {
                log.debug("spin in thread:{}", Thread.currentThread());
            }

            if (!CollectionUtils.isEmpty(slaveChannelCounter)) {
                for (Map.Entry<String, Map<String, AtomicInteger>> entry : slaveChannelCounter.entrySet()) {
                    String serverName = entry.getKey();

                    for (Map.Entry<String, AtomicInteger> childEntry : entry.getValue().entrySet()) {
                        int i = childEntry.getValue().decrementAndGet();
                        if (i < 0) {
                            // 如果对应channel的心跳计数低于0则将其从服务可选择的channelPool中移除
                            GatewaySlaveChannelPool.getInstance().removeSlaveChannelFromPool(serverName, childEntry.getKey());
                            log.debug("remove server:{} instance:{}", serverName, childEntry.getKey());
                        }
                    }
                }
            }

            atomicReference.set(null);
        }
    }
}