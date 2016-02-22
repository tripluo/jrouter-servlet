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

import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import jrouter.ActionInvocation;

/**
 * ServletThreadContext是一个线程变量，使用了一个公共的{@link ThreadLocal}。
 * ServletThreadContext包含一个{@link ActionInvocation}，存储线程安全的Action运行时上下文。
 * ServletThreadContext包含http的相关的变量。
 * ServletThreadContext包含一个contextMap变量，存储自定义的key-value。
 * ServletThreadContext包含一个Exception对象，存储发生的异常。
 *
 * @see ServletActionFactory.DefaultServletActionFactory
 * @see ServletActionFactory.DefaultServletActionInvocation
 */
public class ServletThreadContext {

    /** Thread Safe */
    private static final ThreadLocal<ServletThreadContext> threadLocal = new ThreadLocal<ServletThreadContext>() {

        @Override
        protected ServletThreadContext initialValue() {
            return new ServletThreadContext(new HashMap<String, Object>(8));
        }

    };

    /** Action运行时上下文 */
    private ActionInvocation<?> actionInvocation;

    /** Http request */
    private HttpServletRequest request;

    /** Http response */
    private HttpServletResponse response;

    /** Http modifiable request parameters map */
    private RequestMap requestMap;

    /** ServletContext */
    private ServletContext servletContext;

    /** Store key-value */
    private final Map<String, Object> contextMap;

    /** Exception */
    private Exception exception;

    /**
     * 构造一个指定存储键值对{@code Map}的ServletThreadContext。
     *
     * @param contextMap 指定存储键值对的Map。
     */
    private ServletThreadContext(Map<String, Object> contextMap) {
        this.contextMap = contextMap;
    }

    /**
     * 获取当前线程副本中的ServletThreadContext。
     *
     * @return 前线程副本中的ServletThreadContext。
     */
    private static ServletThreadContext get() {
        return threadLocal.get();
    }

    /**
     * 移除前线程副本中的ServletThreadContext。
     */
    public static void remove() {
        threadLocal.remove();
    }

    /**
     * 返回Action运行时上下文。
     *
     * @param <T> Action运行时上下文类型。
     *
     * @return Action运行时上下文。
     *
     * @see ServletActionFactory.DefaultServletActionFactory#createActionInvocation
     */
    public static <T extends ActionInvocation> T getActionInvocation() {
        return (T) get().actionInvocation;
    }

    /**
     * 设置Action运行时上下文。
     *
     * @param actionInvocation Action运行时上下文。
     */
    public static void setActionInvocation(ActionInvocation<?> actionInvocation) {
        get().actionInvocation = actionInvocation;
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
     * Gets the HTTP servlet session object.
     *
     * @return the HTTP servlet session object.
     */
    public static HttpSession getSession(boolean create) {
        return getRequest().getSession(create);
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
     * 返回ThreadContext中的Map容器。
     *
     * @return the context map。
     */
    public static Map<String, Object> getContextMap() {
        return get().contextMap;
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
}
