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

import io.netty.channel.*;
import io.netty.handler.codec.http.*;
import io.netty.util.ReferenceCountUtil;
import java.util.Objects;
import java.util.function.BiPredicate;
import lombok.extern.slf4j.Slf4j;
import net.jrouter.NotFoundException;
import net.jrouter.util.StringUtil;

/**
 * 提供适配{@link HttpServerActionFactory}接受{@code FullHttpRequest}请求参数的处理类。
 */
@Slf4j
public class JRouterHttpRequestHandler extends ChannelInboundHandlerAdapter {

    /** 默认的路径分隔符 */
    public static final char PATH_SEPARATOR = '/';

    private static final String PATH_SEPARATOR_STRING = String.valueOf(PATH_SEPARATOR);

    @lombok.Getter
    private final HttpServerActionFactory httpServerActionFactory;

    @lombok.Getter
    private String contextPath;

    /** Check if need to log {@code NotFoundException} */
    @lombok.Getter
    @lombok.Setter
    private boolean logNotFoundException = true;

    @lombok.Setter
    @lombok.NonNull
    private BiPredicate<ChannelHandlerContext, FullHttpRequest> httpRequestPredicate = (channelHandlerContext, fullHttpRequest) -> true;

    /**
     * Constructor.
     *
     * @param httpServerActionFactory HttpServerActionFactory object.
     */
    public JRouterHttpRequestHandler(HttpServerActionFactory httpServerActionFactory) {
        Objects.requireNonNull(httpServerActionFactory, "httpServerActionFactory can't be null");
        this.httpServerActionFactory = httpServerActionFactory;
    }

    /**
     * Get action's path from URI.
     */
    private static String parseActionPath(String uri) {
        if (StringUtil.isEmpty(uri)) {
            return uri;
        }
        int start = 0;
        if (uri.charAt(0) != PATH_SEPARATOR) {
            int idx = uri.indexOf("://");
            //no ://
            if (idx > -1) {
                int sepIdx = uri.indexOf(PATH_SEPARATOR, idx + 3);
                if (sepIdx == -1) {
                    return PATH_SEPARATOR_STRING;
                } else {
                    start = sepIdx;
                }
            } else {
                uri = PATH_SEPARATOR + uri;
            }
        }
        int end = findPathEndIndex(uri);
        return end > -1 ? uri.substring(start, end) : uri.substring(start);
    }

    private static int findPathEndIndex(String uri) {
        int len = uri.length();
        for (int i = 0; i < len; i++) {
            char c = uri.charAt(i);
            if (c == '?' || c == '#') {
                return i;
            }
        }
        return len;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        FullHttpRequest fullHttpRequest;
        if (msg instanceof FullHttpRequest && httpRequestPredicate.test(ctx, fullHttpRequest = (FullHttpRequest) msg)) {
            try {
                FullHttpResponse fullHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
                String uri = fullHttpRequest.uri();
                Object res = null;
                invoke:
                try {
                    String actionPath = parseActionPath(uri);
                    if (StringUtil.isNotBlank(contextPath) && !PATH_SEPARATOR_STRING.equals(contextPath)) {
                        if (actionPath.startsWith(contextPath + PATH_SEPARATOR)) {
                            actionPath = actionPath.substring(contextPath.length());
                        } else {
                            //not match context path
                            log.warn("Uri not matched [{}] : {}", contextPath, actionPath);
                            fullHttpResponse.setStatus(HttpResponseStatus.NOT_FOUND);
                            break invoke;
                        }
                    }
                    res = httpServerActionFactory.invokeAction(actionPath, fullHttpRequest, fullHttpResponse, ctx);
                } catch (NotFoundException e) {
                    if (logNotFoundException) {
                        log.error("Not Found : {}", uri, e);
                    }
                    fullHttpResponse.setStatus(HttpResponseStatus.NOT_FOUND);
                } catch (Exception e) {
                    log.error("Internal Server Error : {}", uri, e);
                    fullHttpResponse.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
                }
                if (res instanceof FullHttpResponse) {
                    writeHttpResponse(ctx, fullHttpRequest, (FullHttpResponse) res);
                } else if (res instanceof HttpChunkedInput) {
                    HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, fullHttpResponse.headers());
                    HttpUtil.setTransferEncodingChunked(response, true);
                    // Write the initial line and the header.
                    ctx.write(response);
                    ChannelFuture lastContentFuture = ctx.writeAndFlush((HttpChunkedInput) res, ctx.newProgressivePromise());
                    // HttpChunkedInput will write the end marker (LastHttpContent) for us.
                    if (log.isDebugEnabled()) {
                        lastContentFuture.addListener(new ChannelProgressiveFutureListener() {

                            @Override
                            public void operationProgressed(ChannelProgressiveFuture future, long progress, long total) {
                                if (log.isDebugEnabled()) {
                                    log.debug(future.channel() + " Transfer progress: " + progress + " / " + total);
                                }
                            }

                            @Override
                            public void operationComplete(ChannelProgressiveFuture future) {
                                if (log.isDebugEnabled()) {
                                    log.debug(future.channel() + " Transfer complete.");
                                }
                            }
                        });
                    }
                    // Decide whether to close the connection or not.
                    if (!HttpUtil.isKeepAlive(fullHttpRequest)) {
                        // Close the connection when the whole content is written out.
                        lastContentFuture.addListener(ChannelFutureListener.CLOSE);
                    }
                } else {
                    writeHttpResponse(ctx, fullHttpRequest, fullHttpResponse, res);
                }
            } finally {
                ReferenceCountUtil.release(msg);
            }
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    protected void writeHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response) {
        boolean keepAlive = HttpUtil.isKeepAlive(request);
        HttpUtil.setContentLength(response, response.content().readableBytes());
        HttpUtil.setKeepAlive(response, keepAlive);
        ChannelFuture channelFuture = ctx.writeAndFlush(response);
        if (!keepAlive) {
            channelFuture.addListener(ChannelFutureListener.CLOSE);
        }
    }

    protected void writeHttpResponse(ChannelHandlerContext ctx, FullHttpRequest request, FullHttpResponse response,
                                     Object invokedRes) {
        writeHttpResponse(ctx, request, response);
    }

    public void setContextPath(String contextPath) {
        if (StringUtil.isNotEmpty(contextPath)) {
            //fill first /
            if (contextPath.charAt(0) != PATH_SEPARATOR) {
                contextPath = PATH_SEPARATOR + contextPath;
            }
            //trim last /
            if (contextPath.length() > 1 && PATH_SEPARATOR == contextPath.charAt(contextPath.length() - 1)) {
                contextPath = contextPath.substring(0, contextPath.length() - 1);
            }
        }
        this.contextPath = contextPath;
    }

}
