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

package net.jrouter.http.netty.result;

import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.util.internal.StringUtil;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import net.jrouter.annotation.ResultType;
import net.jrouter.http.netty.HttpActionInvocation;

/**
 * Result for http response.
 */
public class HttpResult {

    /**
     * "text" symbol.
     */
    public static final String TEXT = "text";

    /**
     * @param invocation Action运行时上下文。
     * @return 返回forward后的调用结果。
     */
    @ResultType(type = TEXT)
    public static Object text(HttpActionInvocation invocation) {
        Object res = invocation.getInvokeResult();
        FullHttpResponse response = invocation.getResponse();
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, HttpHeaderValues.TEXT_PLAIN);
        response.content().writeCharSequence(Objects.toString(res, StringUtil.EMPTY_STRING), StandardCharsets.UTF_8);
        return res;
    }

}
