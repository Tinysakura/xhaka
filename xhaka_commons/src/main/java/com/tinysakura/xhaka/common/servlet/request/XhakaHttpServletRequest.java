package com.tinysakura.xhaka.common.servlet.request;

import com.tinysakura.xhaka.common.context.XhakaWebServerContext;
import com.tinysakura.xhaka.common.servlet.parser.CookieParser;
import com.tinysakura.xhaka.common.servlet.parser.ProtocolParser;
import com.tinysakura.xhaka.common.servlet.parser.UriParser;
import com.tinysakura.xhaka.common.servlet.response.XhakaHttpServletResponse;
import com.tinysakura.xhaka.common.util.DateUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.handler.codec.http.multipart.Attribute;
import io.netty.handler.codec.http.multipart.HttpPostRequestDecoder;
import io.netty.handler.codec.http.multipart.InterfaceHttpData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.CollectionUtils;

import javax.servlet.*;
import javax.servlet.http.Cookie;
import javax.servlet.http.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.security.Principal;
import java.util.*;

/**
 * copy from crazymaker-server
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/21
 */
@Slf4j
public class XhakaHttpServletRequest implements HttpServletRequest {
    private static final String DEFAULT_CHARSET = "UTF-8";

    private FullHttpRequest originalRequest;

    private ChannelHandlerContext ctx;

    private XhakaServletInputStream xhakaServletInputStream;

    private HttpHeaders headers;

    private String contextPath;

    private Map<String, Object> attributes;

    private XhakaHttpServletResponse httpServletResponse;

    private Map<String, List<String>> parameters;

    private boolean parameterParsed;

    private String characterEncoding;

    private DispatcherType dispatcherType = DispatcherType.REQUEST;

    private CookieParser cookieParser;

    private ProtocolParser protocolParser;

    private UriParser uriParser;

    public XhakaHttpServletRequest(FullHttpRequest originalRequest,
                                   ChannelHandlerContext ctx) {
        this.originalRequest = originalRequest;
        this.ctx = ctx;
        this.xhakaServletInputStream = new XhakaServletInputStream(originalRequest.content());

        this.headers = originalRequest.headers();
        this.contextPath = XhakaWebServerContext.getInstance().getContextPath();
        this.attributes = new HashMap<>();

        this.cookieParser = new CookieParser(originalRequest);
        this.protocolParser = new ProtocolParser(originalRequest);
        this.uriParser = new UriParser(originalRequest, contextPath);
    }


    @Override
    public String getAuthType() {
        throw new IllegalStateException("getAuthType not support");
    }

    @Override
    public Cookie[] getCookies() {
        return cookieParser.getCookies();
    }

    @Override
    public long getDateHeader(String name) {
        String header = this.headers.get(name);

        if (header == null) {
            return -1L;
        } else {
            Date date = DateUtils.parseDate(header);
            if (date == null) {
                return -1L;
            } else {
                return date.getTime();
            }
        }


    }

    public void addHeader(String name, Object value) {
        this.headers.add(name, value);
    }

    @Override
    public String getHeader(String name) {
        return this.headers.get(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        return Collections.enumeration(this.headers.getAll(name));
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        return Collections.enumeration(this.headers.names());
    }

    @Override
    public int getIntHeader(String name) {
        String headerStringValue = this.headers.get(name);
        if (headerStringValue == null) {
            return -1;
        }
        return Integer.parseInt(headerStringValue);
    }

    @Override
    public String getMethod() {
        return originalRequest.getMethod().name();
    }

    @Override
    public String getPathInfo() {
        return uriParser.getPathInfo();
    }

    @Override
    public String getPathTranslated() {
        throw new IllegalStateException("getPathTranslated not support");
    }

    @Override
    public String getContextPath() {
        return contextPath;
    }

    @Override
    public String getQueryString() {
        return uriParser.getQueryString();
    }

    @Override
    public String getRemoteUser() {
        throw new IllegalStateException("getRemoteUser not support");

    }

    @Override
    public boolean isUserInRole(String role) {
        throw new IllegalStateException("isUserInRole not support");

    }

    @Override
    public Principal getUserPrincipal() {
        throw new IllegalStateException("getUserPrincipal not support");

    }

    @Override
    public String getRequestedSessionId() {
        throw new IllegalStateException("getRequestedSessionId not support");

    }

    @Override
    public String getRequestURI() {
        return uriParser.getRequestURI();
    }

    public void setRequestURI(String uri) {
        this.originalRequest.setUri(uri);
    }

    @Override
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer();
        String scheme = this.getScheme();
        int port = this.getServerPort();
        String urlPath = this.getRequestURI();

        url.append(scheme); // http, https
        url.append("://");
        url.append(this.getServerName());
        if ((scheme.equals("http") && port != 80)
                || (scheme.equals("https") && port != 443)) {
            url.append(':');
            url.append(this.getServerPort());
        }
        url.append(urlPath);
        return url;
    }

