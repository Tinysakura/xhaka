package com.tinysakura.xhaka.client.filter;

import net.bytebuddy.implementation.bind.annotation.AllArguments;
import net.bytebuddy.implementation.bind.annotation.Super;
import org.apache.catalina.core.ApplicationFilterRegistration;

import javax.servlet.DispatcherType;
import java.util.EnumSet;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/10/14
 */

public class FilterAsyncDispatcherSupportEnhance {

    public static void addMappingForServletNames(
            @AllArguments Object[] allParams, @Super ApplicationFilterRegistration zuper) {

        EnumSet<DispatcherType> dispatcherTypes = (EnumSet<DispatcherType>) allParams[0];
        boolean isMatchAfter = (boolean) allParams[1];
        String[] servletNames = (String[]) allParams[2];

        if (!dispatcherTypes.contains(DispatcherType.ASYNC)) {
            dispatcherTypes.add(DispatcherType.ASYNC);
        }

        zuper.addMappingForServletNames(dispatcherTypes, isMatchAfter, servletNames);
    }
}