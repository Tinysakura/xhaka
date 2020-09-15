package com.tinysakura.xhaka.client.context;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.AsyncDispatcher;
import org.apache.catalina.core.ApplicationContextFacade;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

import javax.servlet.RequestDispatcher;
import java.lang.reflect.Field;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/28
 */
@Slf4j
@Component
public class TomcatServletContext implements ApplicationContextAware {
    private FilterMap[] filterMaps;

    private StandardContext standardContext;

    private org.apache.catalina.core.ApplicationContext applicationContext;

    private static TomcatServletContext instance;

    private static boolean currentWebServer = false;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        assert applicationContext instanceof WebApplicationContext;

        WebApplicationContext webApplicationContext = (WebApplicationContext) applicationContext;

        if (!(webApplicationContext.getServletContext() instanceof ApplicationContextFacade)) {
            currentWebServer = false;
            return;
        }

        ApplicationContextFacade applicationContextFacade = (ApplicationContextFacade) webApplicationContext.getServletContext();

        try {
            Field contextField = applicationContextFacade.getClass().getDeclaredField("context");
            contextField.setAccessible(true);
            org.apache.catalina.core.ApplicationContext tomcatApplicationContext = (org.apache.catalina.core.ApplicationContext) contextField.get(applicationContextFacade);
            this.applicationContext = tomcatApplicationContext;

            Field standardContextField = tomcatApplicationContext.getClass().getDeclaredField("context");
            standardContextField.setAccessible(true);
            StandardContext standardContext = (StandardContext) standardContextField.get(tomcatApplicationContext);

            this.filterMaps = standardContext.findFilterMaps();
            this.standardContext = standardContext;
            currentWebServer = true;
            instance = this;
        } catch (Exception e) {
            log.info("init TomcatServletContext failed : {}", e);
        }
    }

    public FilterMap[] getFilterMaps() {
        return this.filterMaps;
    }

    public boolean isCurrentWebServer() {
        return currentWebServer;
    }

    public FilterDef findFilterDef(String filterName) {
        return standardContext.findFilterDef(filterName);
    }

    public StandardContext getStandardContext() {
        return standardContext;
    }

    public org.apache.catalina.core.ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public RequestDispatcher getDispatch(String requestURI) {
        return applicationContext.getRequestDispatcher(requestURI);
    }

    public static TomcatServletContext getInstance() {
        return instance;
    }
}