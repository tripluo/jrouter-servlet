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

package net.jrouter.http.servlet;

import java.lang.reflect.Method;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import net.jrouter.ActionFactory;
import net.jrouter.ActionInvocation;
import net.jrouter.JRouterException;
import net.jrouter.annotation.Dynamic;
import net.jrouter.impl.PathActionFactory;
import net.jrouter.support.ActionInvocationDelegate;

/**
 * {@code ServletActionFactory} invoke Action with Http parameters.
 */
public interface ServletActionFactory extends ActionFactory<String> {

    /**
     * Use this instead of {@link #invokeAction(Object, Object...)} to pass Http parameters.
     *
     * @param <T> Generic type.
     * @param path Action path.
     * @param request HttpServletRequest.
     * @param response HttpServletResponse.
     * @param servletContext ServletContext.
     *
     * @return Action invoked result.
     *
     * @throws JRouterException if error occurrs.
     * @see #invokeAction(Object, Object...)
     */
    <T> T invokeAction(String path, HttpServletRequest request, HttpServletResponse response,
                       ServletContext servletContext) throws JRouterException;

    /**
     * 提供{@code ServletActionFactory}接口默认实现。覆写{@link #createActionInvocation}方法创建{@code ServletActionFactory}接口对象。
     *
     * @see #createActionInvocation(java.lang.String, java.lang.Object...)
     */
    class DefaultServletActionFactory extends PathActionFactory.ColonString implements ServletActionFactory {

        /** Use ThreadLocal to store Http parameter object or not */
        @lombok.Getter
        private final boolean useThreadLocal;

        /* Action path是否大小写敏感，默认区分大小写 **/
        @lombok.Getter
        private final boolean actionPathCaseSensitive;

        /**
         * Constructor.
         *
         * @param properties Properties
         */
        public DefaultServletActionFactory(Properties properties) {
            super(properties);
            this.useThreadLocal = properties.useThreadLocal;
            this.actionPathCaseSensitive = properties.actionPathCaseSensitive;
        }

        @Override
        public <T> T invokeAction(String path, HttpServletRequest request, HttpServletResponse response,
                                  ServletContext sc) throws JRouterException {
            //invoke and pass http parameters
            return (T) super.invokeAction(actionPathCaseSensitive ? path : path.toLowerCase(Locale.getDefault()),
                    request, response, sc);
        }

        /**
         * 创建并返回{@link ServletActionInvocation}接口对象。
         *
         * @return {@link ServletActionInvocation}接口对象。
         */
        @Override
        protected ActionInvocation<String> createActionInvocation(String path, Object... params) {
            //create servlet parameters ActionInvocation
            ActionInvocation<String> invocation = super.createActionInvocation(path, params);
            DefaultServletActionInvocation servletInvocation = null;

            //优先从invokeAction参数中获取Http参数对象，已由invokeAction方法指定参数顺序
            if (checkHttpParameters(params)) {
                servletInvocation = new DefaultServletActionInvocation(invocation,
                        (HttpServletRequest) params[0],
                        (HttpServletResponse) params[1],
                        (ServletContext) params[2],
                        //TODO
                        ServletThreadContext.getContextMap());
            }
            //use ThreadLocal
            if (servletInvocation == null && useThreadLocal) {
                servletInvocation = new DefaultServletActionInvocation(invocation,
                        ServletThreadContext.getRequest(),
                        ServletThreadContext.getResponse(),
                        ServletThreadContext.getServletContext(),
                        ServletThreadContext.getContextMap());
            }

            //store in ServletThreadContext ThreadLocal if needed
            if (useThreadLocal) {
                ServletThreadContext.setActionInvocation(servletInvocation);
            }
            if (servletInvocation != null) {
                return servletInvocation;
            } else {
                //return ActionInvocation if can't get any http parameters and null DefaultServletActionInvocation
                return invocation;
            }
        }

        /**
         * 检测{@link #invokeAction}方法传递过来参数的正确性。
         *
         * @param params 由{@link #invokeAction}方法传递过来参数。
         *
         * @return 参数是否为正确的Http Servlet对象。
         *
         * @see #invokeAction(java.lang.String, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, javax.servlet.ServletContext)
         */
        private boolean checkHttpParameters(Object... params) {
            return params != null && params.length == 3
                    && (params[0] instanceof HttpServletRequest)
                    && (params[1] instanceof HttpServletResponse)
                    && (params[2] instanceof ServletContext);
        }

        /**
         * DefaultServletActionFactory 属性。
         */
        @lombok.Getter
        @lombok.Setter
        public static class Properties extends ColonString.Properties {

            /**
             * @see DefaultServletActionFactory#useThreadLocal
             */
            private boolean useThreadLocal = true;

            /**
             * @see DefaultServletActionFactory#actionPathCaseSensitive
             */
            private boolean actionPathCaseSensitive = true;

            @Override
            protected String buildActionPath(String namespace, String aname, Method method) {
                String path = super.buildActionPath(namespace, aname, method);
                return actionPathCaseSensitive ? path : path.toLowerCase(Locale.getDefault());
            }
        }
    }

    /**
     * 扩展{@code ActionInvocation}，提供获取Http参数对象，并提供给参数转换器。
     */
    @Dynamic
    class DefaultServletActionInvocation extends ActionInvocationDelegate<String> implements ServletActionInvocation {

        /** Http request */
        private final HttpServletRequest request;

        /** Http response */
        private final HttpServletResponse response;

        /** Http request parameters map */
        private final RequestMap requestMap;

        /** ServletContext */
        private final ServletContext servletContext;

        /** Store key-value */
        private final Map<String, Object> contextMap;

        public DefaultServletActionInvocation(ActionInvocation<String> invocation, HttpServletRequest request, //NOPMD ExcessiveParameterList
                                              HttpServletResponse response, ServletContext servletContext, Map<String, Object> contextMap) {
            super();
            this.delegate = invocation;
            this.request = request;
            this.requestMap = new RequestMap(request);
            this.response = response;
            this.servletContext = servletContext;
            this.contextMap = contextMap;
            this.setConvertParameters(this);
        }

        @Override
        public HttpServletRequest getRequest() {
            return this.request;
        }

        @Override
        public HttpSession getSession() {
            return getRequest().getSession();
        }

        @Override
        public Map<String, String[]> getRequestParameters() {
            return this.requestMap;
        }

        @Override
        public HttpServletResponse getResponse() {
            return this.response;
        }

        @Override
        public ServletContext getServletContext() {
            return this.servletContext;
        }

        @Override
        public Map<String, Object> getContextMap() {
            return contextMap;
        }
    }
}
