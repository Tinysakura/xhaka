package com.tinysakura.xhaka.client.filter;

import com.tinysakura.xhaka.client.context.TomcatServletContext;
import com.tinysakura.xhaka.client.util.ServletFilterUtils;
import com.tinysakura.xhaka.common.filter.FilterChainImpl;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.springframework.stereotype.Component;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/28
 */
@Deprecated
@Component
public class SlaveFilterChainFactory {

    /** 根据代理服务使用的不同的servlet容器创建过滤器链
     *  暂时只适配了tomcat
     * @param httpServletRequest
     * @return
     */
    public static FilterChain createFilterChain(HttpServletRequest httpServletRequest) {
        FilterChainImpl filterChain = new FilterChainImpl();


        if (TomcatServletContext.getInstance().isCurrentWebServer()) {
            FilterMap[] filterMaps = TomcatServletContext.getInstance().getFilterMaps();

            for (FilterMap filterMap : filterMaps) {
                if (!ServletFilterUtils.matcherDispatcher(filterMap, httpServletRequest.getDispatcherType())) {
                    continue;
                }

                if (!ServletFilterUtils.matchFiltersURL(filterMap, httpServletRequest.getRequestURI())) {
                    continue;
                }

                FilterDef filterDef = TomcatServletContext.getInstance().findFilterDef(filterMap.getFilterName());
                if (filterDef != null) {
                    filterChain.addFilter(filterDef.getFilter());
                }
            }

            return filterChain;
        }

        // 找不到合适的servlet容器，返回空的filterChain
        return filterChain;
    }
}