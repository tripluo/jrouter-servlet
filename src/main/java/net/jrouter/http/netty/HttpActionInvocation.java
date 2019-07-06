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
import java.util.Map;
import net.jrouter.ActionInvocation;
import net.jrouter.annotation.Dynamic;

/**
 * 扩展{@code ActionInvocation<String>}，返回HTTP Servlet常用参数的接口。
 */
@Dynamic
public interface HttpActionInvocation extends ActionInvocation<String> {

    /**
     * Gets the HTTP request object.
     *
     * @return the HTTP request object.
     */
    FullHttpRequest getRequest();

    /**
     * Gets the HTTP response object.
     *
     * @return the HTTP response object.
     */
    FullHttpResponse getResponse();

    /**
     * Gets the {@code ChannelHandlerContext} object.
     *
     * @return the {@code ChannelHandlerContext} object.
     */
    ChannelHandlerContext getChannelHandlerContext();

    /**
     * Get Invocation Context Map.
     *
     * @return the Context Map.
     */
    Map<String, Object> getContextMap();
}
