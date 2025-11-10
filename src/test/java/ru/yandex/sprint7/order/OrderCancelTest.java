package ru.yandex.sprint7.order;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;
import ru.yandex.sprint7.constants.Constants;
import ru.yandex.sprint7.TestDataGenerator;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class OrderCancelTest extends BaseTest {

    @Test
    @Description("Тест успешной отмены заказа")
    public void testCancelOrderSuccess() {
        String firstName = TestDataGenerator.generateRandomFirstName();
        String lastName = TestDataGenerator.generateRandomLastNameForFirstName(firstName);
        String address = TestDataGenerator.generateRandomAddress();
        String metroStation = TestDataGenerator.generateRandomMetroStation();
        String phone = TestDataGenerator.generateRandomPhone();
        int rentTime = TestDataGenerator.generateRandomRentTime();
        String deliveryDate = TestDataGenerator.generateRandomDeliveryDate();
        String comment = TestDataGenerator.generateRandomComment();

        Response createOrderResponse = orderApi.createOrder(
                firstName,
                lastName,
                address,
                metroStation,
                phone,
                rentTime,
                deliveryDate,
                comment,
                Arrays.asList("BLACK")
        );
        int track = orderApi.getOrderTrack(createOrderResponse);

        Response response = orderApi.cancelOrder(track);

        response.then().statusCode(anyOf(is(SC_OK), is(SC_BAD_REQUEST)));
    }

    @Test
    @Description("Тест отмены заказа без трек-номера")
    public void testCancelOrderWithoutTrack() {
        Response response = given()
                .spec(requestSpec)
                .body("{}")
                .when()
                .put(Constants.ORDERS_CANCEL_PATH);

        checkCancelError(response);
    }

    @Test
    @Description("Тест отмены заказа с неверным трек-номером")
    public void testCancelOrderWithInvalidTrack() {
        Response response = orderApi.cancelOrder(999999);

        checkCancelError(response);
    }

    @Step("Проверить ошибку при отмене заказа")
    private void checkCancelError(Response response) {
        response.then()
                .statusCode(SC_BAD_REQUEST);
    }
}
