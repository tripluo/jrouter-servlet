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

import java.lang.reflect.Method;
import net.jrouter.http.DemoAction;
import net.jrouter.http.netty.result.HttpResult;
import org.springframework.util.ReflectionUtils;
import static org.testng.Assert.assertEquals;
import org.testng.annotations.Test;

/**
 * JRouterHttpRequestHandlerNGTest.
 */
public class JRouterHttpRequestHandlerNGTest extends NettyHttpServerBaseTest {

    private static String parseActionPath(String str) throws Exception {
        Method m = ReflectionUtils.findMethod(JRouterHttpRequestHandler.class, "parseActionPath", String.class);
        m.setAccessible(true);
        return (String) ReflectionUtils.invokeMethod(m, null, str);
    }

    /**
     * Test of parseActionPath method, of class JRouterHttpRequestHandler.
     */
    @Test
    public void testParseActionPathPath() throws Exception {
        assertEquals(parseActionPath(null), null);
        assertEquals(parseActionPath(""), "");
        assertEquals(parseActionPath("/"), "/");
        assertEquals(parseActionPath("/test"), "/test");
        assertEquals(parseActionPath("/test?a=123"), "/test");
        assertEquals(parseActionPath("test"), "/test");
        assertEquals(parseActionPath("/test/xyz"), "/test/xyz");
        assertEquals(parseActionPath("/test/xyz?a=123"), "/test/xyz");
        assertEquals(parseActionPath("test/xyz"), "/test/xyz");

        assertEquals(parseActionPath("http://local.com"), "/");
        assertEquals(parseActionPath("http://local.com/"), "/");
        assertEquals(parseActionPath("http://local.com/test"), "/test");
        assertEquals(parseActionPath("http://local.com/test?a=123"), "/test");
        assertEquals(parseActionPath("https://local.com/test/xyz"), "/test/xyz");
        assertEquals(parseActionPath("https://local.com/test/xyz?a=123"), "/test/xyz");
    }

    @Override
    public HttpServerActionFactory getHttpServerActionFactory() {
        HttpServerActionFactory.DefaultHttpActionFactory.Properties properties = new HttpServerActionFactory.DefaultHttpActionFactory.Properties();
        properties.setDefaultResultType(HttpResult.TEXT);
        HttpServerActionFactory.DefaultHttpActionFactory actionFactory = new HttpServerActionFactory.DefaultHttpActionFactory(properties);
        actionFactory.addResultTypes(new HttpResult());
        actionFactory.addActions(DemoAction.class);
        return actionFactory;
    }

}
