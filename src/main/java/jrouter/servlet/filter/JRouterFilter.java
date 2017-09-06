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
package jrouter.servlet.filter;

import java.io.IOException;
import java.util.Map;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import jrouter.ActionFactory;
import jrouter.NotFoundException;
import jrouter.config.Configuration;
import jrouter.impl.InvocationProxyException;
import jrouter.servlet.ServletActionFactory;
import jrouter.servlet.ServletThreadContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JRouter servlet filter.通过configLocation配置Configuration进而加载ActionFactory对象。
 *
 * @see jrouter.config.Configuration
 */
public class JRouterFilter implements Filter {

    /** Log */
    protected final Logger log = LoggerFactory.getLogger(getClass());

    /** Set http request and response encoding */
    protected String encoding = null;

    /**
     * 是否对HttpServletRequest Parameter值做去除首位空白处理；默认不处理。
     *
     * @see HttpServletRequest#getParameter(java.lang.String)
     */
    private boolean trimRequestParameter = false;

    /**
     * Location of the jrouter ActionFactory's configuration file, default load resource file jrouter.xml.
     */
    protected String configLocation = "jrouter.xml";

    /** Use ThreadLocal to store Http parameter object or not */
    protected boolean useThreadLocal = true;

    /** JRouter ActionFactory */
    protected ActionFactory actionFactory;

    /** Check if is ServletActionFactory */
    private boolean isServletActionFactory = false;

    /** Check if need to log NotFoundException */
    private boolean logNotFoundException = true;

    /**
     * Http ServletContext reference.
     */
    protected ServletContext servletContext;

    @Override
    public void init(FilterConfig filterConfig) {
        this.encoding = filterConfig.getInitParameter("encoding");
        String _trimRequestParameter = filterConfig.getInitParameter("trimRequestParameter");
        String conf = filterConfig.getInitParameter("configLocation");
        String factoryName = filterConfig.getInitParameter("factoryName");
        String _useThreadLocal = filterConfig.getInitParameter("useThreadLocal");
        String _logNotFoundException = filterConfig.getInitParameter("logNotFoundException");
        //default true if not set
        if (_useThreadLocal != null)
            useThreadLocal = Boolean.parseBoolean(_useThreadLocal);
        if (_trimRequestParameter != null)
            this.trimRequestParameter = Boolean.parseBoolean(_trimRequestParameter);
        if (conf != null)
            this.configLocation = conf;
        //default true if not set
        if (_logNotFoundException != null)
            logNotFoundException = Boolean.parseBoolean(_logNotFoundException);
        log.info("Set character encoding : {}", encoding);
        servletContext = filterConfig.getServletContext();
        try {
            if (useThreadLocal) {
                //初始化ServletContext, 提供其他模块初始化调用
                ServletThreadContext.setServletContext(servletContext);
            }
            //create ActionFactory
            actionFactory = createActionFactory(filterConfig);

            if (factoryName != null) {
                servletContext.setAttribute(factoryName, actionFactory);
                log.info("Set ActionFactory's name in ServletContext : {}", factoryName);
            }
            isServletActionFactory = (actionFactory instanceof ServletActionFactory);
        } finally {
            if (useThreadLocal)
                ServletThreadContext.remove();
        }
    }

    /**
     * A hook to give subclass another way to create ActionFactory.
     *
     * @param filterConfig 过滤器配置。
     *
     * @return ActionFactory对象。
     */
    protected ActionFactory createActionFactory(FilterConfig filterConfig) {
        log.info("Load configuration location : {}", configLocation);
        Configuration configuration = new Configuration();
        configuration.load(configLocation);
        return configuration.buildActionFactory();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws
            IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        if (encoding != null) {
            request.setCharacterEncoding(encoding);
            response.setCharacterEncoding(encoding);
        }
        if (trimRequestParameter) {
            Map map = request.getParameterMap();
            if (!(map == null || map.isEmpty())) {
                request = new TrimParameterRequestWraper(request);
            }
        }
        //create thread local
        if (useThreadLocal)
            createServletThreadContext(request, response);
        try {
            //action url and invoke
            if (isServletActionFactory) {
                ((ServletActionFactory) actionFactory).invokeAction(getActionPath(request), request, response, servletContext);
            } else {
                actionFactory.invokeAction(getActionPath(request));
            }
            if (!response.isCommitted())
                chain.doFilter(request, response);
        } catch (NotFoundException e) {
            if (logNotFoundException) {
                log.error(e.getMessage(), e);
            }
            response.sendError(HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (InvocationProxyException e) {
            throw new ServletException(e.getSource());
        } finally {
            if (useThreadLocal)
                ServletThreadContext.remove();
        }
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
     * Put request & response in thread local variable.
     *
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     */
    protected void createServletThreadContext(HttpServletRequest request,
            HttpServletResponse response) {
        ServletThreadContext.setRequest(request);
        ServletThreadContext.setResponse(response);
    }

    @Override
    public void destroy() {
        if (actionFactory != null) {
            actionFactory.clear();
        }
        ServletThreadContext.remove();
    }

    /** TrimParameterRequestWraper */
    private static final class TrimParameterRequestWraper extends HttpServletRequestWrapper {

        /**
         * Constructs a request object wrapping the given request.
         *
         * @throws java.lang.IllegalArgumentException if the request is null
         */
        public TrimParameterRequestWraper(HttpServletRequest request) {
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
