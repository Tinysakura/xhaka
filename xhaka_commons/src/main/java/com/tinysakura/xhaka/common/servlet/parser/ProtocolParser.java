package com.tinysakura.xhaka.common.servlet.parser;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

import java.net.InetSocketAddress;


/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/21
 */
public class ProtocolParser {
    /**
     * 懒加载：是否已经解析过
     */
    boolean isParsed = false;

    /**
     * 请求实例
     */
    private final HttpRequest request;
    /**
     * 请求头
     */
    private final HttpHeaders headers;
    /**
     * 请求的服务地址
     */
    private InetSocketAddress socketAddress;


    private String hostName;

    public ProtocolParser(HttpRequest request) {
        this.request = request;
        headers = request.headers();
    }


    /**
     * 解析主机和端口
     */
    private void checkAndParse() {
        if (isParsed) {
            return;
        }
        String hostHeader = headers.get(HttpHeaderNames.HOST);
        if (hostHeader != null) {
            hostHeader = hostHeader.trim();
            if (hostHeader.startsWith("[")) {
                hostHeader = hostHeader.substring(1, hostHeader.indexOf(']'));
            }
            hostName = hostHeader;

            String[] parsed = hostHeader.split(":");
            if (parsed.length > 1) {
                socketAddress = new InetSocketAddress(parsed[0], Integer.parseInt(parsed[1]));
            } else {
                socketAddress = new InetSocketAddress(parsed[0], 80);
            }
            hostName = parsed[0];

        }
        isParsed = true;
    }


    /**
     * 获取协议和版本
     *
     * @return 协议和版本
     */
    public String getProtocol() {
        return request.protocolVersion().toString();
    }

    /**
     * 获取协议 Scheme
     *
     * @return Scheme
     */
    public String getScheme() {
        return request.protocolVersion().protocolName();
    }

    /**
     * 取得请求的主机名称
     *
     * @return port
     */
    public String getHostName() {
        checkAndParse();
        return hostName;
    }
}
