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

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpResponseEncoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.util.NettyRuntime;
import io.netty.util.concurrent.DefaultThreadFactory;
import lombok.extern.slf4j.Slf4j;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Netty Server Base Test.
 */
@Slf4j
public abstract class NettyHttpServerBaseTest {

    /** Netty server port */
    private static final int PORT = 9998;

    // Configure the server.
    EventLoopGroup bossGroup = new NioEventLoopGroup(1, new DefaultThreadFactory("http-boss"));

    EventLoopGroup workerGroup = new NioEventLoopGroup(NettyRuntime.availableProcessors() * 2, new DefaultThreadFactory("http-worker"));

    Channel serverChannel;

    /**
     * Sub-classes need to know port so they can connect
     */
    public static int getPort() {
        return PORT;
    }

    public abstract HttpServerActionFactory getHttpServerActionFactory();

    @BeforeSuite
    public final void setUpNettyServer() throws Exception {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
        serverBootstrap.option(ChannelOption.SO_REUSEADDR, true);
        serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
        serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

        serverBootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast("loggingHandler", new LoggingHandler(LogLevel.INFO));
//                        pipeline.addLast("idleStateHandler", new IdleStateHandler(0, 0, 90, TimeUnit.SECONDS));
                        pipeline.addLast("httpRequestDecoder", new HttpRequestDecoder());
                        pipeline.addLast("httpResponseEncoder", new HttpResponseEncoder());
                        pipeline.addLast("httpObjectAggregator", new HttpObjectAggregator(10 * 1024 * 1024));
                        pipeline.addLast("jrouterHttpRequestHandler", new JRouterHttpRequestHandler(getHttpServerActionFactory()));
                    }
                });

        serverChannel = serverBootstrap.bind(PORT).syncUninterruptibly().channel();
        log.info("Netty Server started on port(s):{}", PORT);
    }

    @AfterSuite
    public final void tearDownNettyServer() throws Exception {
        bossGroup.shutdownGracefully();
        serverChannel.closeFuture();
        workerGroup.shutdownGracefully();
        log.info("Netty Server closed on port(s):{}", PORT);
    }
}
