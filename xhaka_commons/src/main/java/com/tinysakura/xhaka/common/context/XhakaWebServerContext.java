package com.tinysakura.xhaka.common.context;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/21
 */

public class XhakaWebServerContext {
    public static String XHAKA_WEB_SERVER_INFO = "xhaka-gateway";

    private String contextPath;

    private Integer port;

    private static XhakaWebServerContext instance;

    public XhakaWebServerContext(String contextPath, Integer port) {
        instance = this;

        this.contextPath = contextPath;
        this.port = port;
    }

    public static XhakaWebServerContext getInstance() {
        return instance;
    }

    public String getContextPath() {
        return contextPath;
    }

    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getServerInfo() {
        return XHAKA_WEB_SERVER_INFO;
    }
}