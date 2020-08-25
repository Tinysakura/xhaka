package com.tinysakura.xhaka;

import com.tinysakura.xhaka.filter.TestFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/25
 */
@Configuration
public class FilterConfiguration {

    @Bean
    public FilterRegistrationBean httpToHttpsFilterRegistrationBean() {

        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new TestFilter());

        registration.addUrlPatterns("/*");

        return registration;
    }

}