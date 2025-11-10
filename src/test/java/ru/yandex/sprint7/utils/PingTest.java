package ru.yandex.sprint7.utils;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class PingTest extends BaseTest {

    @Test
    public void testPingServer() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .get("/api/v1/ping");

        checkPingSuccess(response);
    }

    @Step("Проверить успешный пинг сервера")
    private void checkPingSuccess(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body(equalTo("pong;"));
    }
}