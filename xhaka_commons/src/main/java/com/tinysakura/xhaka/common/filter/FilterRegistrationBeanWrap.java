package com.tinysakura.xhaka.common.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import java.lang.reflect.Field;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Locale;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/25
 */
@Slf4j
public class FilterRegistrationBeanWrap extends FilterRegistrationBean {
    private FilterRegistrationBean filterRegistrationBean;

    private EnumSet<DispatcherType> dispatcherTypes;

    private static final int NOT_SET = 0;

    private static volatile boolean dispatcherMappingInit = false;

    private int dispatcherMapping = NOT_SET;

    public static final int ERROR = 1;
    public static final int FORWARD = 2;
    public static final int INCLUDE = 4;
    public static final int REQUEST = 8;
    public static final int ASYNC = 16;

    public FilterRegistrationBeanWrap(FilterRegistrationBean filterRegistrationBean) {
        this.filterRegistrationBean = filterRegistrationBean;
    }

    private void setDispatcher(String dispatcherString) {
        String dispatcher = dispatcherString.toUpperCase(Locale.ENGLISH);

        if (dispatcher.equals(DispatcherType.FORWARD.name())) {
            // apply FORWARD to the global dispatcherMapping.
            dispatcherMapping |= FORWARD;
        } else if (dispatcher.equals(DispatcherType.INCLUDE.name())) {
            // apply INCLUDE to the global dispatcherMapping.
            dispatcherMapping |= INCLUDE;
        } else if (dispatcher.equals(DispatcherType.REQUEST.name())) {
            // apply REQUEST to the global dispatcherMapping.
            dispatcherMapping |= REQUEST;
        } else if (dispatcher.equals(DispatcherType.ERROR.name())) {
            // apply ERROR to the global dispatcherMapping.
            dispatcherMapping |= ERROR;
        } else if (dispatcher.equals(DispatcherType.ASYNC.name())) {
            // apply ERROR to the global dispatcherMapping.
            dispatcherMapping |= ASYNC;
        }
    }

    private void initDispatcherMapping() {
        try {
            Field dispatcherTypesField = this.filterRegistrationBean.getClass().getSuperclass().getDeclaredField("dispatcherTypes");
            dispatcherTypesField.setAccessible(true);
            Object o = dispatcherTypesField.get(filterRegistrationBean);
            this.dispatcherTypes = (EnumSet<DispatcherType>) o;

            for (DispatcherType dispatcherType : dispatcherTypes) {
                setDispatcher(dispatcherType.name());
            }
        } catch (Exception e) {
            log.info("getDispatcherTypes failed", e);
        }

        dispatcherMappingInit = true;
    }

    public int getDispatcherMapping() {
        if (!dispatcherMappingInit) {
            initDispatcherMapping();
        }

        // per the SRV.6.2.5 absence of any dispatcher elements is
        // equivalent to a REQUEST value
        if (dispatcherMapping == NOT_SET) {
            return REQUEST;
        }

        return dispatcherMapping;
    }

    @Override
    public Filter getFilter() {
        return this.filterRegistrationBean.getFilter();
    }

    @Override
    public int getOrder() {
        return this.filterRegistrationBean.getOrder();
    }

    @Override
    public Collection<String> getUrlPatterns() {
        return this.filterRegistrationBean.getUrlPatterns();
    }

    @Override
    public Collection<String> getServletNames() {
        return this.filterRegistrationBean.getServletNames();
    }

    @Override
    public Collection<ServletRegistrationBean<?>> getServletRegistrationBeans() {
        return this.filterRegistrationBean.getServletRegistrationBeans();
    }
}