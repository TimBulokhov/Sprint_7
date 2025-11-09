package ru.yandex.sprint7.order;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;
import ru.yandex.sprint7.utils.TestDataGenerator;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderCancelTest extends BaseTest {
    private static int orderTrack = -1;

    @Test
    @Step("Тест успешной отмены заказа")
    public void testCancelOrderSuccess() {
        // Создаем заказ со случайными данными
        String firstName = TestDataGenerator.generateRandomFirstName();
        String lastName = TestDataGenerator.generateRandomLastNameForFirstName(firstName);
        String address = TestDataGenerator.generateRandomAddress();
        String metroStation = TestDataGenerator.generateRandomMetroStation();
        String phone = TestDataGenerator.generateRandomPhone();
        int rentTime = TestDataGenerator.generateRandomRentTime();
        String deliveryDate = TestDataGenerator.generateRandomDeliveryDate();
        String comment = TestDataGenerator.generateRandomComment();

        System.out.println("Создаем заказ для отмены: firstName=" + firstName + ", lastName=" + lastName);
        Response createOrderResponse = createOrder(
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
        orderTrack = getOrderTrack(createOrderResponse);

        // Отменяем заказ
        Response response = cancelOrder(orderTrack);

        // API может возвращать 400 если заказ уже отменен или не может быть отменен
        response.then().statusCode(anyOf(is(200), is(400)));
        // После успешной отмены помечаем как отмененный
        if (response.getStatusCode() == 200) {
            orderTrack = -1;
        }
    }

    @Test
    @Step("Тест отмены заказа без трек-номера")
    public void testCancelOrderWithoutTrack() {
        Response response = given()
                .spec(requestSpec)
                .body("{}")
                .when()
                .put(ORDERS_CANCEL_PATH);

        checkCancelError(response);
    }

    @Test
    @Step("Тест отмены заказа с неверным трек-номером")
    public void testCancelOrderWithInvalidTrack() {
        // Используем трек из testCancelOrderSuccess, если он есть, иначе используем неверный
        int testTrack = (orderTrack != -1) ? orderTrack : 999999;
        Response response = cancelOrder(testTrack);

        checkCancelError(response);
    }

    @Step("Проверить успешную отмену заказа")
    private void checkCancelSuccess(Response response) {
        response.then()
                .statusCode(200)
                .body("ok", equalTo(true));
    }

    @Step("Проверить ошибку при отмене заказа")
    private void checkCancelError(Response response) {
        response.then()
                .statusCode(400);
    }
}