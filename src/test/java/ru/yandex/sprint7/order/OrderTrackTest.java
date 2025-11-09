package ru.yandex.sprint7.order;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;
import ru.yandex.sprint7.utils.TestDataGenerator;

import java.util.Arrays;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class OrderTrackTest extends BaseTest {
    private static int orderTrack = -1;

    @AfterClass
    public static void tearDown() {
        if (orderTrack != -1) {
            try {
                OrderTrackTest test = new OrderTrackTest();
                test.setUp();
                test.cancelOrder(orderTrack);
            } catch (Exception e) {
                // Заказ уже отменен или не существует
            }
        }
    }

    @Test
    @Step("Тест успешного получения заказа по трек-номеру")
    public void testGetOrderByTrackSuccess() {
        // Создаем заказ со случайными данными
        String firstName = TestDataGenerator.generateRandomFirstName();
        String lastName = TestDataGenerator.generateRandomLastNameForFirstName(firstName);
        String address = TestDataGenerator.generateRandomAddress();
        String metroStation = TestDataGenerator.generateRandomMetroStation();
        String phone = TestDataGenerator.generateRandomPhone();
        int rentTime = TestDataGenerator.generateRandomRentTime();
        String deliveryDate = TestDataGenerator.generateRandomDeliveryDate();
        String comment = TestDataGenerator.generateRandomComment();

        System.out.println("Создаем заказ для получения по трек-номеру: firstName=" + firstName + ", lastName=" + lastName);
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

        Response response = getOrderByTrack(orderTrack);

        checkGetOrderSuccess(response);
    }

    @Test
    @Step("Тест получения заказа без трек-номера")
    public void testGetOrderWithoutTrack() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .get(ORDERS_TRACK_PATH);

        checkGetOrderError(response);
    }

    @Test
    @Step("Тест получения заказа с неверным трек-номером")
    public void testGetOrderWithInvalidTrack() {
        Response response = getOrderByTrack(999999);

        checkGetOrderError(response);
    }

    @Step("Проверить успешное получение заказа")
    private void checkGetOrderSuccess(Response response) {
        response.then()
                .statusCode(200)
                .body("order", notNullValue())
                .body("order.id", notNullValue())
                .body("order.firstName", notNullValue())
                .body("order.lastName", notNullValue());
    }

    @Step("Проверить ошибку при получении заказа")
    private void checkGetOrderError(Response response) {
        // API возвращает 400 для отсутствующих параметров, 404 для несуществующих ресурсов
        response.then()
                .statusCode(anyOf(is(400), is(404)));
    }
}