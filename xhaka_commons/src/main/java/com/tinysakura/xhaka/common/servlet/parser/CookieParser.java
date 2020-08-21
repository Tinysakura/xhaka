package com.tinysakura.xhaka.common.servlet.parser;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.cookie.Cookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;
import org.apache.commons.lang3.StringUtils;

import java.util.Set;

/**
 * 解析Http协议中的cookie内容
 * copy from crazymaker-server
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/21
 */
public class CookieParser {
    /**
     * 懒加载：是否已经解析过
     */
    boolean isParsed = false;

    /**
     * 请求实例
     */
    private final HttpRequest request;
    /**
     * 请求头
     */
    private final HttpHeaders headers;

    /**
     * cookies数组
     */
    private javax.servlet.http.Cookie[] cookies;

    public CookieParser(HttpRequest request)
    {
        this.request = request;
        this.headers = request.headers();
    }


    /**
     * 解析request中的Cookie到本类的cookies数组中
     */
    private void checkAndParse()
    {
        if (isParsed)
        {
            return;
        }

        String cookieOriginStr = this.headers.get("Cookie");
        if (cookieOriginStr == null)
        {
            return;
        }
        Set<Cookie> nettyCookies = ServerCookieDecoder.LAX.decode(cookieOriginStr);
        if (nettyCookies.size() == 0)
        {
            return;
        }
        this.cookies = new javax.servlet.http.Cookie[nettyCookies.size()];
        int index = 0;
        for (io.netty.handler.codec.http.cookie.Cookie nettyCookie : nettyCookies)
        {
            javax.servlet.http.Cookie servletCookie =
                    new javax.servlet.http.Cookie(nettyCookie.name(), nettyCookie.value());
            servletCookie.setDomain(StringUtils.defaultString(nettyCookie.domain()));
            servletCookie.setMaxAge((int) nettyCookie.maxAge());
            servletCookie.setPath(nettyCookie.path());
            servletCookie.setSecure(nettyCookie.isSecure());
            servletCookie.setHttpOnly(nettyCookie.isHttpOnly());
            this.cookies[index] = servletCookie;
            index++;
        }
        isParsed = true;

    }


    public javax.servlet.http.Cookie[] getCookies()
    {
        checkAndParse();
        return cookies;
    }
}
