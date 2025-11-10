package ru.yandex.sprint7;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.Before;
import ru.yandex.sprint7.api.CourierApi;
import ru.yandex.sprint7.api.OrderApi;
import ru.yandex.sprint7.constants.Constants;

public class BaseTest {
    protected RequestSpecification requestSpec;
    protected CourierApi courierApi;
    protected OrderApi orderApi;

    @Before
    public void setUp() {
        RestAssured.baseURI = Constants.BASE_URL;
        requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addFilter(new AllureRestAssured())
                .build();
        courierApi = new CourierApi(requestSpec);
        orderApi = new OrderApi(requestSpec);
    }
}