    @Override
    public String getServletPath() {
        return uriParser.getServletPath();
    }

    @Override
    public HttpSession getSession(boolean create) {
        return null;
    }

    @Override
    public HttpSession getSession() {
        return null;
    }

    @Override
    public String changeSessionId() {
        return null;
    }

    @Override
    public boolean isRequestedSessionIdValid() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromCookie() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromURL() {
        return false;
    }

    @Override
    public boolean isRequestedSessionIdFromUrl() {
        return false;
    }

    @Override
    public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
        throw new IllegalStateException("authenticate not support");

    }

    @Override
    public void login(String username, String password) throws ServletException {
        throw new IllegalStateException("login not support");

    }

    @Override
    public void logout() throws ServletException {
        throw new IllegalStateException("logout not support");
    }

    /*====== multipart/form-data 相关方法 ======*/
    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        throw new IllegalStateException("getParts not support");

    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        throw new IllegalStateException("getPart not support");

    }

    @Override
    public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) throws IOException, ServletException {
        throw new IllegalStateException("upgrade not support");

    }

    @Override
    public Object getAttribute(String name) {
        return attributes.get(name);
    }

    @Override
    public Enumeration<String> getAttributeNames() {
        return Collections.enumeration(attributes.keySet());
    }

    @Override
    public String getCharacterEncoding() {
        if (characterEncoding == null) {
            characterEncoding = parseCharacterEncoding();
        }

        return characterEncoding;
    }

    private String parseCharacterEncoding() {
        String contentType = getContentType();
        if (contentType == null) {
            return DEFAULT_CHARSET;
        }
        int start = contentType.indexOf("charset=");
        if (start < 0) {
            return DEFAULT_CHARSET;
        }
        String encoding = contentType.substring(start + 8);
        int end = encoding.indexOf(';');
        if (end >= 0) {
            encoding = encoding.substring(0, end);
        }
        encoding = encoding.trim();
        if ((encoding.length() > 2) && (encoding.startsWith("\""))
                && (encoding.endsWith("\""))) {
            encoding = encoding.substring(1, encoding.length() - 1);
        }
        return encoding.trim();
    }

    @Override
    public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
        this.characterEncoding = env;
    }

    @Override
    public int getContentLength() {
        return HttpUtil.getContentLength(originalRequest, -1);
    }

    @Override
    public long getContentLengthLong() {
        return getContentLength();
    }

    @Override
    public String getContentType() {
        return headers.get("content-type");
    }

    @Override
    public ServletInputStream getInputStream() throws IOException {
        return xhakaServletInputStream;
    }

    @Override
    public String getParameter(String name) {
        if (!parameterParsed) {
            parseParameter();
        }

        return CollectionUtils.isEmpty(parameters.get(name)) ? null : parameters.get(name).get(0);
    }

    /**
     * 解析请求参数
     */
    private void parseParameter() {
        if (this.parameterParsed) {
            return;
        }

        HttpMethod method = originalRequest.method();

        QueryStringDecoder decoder = new QueryStringDecoder(originalRequest.uri());
        if (HttpMethod.GET == method) {
            // 是GET请求
            parameters.putAll(decoder.parameters());
        } else if (HttpMethod.POST == method) {
            // 是POST请求
            HttpPostRequestDecoder httpPostRequestDecoder = new HttpPostRequestDecoder(originalRequest);
            try {
                List<InterfaceHttpData> parmList = httpPostRequestDecoder.getBodyHttpDatas();
                for (InterfaceHttpData parm : parmList) {
                    Attribute data = (Attribute) parm;
                    try {
                        parseRequestBody(data);
                    } catch (Exception e) {
                        log.error("HttpPostRequestDecoder error", e);
                    }
                }
            } finally {
                httpPostRequestDecoder.destroy();
            }

        }

        this.parameterParsed = true;
    }

    private void parseRequestBody(Attribute attribute) throws Exception
    {
        if (this.parameters.containsKey(attribute.getName())) {
            this.parameters.get(attribute.getName()).add(attribute.getValue());
        } else {
            List<String> values = new ArrayList<>();
            values.add(attribute.getValue());
            this.parameters.put(attribute.getName(), values);
            this.attributes.put(attribute.getValue(), values);
        }
    }

    @Override
    public Enumeration<String> getParameterNames() {
        if (!parameterParsed) {
            parseParameter();
        }

        return Collections.enumeration(parameters.keySet());
    }

    @Override
    public String[] getParameterValues(String name) {
        if (!parameterParsed) {
            parseParameter();
        }

        List<String> values = this.parameters.get(name);

        return CollectionUtils.isEmpty(values) ? null : values.toArray(new String[]{});
    }

    @Override
    public Map<String, String[]> getParameterMap() {
        if (!parameterParsed) {
            parseParameter();
        }

        Map<String, String[]> parameterMap = new HashMap<>();
        for (Map.Entry<String, List<String>> parameter : parameters.entrySet())
        {
            parameterMap.put(parameter.getKey(), parameter.getValue().toArray(new String[]{}));
        }
        return parameterMap;
    }

    @Override
    public String getProtocol() {
        return protocolParser.getProtocol();
    }

    @Override
    public String getScheme() {
        return protocolParser.getScheme();
    }

    @Override
    public String getServerName() {
        return protocolParser.getHostName();
    }

    @Override
    public int getServerPort() {
        return XhakaWebServerContext.getInstance().getPort();
    }

    @Override
    public BufferedReader getReader() throws IOException {
        return new BufferedReader(new InputStreamReader(xhakaServletInputStream));
    }

    @Override
    public String getRemoteAddr() {
        return ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostAddress();
    }

    @Override
    public String getRemoteHost() {
        return ((InetSocketAddress) ctx.channel().remoteAddress()).getAddress().getHostName();
    }

    @Override
    public void setAttribute(String name, Object o) {
        this.attributes.put(name, o);
    }

    @Override
    public void removeAttribute(String name) {
        this.attributes.remove(name);
    }

    @Override
    public Locale getLocale() {
        throw new IllegalStateException("getLocale not support");
    }

    @Override
    public Enumeration<Locale> getLocales() {
        throw new IllegalStateException("getLocales not support");
    }

    @Override
    public boolean isSecure() {
        return "HTTPS".equalsIgnoreCase(getScheme());
    }

    @Override
    public RequestDispatcher getRequestDispatcher(String path) {
        return null;
    }

    @Override
    public String getRealPath(String path) {
        return null;
    }

    @Override
    public int getRemotePort() {
        return ((InetSocketAddress) ctx.channel().remoteAddress()).getPort();
    }

    @Override
    public String getLocalName() {
        return getServerName();
    }

    @Override
    public String getLocalAddr() {
        return  ((InetSocketAddress) ctx.channel().localAddress()).getAddress().getHostAddress();
    }

    @Override
    public int getLocalPort() {
        return ((InetSocketAddress) ctx.channel().localAddress()).getPort();
    }

    @Override
    public ServletContext getServletContext() {
        return null;
    }

    @Override
    public AsyncContext startAsync() throws IllegalStateException {
        throw new IllegalStateException("not support async");
    }

    @Override
    public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
        throw new IllegalStateException("not support async");
    }

    @Override
    public boolean isAsyncStarted() {
        throw new IllegalStateException("not support async");
    }

    @Override
    public boolean isAsyncSupported() {
        return false;
    }

    @Override
    public AsyncContext getAsyncContext() {
        return null;
    }

    @Override
    public DispatcherType getDispatcherType() {
        return this.dispatcherType;
    }

    public FullHttpRequest getOriginalRequest() {
        return originalRequest;
    }

    public XhakaHttpServletResponse getHttpServletResponse() {
        return httpServletResponse;
    }

    public void setHttpServletResponse(XhakaHttpServletResponse httpServletResponse) {
        this.httpServletResponse = httpServletResponse;
    }
}