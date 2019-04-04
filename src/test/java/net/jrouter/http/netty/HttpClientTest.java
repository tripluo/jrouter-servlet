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

import io.restassured.response.ValidatableResponse;
import net.jrouter.http.DemoAction;
import net.jrouter.http.RestUtil;
import static org.hamcrest.Matchers.equalTo;
import org.testng.annotations.Test;

/**
 * HttpClientTest.
 *
 * @see DemoAction
 */
public class HttpClientTest extends JRouterHttpRequestHandlerNGTest {

    /**
     * Test of test method, of class DemoAction.
     */
    @Test
    public void test100() {
        ValidatableResponse vs = RestUtil.givenNetty()
                .when()
                .get("/test/test100")
                .then()
                .statusCode(200)
                .body(equalTo("/test100"));
        vs.log().all();
    }

    /**
     * Test of test method, of class DemoAction.
     */
    @Test
    public void test200() {
        ValidatableResponse vs = RestUtil.givenNetty()
                .when()
                .get("/test/test200")
                .then()
                .statusCode(200)
                .body(equalTo("/test200"));
        vs.log().all();
    }
}
