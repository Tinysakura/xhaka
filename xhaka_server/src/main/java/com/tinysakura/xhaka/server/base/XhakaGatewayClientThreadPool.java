package com.tinysakura.xhaka.server.base;

import com.tinysakura.xhaka.common.gateway.config.XhakaGateWayConfig;
import com.tinysakura.xhaka.server.bootstrap.XhakaGatewayClient;

import java.util.HashSet;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */

public class XhakaGatewayClientThreadPool {

    private static final ExecutorService executorService;

    private static final HashSet<String> ipSet;

    private static final String THREAD_POOL_NAME = "xhaka_gateway_client";

    private static final AtomicLong threadNum = new AtomicLong();

    static {
        ipSet = new HashSet<>();

        Integer coreThreadSize = XhakaGateWayConfig.getInstance().getGatewayClientConnCoreThreadSize();
        Integer maxThreadSize = XhakaGateWayConfig.getInstance().getGatewayClientConnMaxThreadSize();

        executorService = new ThreadPoolExecutor(coreThreadSize, maxThreadSize,
                0L, TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>(), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                return new Thread(r, THREAD_POOL_NAME + "_" + threadNum.incrementAndGet());
            }
        });
    }

    /**
     * 防止监听的zk节点发生变更重复与代理服务创建连接
     * 需要是线程安全的，并发度不高synchronized性能足以胜任
     * @param runnable
     */
    public static synchronized void submit(Runnable runnable) {
        assert runnable instanceof XhakaGatewayClientRunnable;
        String name = ((XhakaGatewayClientRunnable) runnable).getName();
        if (ipSet.contains(name)) {
            return;
        }

        executorService.submit(runnable);
    }

    public static class XhakaGatewayClientRunnable implements Runnable {

        private String serverName;
        private String host;
        private Integer port;

        public XhakaGatewayClientRunnable(String serverName, String host, Integer port) {
            this.serverName = serverName;
            this.host = host;
            this.port = port;
        }

        public String getHost() {
            return host;
        }

        public Integer getPort() {
            return port;
        }

        String getName() {
            return "XhakaGatewayClient_" + serverName + "_" + host + ":" + port;
        }

        @Override
        public void run() {
            new XhakaGatewayClient(serverName, host, port).connect();
        }
    }
}