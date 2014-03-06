/*
 * Copyright (C) 2010-2111 sunjumper@163.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package jrouter.servlet;

import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.jsp.PageContext;
import jrouter.impl.ThreadContext;

/**
 * ServletThreadContext是一个线程变量，继承自{@link ThreadContext}。
 * ServletThreadContext包含http的相关的变量，因而它们都是线程安全的。
 */
public class ServletThreadContext extends ThreadContext {

    /** Http request */
    private HttpServletRequest request;

    /** Http response */
    private HttpServletResponse response;

    /** Http request parameters map */
    private RequestMap requestMap;

    /** page context */
    private PageContext pageContext;

    /** ServletContext */
    private ServletContext servletContext;

    /** Exception */
    private Exception exception;

    /** store key-value */
    private Map<String, Object> contextMap;

    /**
     * 构造一个指定存储键值对<code>Map</code>的ServletThreadContext。
     *
     * @param contextMap 指定存储键值对的Map。
     */
    public ServletThreadContext(Map<String, Object> contextMap) {
        super();
        this.contextMap = contextMap;
    }

    /**
     * 获取当前线程副本中的ServletThreadContext。
     *
     * @return 前线程副本中的ServletThreadContext。
     */
    public static ServletThreadContext get() {
        return ThreadContext.get();
    }

    /**
     * Returns the HTTP page context.
     *
     * @param pageContext the HTTP page context.
     */
    public static void setPageContext(PageContext pageContext) {
        get().pageContext = pageContext;
    }

    /**
     * Returns the HTTP page context.
     *
     * @return the HTTP page context.
     */
    public static PageContext getPageContext() {
        return get().pageContext;
    }

    /**
     * Sets the HTTP servlet request object.
     *
     * @param request the HTTP servlet request object.
     */
    public static void setRequest(HttpServletRequest request) {
        get().request = request;
        get().requestMap = new RequestMap(request);
    }

    /**
     * Gets the HTTP servlet request object.
     *
     * @return the HTTP servlet request object.
     */
    public static HttpServletRequest getRequest() {
        return get().request;
    }

    /**
     * Gets the HTTP servlet session object.
     *
     * @return the HTTP servlet session object.
     */
    public static HttpSession getSession() {
        return getRequest().getSession();
    }

    /**
     * Gets the HTTP servlet request parameters.
     *
     * @return the HTTP servlet request parameters.
     */
    public static Map<String, String[]> getRequestParameters() {
        return get().requestMap;
    }

    /**
     * Sets the HTTP servlet response object.
     *
     * @param response the HTTP servlet response object.
     */
    public static void setResponse(HttpServletResponse response) {
        get().response = response;
    }

    /**
     * Gets the HTTP servlet response object.
     *
     * @return the HTTP servlet response object.
     */
    public static HttpServletResponse getResponse() {
        return get().response;

    }

    /**
     * Gets the servlet context.
     *
     * @return the servlet context.
     */
    public static ServletContext getServletContext() {
        return get().servletContext;
    }

    /**
     * Sets the current servlet context object.
     *
     * @param servletContext The servlet context to use
     */
    public static void setServletContext(ServletContext servletContext) {
        get().servletContext = servletContext;
    }

    /**
     * Get the exception.
     *
     * @return the exception
     */
    public static Exception getException() {
        return get().exception;
    }

    /**
     * Store the exception.
     *
     * @param exception The exception
     */
    public static void setException(Exception exception) {
        get().exception = exception;
    }

    /**
     * 设置ThreadContext中的Map容器。
     *
     * @param contextMap the context map.
     */
    public static void setContextMap(Map<String, Object> contextMap) {
        get().contextMap = contextMap;
    }

    /**
     * 返回ThreadContext中的Map容器。
     *
     * @return the context map。
     */
    public static Map<String, Object> getContextMap() {
        return get().contextMap;
    }
}
