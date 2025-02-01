package net.jrouter.http;

import io.restassured.RestAssured;
import io.restassured.specification.RequestSpecification;
import lombok.extern.slf4j.Slf4j;
import net.jrouter.http.netty.NettyHttpServerBaseTest;

@Slf4j
public class RestUtil {

    static {
        // RestAssured.registerParser(MediaType.TEXT_PLAIN_VALUE, Parser.TEXT);
    }

    public static RequestSpecification givenNetty() {
        return RestAssured.given().port(NettyHttpServerBaseTest.getPort());
    }

    public static RequestSpecification given(int port) {
        return RestAssured.given().port(port);
    }

    public static RequestSpecification givenLog(int port) {
        return given(port).log().all();
    }

}
