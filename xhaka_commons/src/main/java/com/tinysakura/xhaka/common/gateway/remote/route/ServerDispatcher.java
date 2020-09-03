package com.tinysakura.xhaka.common.gateway.remote.route;

import com.tinysakura.xhaka.common.servlet.request.XhakaHttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */
@Component
public class ServerDispatcher {
    private static XhakaRouteProperties xhakaRouteProperties;

    private static final PathMatcher pathMatcher = new AntPathMatcher();

    @Autowired
    public static void setXhakaRouteProperties(XhakaRouteProperties xhakaRouteProperties) {
        ServerDispatcher.xhakaRouteProperties = xhakaRouteProperties;
    }

    public static String getDispatcherServerName(XhakaHttpServletRequest httpServletRequest) {
        String requestURI = httpServletRequest.getRequestURI();

        for (Map.Entry<String, XhakaRouteProperties.Route> entry : xhakaRouteProperties.getRoutes().entrySet()) {
            if (pathMatcher.match(entry.getValue().getUrl(), requestURI)) {
                String matchBody = pathMatcher.extractPathWithinPattern(entry.getValue().getUrl(), requestURI);
                httpServletRequest.setRequestURI("/" + matchBody);

                return entry.getValue().getServerName();
            }
        }

        return null;
    }
}