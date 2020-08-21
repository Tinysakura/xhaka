package com.tinysakura.xhaka.common.servlet.parser;

import io.netty.handler.codec.http.HttpRequest;

/**
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/21
 */
public class UriParser
{
    /**
     * 懒加载：是否已经解析过
     */
    boolean isPathsParsed = false;

    /**
     * 请求实例
     */
    private final HttpRequest request;
    /**
     * 容器上下文路径
     */
    private final String contextPath;
    /**
     * servlet 路径
     */
    private String servletPath;
    /**
     * 请求URI，request uri=Context Path + servlet path + path info
     */
    private String requestUri;
    /**
     * pathInfo= requestUri - contextPath - servletPath
     */
    private String pathInfo;
    /**
     * 查询字符串
     */
    private String queryString;

    public UriParser(HttpRequest request, String contextPath)
    {
        this.request = request;
        this.contextPath = contextPath;

    }

    public String getServletPath()
    {
        checkAndParsePaths();
        return this.servletPath;
    }

    public String getQueryString()
    {
        checkAndParsePaths();
        return this.queryString;
    }

    public String getPathInfo()
    {
        checkAndParsePaths();
        return this.pathInfo;
    }

    public String getRequestUri()
    {
        return requestUri;
    }

    public String getRequestURI()
    {
        checkAndParsePaths();
        return this.requestUri;
    }
    //Context Path + servlet path + path info = request uri
    //TODO ServletPath和PathInfo应该是互补的，根据URL-Pattern匹配的路径不同而不同
    // 现在把PathInfo恒为null，ServletPath恒为uri-contextPath
    // 可以满足SpringBoot的需求，但不满足ServletPath和PathInfo的语义
    // 需要在RequestUrlPatternMapper匹配的时候设置,new NettyRequestDispatcher的时候传入MapperData


    private void checkAndParsePaths()
    {
        if (isPathsParsed)
        {
            return;
        }
        /**
         * 临时变量，上下文路径
         */
        if (request.uri().startsWith(contextPath))
        {
            /**
             * 如果请求路径，包含了上下文路径，servletPath 去掉上下文路径
             */
            String servletPath = request.uri().substring(contextPath.length());
            if (!servletPath.startsWith("/"))
            {
                servletPath = "/" + servletPath;
            }
            /**
             * 截取查询字符串
             */
            int queryInx = servletPath.indexOf('?');
            if (queryInx > -1)
            {
                this.queryString = servletPath.substring(queryInx + 1);
                servletPath = servletPath.substring(0, queryInx);
            }
            this.servletPath = servletPath;

            //TODO 加上pathInfo
            this.requestUri = contextPath + servletPath;
        } else
        {
            this.servletPath = "";
            this.requestUri = "";
        }
        this.pathInfo = null;

        isPathsParsed = true;
    }
}
