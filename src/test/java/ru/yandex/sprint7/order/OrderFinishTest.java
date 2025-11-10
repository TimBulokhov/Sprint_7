package ru.yandex.sprint7.order;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;
import ru.yandex.sprint7.constants.Constants;
import ru.yandex.sprint7.TestDataGenerator;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class OrderFinishTest extends BaseTest {
    private static String login;
    private static String password;
    private static String firstName;
    private static int orderTrack = -1;
    private static int orderId = -1;
    private static int courierId = -1;

    @BeforeClass
    public static void setUpCourier() {
        OrderFinishTest test = new OrderFinishTest();
        test.setUp();
        
        login = TestDataGenerator.generateRandomLogin();
        password = TestDataGenerator.generateRandomPassword();
        firstName = TestDataGenerator.generateRandomFirstName();

        test.courierApi.createCourier(login, password, firstName);
        courierId = test.courierApi.getCourierId(login, password);
    }

    @AfterClass
    public static void tearDown() {
        OrderFinishTest test = new OrderFinishTest();
        test.setUp();
        
        if (orderTrack != -1) {
            try {
                test.orderApi.cancelOrder(orderTrack);
            } catch (Exception e) {
                // Заказ уже отменен или не существует
            }
        }

        if (login != null && password != null && courierId != -1) {
            try {
                test.courierApi.deleteCourier(courierId);
            } catch (Exception e) {
                // Курьер уже удален или не существует
            }
        }
    }

    @Test
    @Description("Тест успешного завершения заказа")
    public void testFinishOrderSuccess() {
        String orderFirstName = TestDataGenerator.generateRandomFirstName();
        String orderLastName = TestDataGenerator.generateRandomLastNameForFirstName(orderFirstName);
        String address = TestDataGenerator.generateRandomAddress();
        String metroStation = TestDataGenerator.generateRandomMetroStation();
        String phone = TestDataGenerator.generateRandomPhone();
        int rentTime = TestDataGenerator.generateRandomRentTime();
        String deliveryDate = TestDataGenerator.generateRandomDeliveryDate();
        String comment = TestDataGenerator.generateRandomComment();

        Response createOrderResponse = orderApi.createOrder(
                orderFirstName,
                orderLastName,
                address,
                metroStation,
                phone,
                rentTime,
                deliveryDate,
                comment,
                Arrays.asList("BLACK")
        );
        orderTrack = orderApi.getOrderTrack(createOrderResponse);

        Response trackResponse = orderApi.getOrderByTrack(orderTrack);
        orderId = trackResponse.then().extract().path("order.id");

        orderApi.acceptOrder(orderId, courierId);

        Response response = orderApi.finishOrder(orderId);

        checkFinishSuccess(response);
        if (response.getStatusCode() == SC_OK) {
            orderTrack = -1;
        }
    }

    @Test
    @Description("Тест завершения заказа без id")
    public void testFinishOrderWithoutId() {
        Response response = given()
                .spec(requestSpec)
                .body("{}")
                .when()
                .put(Constants.ORDERS_FINISH_PATH);

        checkFinishError(response);
    }

    @Test
    @Description("Тест завершения заказа с неверным id")
    public void testFinishOrderWithInvalidId() {
        int testOrderId = (orderId != -1) ? orderId : 999999;
        Response response = orderApi.finishOrder(testOrderId);

        checkFinishError(response);
    }

    @Step("Проверить успешное завершение заказа")
    private void checkFinishSuccess(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("ok", equalTo(true));
    }

    @Step("Проверить ошибку при завершении заказа")
    private void checkFinishError(Response response) {
        response.then()
                .statusCode(anyOf(is(SC_BAD_REQUEST), is(SC_NOT_FOUND), is(SC_CONFLICT)));
    }
}
