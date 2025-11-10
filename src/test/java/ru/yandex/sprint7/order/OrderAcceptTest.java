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

public class OrderAcceptTest extends BaseTest {
    private static String login;
    private static String password;
    private static String firstName;
    private static int orderTrack = -1;
    private static int orderId = -1;
    private static int courierId = -1;

    @BeforeClass
    public static void setUpCourier() {
        OrderAcceptTest test = new OrderAcceptTest();
        test.setUp();
        
        login = TestDataGenerator.generateRandomLogin();
        password = TestDataGenerator.generateRandomPassword();
        firstName = TestDataGenerator.generateRandomFirstName();

        test.courierApi.createCourier(login, password, firstName);
        courierId = test.courierApi.getCourierId(login, password);
    }

    @AfterClass
    public static void tearDown() {
        OrderAcceptTest test = new OrderAcceptTest();
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
    @Description("Тест успешного принятия заказа")
    public void testAcceptOrderSuccess() {
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

        Response response = orderApi.acceptOrder(orderId, courierId);

        checkAcceptSuccess(response);
    }

    @Test
    @Description("Тест принятия заказа без courierId")
    public void testAcceptOrderWithoutCourierId() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .put(Constants.ORDERS_ACCEPT_PATH + "1");

        checkAcceptError(response);
    }

    @Test
    @Description("Тест принятия заказа с неверным courierId")
    public void testAcceptOrderWithInvalidCourierId() {
        int testOrderId = (orderId != -1) ? orderId : 1;
        Response response = orderApi.acceptOrder(testOrderId, 999999);

        checkAcceptError(response);
    }

    @Test
    @Description("Тест принятия заказа с неверным orderId")
    public void testAcceptOrderWithInvalidOrderId() {
        Response response = orderApi.acceptOrder(999999, courierId);

        checkAcceptError(response);
    }

    @Step("Проверить успешное принятие заказа")
    private void checkAcceptSuccess(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("ok", equalTo(true));
    }

    @Step("Проверить ошибку при принятии заказа")
    private void checkAcceptError(Response response) {
        response.then()
                .statusCode(anyOf(is(SC_BAD_REQUEST), is(SC_NOT_FOUND)));
    }
}
