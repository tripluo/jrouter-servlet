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

package net.jrouter.http.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import net.jrouter.ActionFactory;
import net.jrouter.ActionInvocation;
import net.jrouter.JRouterException;
import net.jrouter.PathGenerator;
import net.jrouter.annotation.Dynamic;
import net.jrouter.impl.PathActionFactory;
import net.jrouter.support.ActionInvocationDelegate;

/**
 * {@code HttpServerActionFactory} invoke Action with Http parameters.
 */
public interface HttpServerActionFactory extends ActionFactory<String> {

    /**
     * Use this instead of {@link #invokeAction(Object, Object...)} to pass Http parameters.
     *
     * @param <T> Generic type.
     * @param path Action path.
     * @param request FullHttpRequest.
     * @param response FullHttpResponse.
     * @param context ChannelHandlerContext.
     *
     * @return Action invoked result.
     *
     * @throws JRouterException if error occurrs.
     * @see #invokeAction(Object, Object...)
     */
    <T> T invokeAction(String path, FullHttpRequest request, FullHttpResponse response, ChannelHandlerContext context)
            throws JRouterException;

    /**
     * 提供{@code ServletActionFactory}接口默认实现。覆写{@link #createActionInvocation}方法创建{@code HttpServerActionFactory}接口对象。
     *
     * @see #createActionInvocation(java.lang.String, java.lang.Object...)
     */
    class DefaultHttpActionFactory extends PathActionFactory implements HttpServerActionFactory {

        /* Action path是否大小写敏感，默认区分大小写 **/
        @lombok.Getter
        private final boolean actionPathCaseSensitive;

        /**
         * Constructor.
         *
         * @param properties Properties
         */
        public DefaultHttpActionFactory(Properties properties) {
            super(properties);
            this.actionPathCaseSensitive = properties.actionPathCaseSensitive;
        }

        @Override
        public <T> T invokeAction(String path, FullHttpRequest request, FullHttpResponse response,
                                  ChannelHandlerContext sc) throws JRouterException {
            //invoke and pass http parameters
            return (T) super.invokeAction(actionPathCaseSensitive ? path : path.toLowerCase(Locale.getDefault()),
                    request, response, sc);
        }

        /**
         * 创建并返回{@link ChannelHandlerContext}接口对象。
         *
         * @return {@link ChannelHandlerContext}接口对象。
         */
        @Override
        protected ActionInvocation<String> createActionInvocation(String path, Object... params) {
            //create http parameters ActionInvocation
            ActionInvocation<String> invocation = super.createActionInvocation(path, params);
            DefaultHttpActionInvocation httpInvocation = null;

            //优先从invokeAction参数中获取Http参数对象，已由invokeAction方法指定参数顺序
            if (checkHttpParameters(params)) {
                httpInvocation = new DefaultHttpActionInvocation(invocation,
                        (FullHttpRequest) params[0],
                        (FullHttpResponse) params[1],
                        (ChannelHandlerContext) params[2],
                        new HashMap<>(4)
                );
                return httpInvocation;
            }
            //return ActionInvocation if can't get any http parameters
            return invocation;
        }

        /**
         * 检测{@link #invokeAction}方法传递过来参数的正确性。
         *
         * @param params 由{@link #invokeAction}方法传递过来参数。
         *
         * @return 参数是否为正确的Http参数对象。
         *
         * @see #invokeAction(String, FullHttpRequest, FullHttpResponse, ChannelHandlerContext)
         */
        private boolean checkHttpParameters(Object... params) {
            return params != null && params.length == 3
                    && (params[0] instanceof FullHttpRequest)
                    && (params[1] instanceof FullHttpResponse)
                    && (params[2] instanceof ChannelHandlerContext);
        }

        /**
         * DefaultServletActionFactory 属性。
         */
        @lombok.Getter
        @lombok.Setter
        public static class Properties extends ColonString.Properties {

            /**
             * @see DefaultHttpActionFactory#actionPathCaseSensitive
             */
            private boolean actionPathCaseSensitive = true;

            @Override
            protected void afterPropertiesSet() {
                if (getPathGenerator() == null) {
                    PathGenerator<String> pathGenerator = new StringPathGenerator(this.getPathSeparator()) {

                        @Override
                        protected String buildActionPath(String namespace, String aname, Method method) {
                            String path = super.buildActionPath(namespace, aname, method);
                            return actionPathCaseSensitive ? path : path.toLowerCase(Locale.getDefault());
                        }
                    };
                    setPathGenerator(pathGenerator);
                }
                super.afterPropertiesSet();
            }
        }
    }

    /**
     * 扩展{@code ActionInvocation}，提供获取Http参数对象，并提供给参数转换器。
     */
    @Dynamic
    class DefaultHttpActionInvocation extends ActionInvocationDelegate<String> implements HttpActionInvocation {

        /** Http request */
        private final FullHttpRequest request;

        /** Http response */
        private final FullHttpResponse response;

        /** ChannelHandlerContext */
        private final ChannelHandlerContext channelHandlerContext;

        /** Store key-value */
        private final Map<String, Object> contextMap;

        public DefaultHttpActionInvocation(ActionInvocation<String> invocation, FullHttpRequest request, //NOPMD ExcessiveParameterList
                                           FullHttpResponse response, ChannelHandlerContext channelHandlerContext, Map<String, Object> contextMap) {
            super();
            this.delegate = invocation;
            this.request = request;
            this.response = response;
            this.channelHandlerContext = channelHandlerContext;
            this.contextMap = contextMap;
            this.setConvertParameters(this);
        }

        @Override
        public FullHttpRequest getRequest() {
            return this.request;
        }

        @Override
        public FullHttpResponse getResponse() {
            return this.response;
        }

        @Override
        public ChannelHandlerContext getChannelHandlerContext() {
            return this.channelHandlerContext;
        }

        @Override
        public Map<String, Object> getContextMap() {
            return contextMap;
        }
    }

}
