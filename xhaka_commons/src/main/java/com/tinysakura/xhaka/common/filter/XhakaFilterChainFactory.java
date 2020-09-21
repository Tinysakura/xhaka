package com.tinysakura.xhaka.common.filter;

import com.tinysakura.xhaka.common.util.ServletFilterUtils;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.servlet.DispatcherType;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * refer to org.apache.catalina.core.ApplicationFilterFactory
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/25
 */
@Component
public class XhakaFilterChainFactory {
    public static FilterChain createFilterChain(HttpServletRequest httpServletRequest, ChannelHandlerContext ctx) {
        FilterChainImpl filterChain = new FilterChainImpl(ctx);

        List<FilterRegistrationBeanWrap> servletFilterRegistList = XhakaWebServerFilterContext.getServletFilterRegistList();

        if (CollectionUtils.isEmpty(servletFilterRegistList)) {
            return filterChain;
        }

        DispatcherType dispatcherType = httpServletRequest.getDispatcherType();
        String requestURI = httpServletRequest.getRequestURI();

        for (FilterRegistrationBeanWrap filterRegistrationBeanWrap : servletFilterRegistList) {
            if (!ServletFilterUtils.matchDispatcher(filterRegistrationBeanWrap, dispatcherType)) {
                continue;
            }

            if (!ServletFilterUtils.matchFiltersURL(filterRegistrationBeanWrap, requestURI)) {
                continue;
            }

            filterChain.addFilter(filterRegistrationBeanWrap.getFilter());
        }

        return filterChain;
    }

}