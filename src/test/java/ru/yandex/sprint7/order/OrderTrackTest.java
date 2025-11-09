package ru.yandex.sprint7.order;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;
import ru.yandex.sprint7.constants.Constants;
import ru.yandex.sprint7.TestDataGenerator;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class OrderTrackTest extends BaseTest {
    private static int orderTrack = -1;

    @AfterClass
    public static void tearDown() {
        if (orderTrack != -1) {
            try {
                OrderTrackTest test = new OrderTrackTest();
                test.setUp();
                test.orderApi.cancelOrder(orderTrack);
            } catch (Exception e) {
                // Заказ уже отменен или не существует
            }
        }
    }

    @Test
    @Description("Тест успешного получения заказа по трек-номеру")
    public void testGetOrderByTrackSuccess() {
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
        orderTrack = orderApi.getOrderTrack(createOrderResponse);

        Response response = orderApi.getOrderByTrack(orderTrack);

        checkGetOrderSuccess(response);
    }

    @Test
    @Description("Тест получения заказа без трек-номера")
    public void testGetOrderWithoutTrack() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .get(Constants.ORDERS_TRACK_PATH);

        checkGetOrderError(response);
    }

    @Test
    @Description("Тест получения заказа с неверным трек-номером")
    public void testGetOrderWithInvalidTrack() {
        Response response = orderApi.getOrderByTrack(999999);

        checkGetOrderError(response);
    }

    @Step("Проверить успешное получение заказа")
    private void checkGetOrderSuccess(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("order", notNullValue())
                .body("order.id", notNullValue())
                .body("order.firstName", notNullValue())
                .body("order.lastName", notNullValue());
    }

    @Step("Проверить ошибку при получении заказа")
    private void checkGetOrderError(Response response) {
        response.then()
                .statusCode(anyOf(is(SC_BAD_REQUEST), is(SC_NOT_FOUND)));
    }
}
