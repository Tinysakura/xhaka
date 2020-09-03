package com.tinysakura.xhaka.common.gateway.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/25
 */
@ConfigurationProperties(prefix = "xhaka.gateway")
@Component
public class XhakaGateWayConfig {

    /**
     * 网关代理服务注册地址(默认使用zk)
     */
    private String discoveryAddress;

    /**
     * xhaka协议体序列化方式
     */
    private String serialization;

    /**
     * 被网关代理的服务在本地启动的nettyServer使用的端口号
     */
    private Integer slaveNettyServerPort;

    /**
     * 被代理服务的处理业务请求的线程数量
     */
    private Integer slaveBusinessThreadCount;

    private Integer slaveResponseTimeout;

    private Integer xhakaMsgMaxLength;

    private String loadBalance;

    private Integer gatewayClientConnCoreThreadSize;

    private Integer gatewayClientConnMaxThreadSize;

    public static XhakaGateWayConfig instance;

    public XhakaGateWayConfig() {
        instance = this;
    }

    public static XhakaGateWayConfig getInstance() {
        return instance;
    }

    public String getDiscoveryAddress() {
        return discoveryAddress;
    }

    public void setDiscoveryAddress(String discoveryAddress) {
        this.discoveryAddress = discoveryAddress;
    }

    public String getSerialization() {
        return serialization;
    }

    public void setSerialization(String serialization) {
        this.serialization = serialization;
    }

    public Integer getSlaveNettyServerPort() {
        return slaveNettyServerPort;
    }

    public void setSlaveNettyServerPort(Integer slaveNettyServerPort) {
        this.slaveNettyServerPort = slaveNettyServerPort;
    }

    public Integer getSlaveBusinessThreadCount() {
        return slaveBusinessThreadCount;
    }

    public void setSlaveBusinessThreadCount(Integer slaveBusinessThreadCount) {
        this.slaveBusinessThreadCount = slaveBusinessThreadCount;
    }

    public Integer getXhakaMsgMaxLength() {
        return xhakaMsgMaxLength;
    }

    public void setXhakaMsgMaxLength(Integer xhakaMsgMaxLength) {
        xhakaMsgMaxLength = xhakaMsgMaxLength;
    }

    public String getLoadBalance() {
        return loadBalance;
    }

    public void setLoadBalance(String loadBalance) {
        this.loadBalance = loadBalance;
    }

    public Integer getSlaveResponseTimeout() {
        return slaveResponseTimeout;
    }

    public void setSlaveResponseTimeout(Integer slaveResponseTimeOut) {
        this.slaveResponseTimeout = slaveResponseTimeOut;
    }

    public Integer getGatewayClientConnCoreThreadSize() {
        return gatewayClientConnCoreThreadSize;
    }

    public void setGatewayClientConnCoreThreadSize(Integer gatewayClientConnCoreThreadSize) {
        this.gatewayClientConnCoreThreadSize = gatewayClientConnCoreThreadSize;
    }

    public Integer getGatewayClientConnMaxThreadSize() {
        return gatewayClientConnMaxThreadSize;
    }

    public void setGatewayClientConnMaxThreadSize(Integer gatewayClientConnMaxThreadSize) {
        this.gatewayClientConnMaxThreadSize = gatewayClientConnMaxThreadSize;
    }
}