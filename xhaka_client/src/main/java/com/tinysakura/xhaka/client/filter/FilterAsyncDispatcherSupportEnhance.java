package com.tinysakura.xhaka.client.filter;

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
            @Super ApplicationFilterRegistration zuper,
            EnumSet<DispatcherType> dispatcherTypes, boolean isMatchAfter,
            String[] servletNames) {
        if (!dispatcherTypes.contains(DispatcherType.ASYNC)) {
            dispatcherTypes.add(DispatcherType.ASYNC);
        }

        zuper.addMappingForServletNames(dispatcherTypes, isMatchAfter, servletNames);
    }
}