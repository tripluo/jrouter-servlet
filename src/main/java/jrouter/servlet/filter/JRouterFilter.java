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
import java.util.HashMap;
import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import jrouter.ActionFactory;
import jrouter.config.Configuration;
import jrouter.impl.InvocationProxyException;
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

    /** Set http request and response encoding, use UTF-8 as default */
    protected String encoding = "UTF-8";

    /**
     * Location of the jrouter ActionFactory's configuration file, default load resource file jrouter.xml.
     */
    protected String configLocation = "jrouter.xml";

    /** JRouter ActionFactory */
    protected ActionFactory actionFactory;

    /**
     * Http ServletContext reference.
     */
    protected ServletContext servletContext;

    @Override
    public void init(FilterConfig filterConfig) {
        String encode = filterConfig.getInitParameter("encoding");
        String conf = filterConfig.getInitParameter("configLocation");
        String factoryName = filterConfig.getInitParameter("factoryName");

        if (null != encode)
            this.encoding = encode;
        if (null != conf)
            this.configLocation = conf;

        log.info("Set character encoding : " + encoding);

        try {
            //初始化ServletContext, 提供其他模块初始化调用
            servletContext = filterConfig.getServletContext();
            ServletThreadContext.set(new ServletThreadContext(new HashMap<String, Object>()));
            ServletThreadContext.setServletContext(servletContext);

            //create ActionFactory
            actionFactory = createActionFactory(filterConfig);

            if (factoryName != null) {
                servletContext.setAttribute(factoryName, actionFactory);
                log.info("Set ActionFactory's name in ServletContext : " + factoryName);
            }
        } finally {
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
        log.info("Load configuration location : " + configLocation);
        Configuration configuration = new Configuration();
        configuration.load(configLocation);
        return configuration.buildActionFactory();
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws
            IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        request.setCharacterEncoding(encoding);
        response.setCharacterEncoding(encoding);

        createServletThreadContext(request, response);
        try {
            //action url and invoke
            actionFactory.invokeAction(getActionPath(request));
            if (!response.isCommitted())
                chain.doFilter(request, response);
        } catch (InvocationProxyException e) {
            throw new ServletException(e.getSource());
        } finally {
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
        if (ServletThreadContext.get() == null) {
            ServletThreadContext.set(new ServletThreadContext(new HashMap<String, Object>()));
            ServletThreadContext.setServletContext(servletContext);
        }
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
}