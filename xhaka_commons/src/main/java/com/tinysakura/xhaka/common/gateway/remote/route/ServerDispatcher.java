package com.tinysakura.xhaka.common.gateway.remote.route;

import com.tinysakura.xhaka.common.servlet.request.XhakaHttpServletRequest;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;

import java.util.Map;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/31
 */
@Component
public class ServerDispatcher {
    private ApplicationContext applicationContext;

    private static final PathMatcher pathMatcher = new AntPathMatcher();

    public static String getDispatcherServerName(XhakaHttpServletRequest httpServletRequest) {
        String requestURI = httpServletRequest.getRequestURI();

        // if request not belong contextPath, return 404
        if (!requestURI.startsWith(httpServletRequest.getContextPath())) {
            return null;
        }

        requestURI = requestURI.replaceFirst(httpServletRequest.getContextPath(), "");

        for (Map.Entry<String, XhakaRouteProperties.Route> entry : XhakaRouteProperties.getInstance().getRoutes().entrySet()) {
            if (pathMatcher.match(entry.getValue().getUrl(), requestURI)) {
                String matchBody = pathMatcher.extractPathWithinPattern(entry.getValue().getUrl(), requestURI);
                httpServletRequest.setRequestURI("/" + matchBody);

                return entry.getValue().getServerName();
            }
        }

        return null;
    }
}