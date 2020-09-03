package com.tinysakura.xhaka.client.util;

import com.tinysakura.xhaka.common.filter.FilterRegistrationBeanWrap;
import org.apache.tomcat.util.descriptor.web.FilterMap;

import javax.servlet.DispatcherType;
import java.util.Collection;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/28
 */

public class ServletFilterUtils {

    public static boolean matchFiltersURL(FilterRegistrationBeanWrap filterRegistrationBeanWrap, String requestPath) {
        if (requestPath == null) {
            return false;
        }

        // Match on context relative request path
        Collection<String> urlPatterns = filterRegistrationBeanWrap.getUrlPatterns();

        for (String urlPattern : urlPatterns) {
            if (matchFiltersURL(urlPattern, requestPath)) {
                return true;
            }
        }

        // No match
        return false;
    }

    public static boolean matchFiltersURL(FilterMap filterMap, String requestPath) {
        if (requestPath == null) {
            return false;
        }

        for (String urlPattern : filterMap.getURLPatterns()) {
            if (matchFiltersURL(urlPattern, requestPath)) {
                return true;
            }
        }

        // Not match
        return false;
    }

    public static boolean matchDispatcher(FilterRegistrationBeanWrap filterRegistrationBeanWrap, DispatcherType type) {
        switch (type) {
            case FORWARD :
                if ((filterRegistrationBeanWrap.getDispatcherMapping() & FilterRegistrationBeanWrap.FORWARD) != 0) {
                    return true;
                }
                break;
            case INCLUDE :
                if ((filterRegistrationBeanWrap.getDispatcherMapping() & FilterRegistrationBeanWrap.INCLUDE) != 0) {
                    return true;
                }
                break;
            case REQUEST :
                if ((filterRegistrationBeanWrap.getDispatcherMapping() & FilterRegistrationBeanWrap.REQUEST) != 0) {
                    return true;
                }
                break;
            case ERROR :
                if ((filterRegistrationBeanWrap.getDispatcherMapping() & FilterRegistrationBeanWrap.ERROR) != 0) {
                    return true;
                }
                break;
            case ASYNC :
                if ((filterRegistrationBeanWrap.getDispatcherMapping() & FilterRegistrationBeanWrap.ASYNC) != 0) {
                    return true;
                }
                break;
        }
        return false;
    }

    public static boolean matcherDispatcher(FilterMap filterMap, DispatcherType type) {
        switch (type) {
            case FORWARD:
                if ((filterMap.getDispatcherMapping() & FilterRegistrationBeanWrap.FORWARD) != 0) {
                    return true;
                }
                break;
            case INCLUDE:
                if ((filterMap.getDispatcherMapping() & FilterRegistrationBeanWrap.INCLUDE) != 0) {
                    return true;
                }
                break;
            case REQUEST:
                if ((filterMap.getDispatcherMapping() & FilterRegistrationBeanWrap.REQUEST) != 0) {
                    return true;
                }
                break;
            case ERROR:
                if ((filterMap.getDispatcherMapping() & FilterRegistrationBeanWrap.ERROR) != 0) {
                    return true;
                }
                break;
            case ASYNC:
                if ((filterMap.getDispatcherMapping() & FilterRegistrationBeanWrap.ASYNC) != 0) {
                    return true;
                }
                break;
        }
        return false;
    }

    public static boolean matchFiltersURL(String urlPattern, String requestPath) {
        if (urlPattern == null) {
            return false;
        }

        // Case 1 - Exact Match
        if (urlPattern.equals(requestPath)) {
            return true;
        }

        // Case 2 - Path Match ("/.../*")
        if ("/*".equals(urlPattern)) {
            return true;
        }

        if (urlPattern.endsWith("/*")) {
            if (urlPattern.regionMatches(0, requestPath, 0,
                    urlPattern.length() - 2)) {
                if (requestPath.length() == (urlPattern.length() - 2)) {
                    return true;
                } else if ('/' == requestPath.charAt(urlPattern.length() - 2)) {
                    return true;
                }
            }
            return false;
        }

        // Case 3 - Extension Match
        if (urlPattern.startsWith("*.")) {
            int slash = requestPath.lastIndexOf('/');
            int period = requestPath.lastIndexOf('.');
            if ((slash >= 0) && (period > slash)
                    && (period != requestPath.length() - 1)
                    && ((requestPath.length() - period)
                    == (urlPattern.length() - 1))) {
                return urlPattern.regionMatches(2, requestPath, period + 1,
                        urlPattern.length() - 2);
            }
        }

        // Case 4 - "Default" Match
        return false; // NOTE - Not relevant for selecting filters
    }
}