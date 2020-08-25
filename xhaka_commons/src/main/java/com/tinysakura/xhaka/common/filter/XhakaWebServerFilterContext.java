package com.tinysakura.xhaka.common.filter;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/21
 */
@Component
public class XhakaWebServerFilterContext implements ApplicationContextAware, InitializingBean {
    private ApplicationContext applicationContext;

    private static List<FilterRegistrationBeanWrap> servletFilterRegistList = new ArrayList<>();

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void afterPropertiesSet() {
        List<FilterRegistrationBean> filterRegistrationBeanList = new ArrayList<>();
        String[] servletFilterRegistrationNames = this.applicationContext.getBeanNamesForType(FilterRegistrationBean.class);

        for (String filterRegistrationName : servletFilterRegistrationNames) {
            FilterRegistrationBean filterRegistrationBean = applicationContext.getBean(filterRegistrationName, FilterRegistrationBean.class);
            filterRegistrationBeanList.add(filterRegistrationBean);
        }

        if (servletFilterRegistrationNames.length == 0) {
            return;
        }

        // 根据order对filter排序
        filterRegistrationBeanList = filterRegistrationBeanList.stream().sorted(Comparator.comparingInt(FilterRegistrationBean::getOrder)).collect(Collectors.toList());

        for (FilterRegistrationBean filterRegistrationBean : filterRegistrationBeanList) {
            servletFilterRegistList.add(new FilterRegistrationBeanWrap(filterRegistrationBean));
        }
    }

    public static List<FilterRegistrationBeanWrap> getServletFilterRegistList() {
        return servletFilterRegistList;
    }
}