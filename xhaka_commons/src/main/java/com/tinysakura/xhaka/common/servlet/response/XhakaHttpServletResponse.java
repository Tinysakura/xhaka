package com.tinysakura.xhaka.common.servlet.response;

import com.tinysakura.xhaka.common.context.XhakaWebServerContext;
import com.tinysakura.xhaka.common.servlet.request.XhakaHttpServletRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.AsciiString;
import io.netty.util.concurrent.FastThreadLocal;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * copy from crazymaker-server
 * @Author: chenfeihao@corp.netease.com
 * @Date: 2020/8/21
 */

public class XhakaHttpServletResponse implements HttpServletResponse {

    private FullHttpResponse originResponse;

    private XhakaServletOutputStream xhakaServletOutputSteam;

    private ChannelHandlerContext ctx;

    private PrintWriter printWriter;

    /**
     * 提交状态
     */
    private boolean responseCommitted = false;

    /**
     * 字符编码
     */
    private String characterEncoding;

    /**
     * 内容类型
     */
    private String contentType;


    private XhakaHttpServletRequest requestFacade;

    private List<Cookie> cookies;


    public XhakaHttpServletResponse(XhakaHttpServletRequest requestFacade, ChannelHandlerContext ctx) {
        this.requestFacade = requestFacade;
        ByteBuf directBuffer = PooledByteBufAllocator.DEFAULT.directBuffer();
        this.xhakaServletOutputSteam = new XhakaServletOutputStream(this, directBuffer);
        this.ctx = ctx;

        this.originResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, directBuffer);
        this.printWriter = new PrintWriter(xhakaServletOutputSteam);
        this.cookies = new ArrayList<>();
    }

    public XhakaHttpServletResponse(XhakaHttpServletRequest requestFacade, FullHttpResponse fullHttpResponse, ChannelHandlerContext ctx) {
        this.requestFacade = requestFacade;
        this.xhakaServletOutputSteam = new XhakaServletOutputStream(this, fullHttpResponse.content());
        this.ctx = ctx;

        this.originResponse = fullHttpResponse;
        this.printWriter = new PrintWriter(xhakaServletOutputSteam);
        this.cookies = new ArrayList<>();
    }

    public void replaceOriginResponse(FullHttpResponse newResponse) {
        this.originResponse.release();
        FullHttpResponse oldResponse = this.originResponse;

        this.originResponse = newResponse;
        this.xhakaServletOutputSteam = new XhakaServletOutputStream(this, newResponse.content());
        this.printWriter = new PrintWriter(xhakaServletOutputSteam);

        // add old header if not exist
        for (String headerName : oldResponse.headers().names()) {
            if (getHeader(headerName) == null) {
                addHeader(headerName, oldResponse.headers().get(headerName));
            }
        }
    }

    @Override
    public void addCookie(Cookie cookie) {
        cookies.add(cookie);
    }

    @Override
    public boolean containsHeader(String name) {
        return this.originResponse.headers().contains(name);
    }

    @Override
    public String encodeURL(String url) {
        try {
            String characterEncoding = getCharacterEncoding();
            if (StringUtils.isEmpty(characterEncoding)) {
                return URLEncoder.encode(url);
            }
            return URLEncoder.encode(url, getCharacterEncoding());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Error encoding url!", e);
        }
    }

    @Override
    public String encodeRedirectURL(String url) {
        return encodeURL(url);
    }

    @Override
    public String encodeUrl(String url) {
        return encodeURL(url);
    }

    @Override
    public String encodeRedirectUrl(String url) {
        return encodeURL(url);
    }

    @Override
    public void sendError(int sc, String msg) throws IOException {
        this.originResponse.setStatus(new HttpResponseStatus(sc, msg));
    }

    @Override
    public void sendError(int sc) throws IOException {
        this.originResponse.setStatus(HttpResponseStatus.valueOf(sc));
    }

    @Override
    public void sendRedirect(String location) throws IOException {
        setStatus(SC_FOUND);
        setHeader(HttpHeaderNames.LOCATION.toString(), location);
    }

    @Override
    public void setDateHeader(String name, long date) {
        this.originResponse.headers().set(name, date);
    }

    @Override
    public void addDateHeader(String name, long date) {
        this.originResponse.headers().add(name, date);
    }

    @Override
    public void setHeader(String name, String value) {
        this.originResponse.headers().set(name, value);
    }

    @Override
    public void addHeader(String name, String value) {
        this.originResponse.headers().add(name, value);
    }

    @Override
    public void setIntHeader(String name, int value) {
        this.originResponse.headers().set(name, value);
    }

    @Override
    public void addIntHeader(String name, int value) {
        this.originResponse.headers().add(name, value);
    }

    @Override
    public void setStatus(int sc) {
        this.originResponse.setStatus(HttpResponseStatus.valueOf(sc));
    }

    @Override
    public void setStatus(int sc, String sm) {
        this.originResponse.setStatus(new HttpResponseStatus(sc, sm));
    }

    @Override
    public int getStatus() {
        return originResponse.status().code();
    }

    @Override
    public String getHeader(String name) {
        return originResponse.headers().get(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return Arrays.asList(originResponse.headers().get(name));
    }

    @Override
    public Collection<String> getHeaderNames() {
        return this.originResponse.headers().names();
    }

    @Override
    public String getCharacterEncoding() {
        return this.originResponse.headers().get(HttpHeaderNames.CONTENT_ENCODING);
    }

    @Override
    public String getContentType() {
        return this.originResponse.headers().get(HttpHeaderNames.CONTENT_TYPE);
    }

    @Override
    public ServletOutputStream getOutputStream() throws IOException {
        return xhakaServletOutputSteam;
    }

    @Override
    public PrintWriter getWriter() throws IOException {
        return printWriter;
    }

    @Override
    public void setCharacterEncoding(String charset) {
        this.originResponse.headers().set(HttpHeaderNames.CONTENT_ENCODING, charset);
        this.characterEncoding = charset;
    }

    @Override
    public void setContentLength(int len) {
        HttpUtil.setContentLength(originResponse, len);
    }

    @Override
    public void setContentLengthLong(long len) {
        HttpUtil.setContentLength(originResponse, len);
    }

    @Override
    public void setContentType(String type) {
        this.originResponse.headers().set(HttpHeaderNames.CONTENT_TYPE, type);
        this.contentType = type;
    }

    @Override
    public void setBufferSize(int size) {
        // todo ByteBuf capacity大小暂时不支持调整
        throw new IllegalStateException("not support setBufferSize");
    }

    @Override
    public int getBufferSize() {
        return this.xhakaServletOutputSteam.getBufferSize();
    }

    @Override
    public void flushBuffer() throws IOException {
        this.xhakaServletOutputSteam.flush();
    }

    @Override
    public void resetBuffer() {
        if (isCommitted()) {
            throw new IllegalStateException("response has reset");
        }

        xhakaServletOutputSteam.resetBuffer();
    }

    @Override
    public boolean isCommitted() {
        return this.responseCommitted;
    }

    @Override
    public void reset() {
        if (isCommitted()) {
            throw new IllegalStateException("response has reset");
        }

        this.originResponse.headers().clear();
        resetBuffer();
    }

    @Override
    public void setLocale(Locale loc) {
        throw new IllegalStateException("not support setLocale");
    }

    @Override
    public Locale getLocale() {
        throw new IllegalStateException("not support getLocale");
    }

    public ChannelHandlerContext getCtx() {
        return ctx;
    }

    public FullHttpResponse getOriginResponse() {
        return originResponse;
    }

    public void setResponseBasicHeader() {
        HttpRequest request = requestFacade.getOriginalRequest();
        HttpUtil.setKeepAlive(originResponse, HttpUtil.isKeepAlive(request));


        HttpHeaders headers = originResponse.headers();
//        String contentType = headers.get(HttpHeaderNames.CONTENT_TYPE);
//        if (StringUtils.isEmpty(contentType)) {
//            //Content Type 如果响应头为空，设置响应头的内容
//            String value = null == characterEncoding ? contentType : contentType + "; charset=" + characterEncoding;
//            headers.set(HttpHeaderNames.CONTENT_TYPE, value);
//        }
        if (!HttpUtil.isContentLengthSet(originResponse)) {
            HttpUtil.setContentLength(originResponse, this.originResponse.content().readableBytes());
        }
        if (StringUtils.isEmpty( headers.get(HttpHeaderNames.DATE))) {
            CharSequence date = getFormattedDate();
            headers.set(HttpHeaderNames.DATE, date); // 时间日期响应头
        }

        headers.set(HttpHeaderNames.SERVER, XhakaWebServerContext.getInstance().getServerInfo()); //服务器信息响应头

        //其他业务或框架设置的cookie，逐条写入到响应头去

        for (Cookie cookie : cookies) {
            StringBuilder sb = new StringBuilder();
            sb.append(cookie.getName()).append("=").append(cookie.getValue())
                    .append("; max-Age=").append(cookie.getMaxAge());
            if (cookie.getPath() != null) {
                sb.append("; path=").append(cookie.getPath());
            }
            if (cookie.getDomain() != null) {
                sb.append("; domain=").append(cookie.getDomain());
            }
            headers.add(HttpHeaderNames.SET_COOKIE, sb.toString());
        }
    }

    /**
     * DateFormat非线程安全
     */
    private static final FastThreadLocal<DateFormat> FORMAT = new FastThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue()
        {
            DateFormat df = new SimpleDateFormat("EEE, dd-MMM-yyyy HH:mm:ss z", Locale.ENGLISH);
            df.setTimeZone(TimeZone.getTimeZone("GMT"));
            return df;
        }
    };

    private CharSequence getFormattedDate() {
        return new AsciiString(FORMAT.get().format(new Date()));
    }
}