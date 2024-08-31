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

package net.jrouter.http.servlet.filter;

import java.io.IOException;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import net.jrouter.ActionFactory;
import net.jrouter.NotFoundException;
import net.jrouter.http.servlet.ServletActionFactory;
import net.jrouter.http.servlet.ServletThreadContext;
import net.jrouter.impl.InvocationProxyException;
import net.jrouter.util.StringUtil;

/**
 * 抽象 JRouter servlet filter.
 */
@Slf4j
public abstract class AbstractJRouterFilter implements Filter {

    /**
     * Http ServletContext reference.
     */
    protected ServletContext servletContext;

    /**
     * Set http request and response encoding.
     */
    @lombok.Getter
    @lombok.Setter
    private String encoding = null;

    /**
     * 是否对HttpServletRequest Parameter值做去除首位空白处理；默认不处理。
     *
     * @see HttpServletRequest#getParameter(java.lang.String)
     */
    @lombok.Getter
    @lombok.Setter
    private boolean trimRequestParameter = false;

    /**
     * Use {@code ThreadLocal} to store Http parameter object or not.
     */
    @lombok.Getter
    @lombok.Setter
    private boolean useThreadLocal = true;

    /**
     * JRouter {@code ActionFactory}.
     */
    @lombok.Getter
    private ActionFactory actionFactory;

    /**
     * Check if is {@code ServletActionFactory}.
     */
    private boolean isServletActionFactory = false;

    /**
     * Set name of {@code ActionFactory} in ServletContext's attribute.
     */
    @lombok.Getter
    @lombok.Setter
    private String factoryName = null;

    /**
     * Check if need to log {@code NotFoundException}.
     */
    @lombok.Getter
    @lombok.Setter
    private boolean logNotFoundException = true;

    @Override
    public void init(FilterConfig filterConfig) {
        String varEncoding = filterConfig.getInitParameter("encoding");
        String varTrimRequestParameter = filterConfig.getInitParameter("trimRequestParameter");
        String varFactoryName = filterConfig.getInitParameter("factoryName");
        String varUseThreadLocal = filterConfig.getInitParameter("useThreadLocal");
        String varLogNotFoundException = filterConfig.getInitParameter("logNotFoundException");
        if (varEncoding != null) {
            encoding = varEncoding;
            log.info("Set character encoding : {}", encoding);
        }
        // default true if not set
        if (varUseThreadLocal != null) {
            useThreadLocal = Boolean.parseBoolean(varUseThreadLocal);
        }
        if (varTrimRequestParameter != null) {
            trimRequestParameter = Boolean.parseBoolean(varTrimRequestParameter);
        }
        // default true if not set
        if (varLogNotFoundException != null) {
            logNotFoundException = Boolean.parseBoolean(varLogNotFoundException);
        }
        if (StringUtil.isNotBlank(varFactoryName)) {
            factoryName = varFactoryName;
        }
        servletContext = filterConfig.getServletContext();
        try {
            if (useThreadLocal) {
                // 初始化ServletContext, 提供其他模块初始化调用
                ServletThreadContext.setServletContext(servletContext);
            }
            // create ActionFactory
            actionFactory = createActionFactory(filterConfig);
            isServletActionFactory = (actionFactory instanceof ServletActionFactory);
        } finally {
            if (useThreadLocal) {
                ServletThreadContext.remove();
            }
        }
        if (StringUtil.isNotBlank(factoryName)) {
            servletContext.setAttribute(factoryName, actionFactory);
            log.info("Set ActionFactory's name in ServletContext : {}", factoryName);
        }
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException,
            ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (encoding != null) {
            request.setCharacterEncoding(encoding);
            response.setCharacterEncoding(encoding);
        }
        if (trimRequestParameter) {
            Map map = request.getParameterMap();
            if (!(map == null || map.isEmpty())) {
                request = new TrimParameterRequestWrapper(request);
            }
        }
        // create thread local
        if (useThreadLocal) {
            createServletThreadContext(request, response);
        }
        try {
            // action url and invoke
            if (isServletActionFactory) {
                ((ServletActionFactory) actionFactory).invokeAction(getActionPath(request), request, response, servletContext);
            } else {
                actionFactory.invokeAction(getActionPath(request));
            }
            if (!response.isCommitted()) {
                chain.doFilter(request, response);
            }
        } catch (NotFoundException e) {
            if (logNotFoundException) {
                log.error("Not Found - {}", request.getRequestURI(), e);
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (InvocationProxyException e) {
            throw new ServletException(e.getSource()); //NOPMD PreserveStackTrace
        } finally {
            if (useThreadLocal) {
                ServletThreadContext.remove();
            }
        }
    }

    /**
     * A hook to give subclass another way to create {@code ActionFactory}.
     *
     * @param filterConfig 过滤器配置。
     *
     * @return ActionFactory对象。
     */
    abstract ActionFactory createActionFactory(FilterConfig filterConfig);

    @Override
    public void destroy() {
        if (actionFactory != null) {
            actionFactory.clear();
        }
        ServletThreadContext.remove();
    }

    /**
     * Get the action's path from http request.
     *
     * @param request HttpServletRequest.
     *
     * @return the action's path.
     */
    protected String getActionPath(HttpServletRequest request) {
        return request.getServletPath();
    }

    /**
     * Put request and response in thread local variable.
     *
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     */
    protected void createServletThreadContext(HttpServletRequest request, HttpServletResponse response) {
        ServletThreadContext.setRequest(request);
        ServletThreadContext.setResponse(response);
    }

    /**
     * TrimParameterRequestWrapper
     */
    private static final class TrimParameterRequestWrapper extends HttpServletRequestWrapper {

        /**
         * Constructs a request object wrapping the given request.
         *
         * @throws java.lang.IllegalArgumentException if the request is null
         */
        public TrimParameterRequestWrapper(HttpServletRequest request) {
            super(request);
        }

        @Override
        public String getParameter(String name) {
            String val = super.getParameter(name);
            if (val != null) {
                val = val.trim();
            }
            return val;
        }

    }
}
