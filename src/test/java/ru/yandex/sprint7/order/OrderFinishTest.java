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

public class OrderFinishTest extends BaseTest {
    private static String login;
    private static String password;
    private static String firstName;
    private static int orderTrack = -1;
    private static int orderId = -1;
    private static int courierId = -1;

    @AfterClass
    public static void tearDown() {
        OrderFinishTest test = new OrderFinishTest();
        test.setUp();
        
        // Отменяем заказ если не завершен
        if (orderTrack != -1) {
            try {
                test.cancelOrder(orderTrack);
            } catch (Exception e) {
                // Заказ уже отменен или не существует
            }
        }

        // Удаляем курьера
        if (login != null && password != null) {
            try {
                if (courierId == -1) {
                    courierId = test.getCourierId(login, password);
                }
                test.deleteCourier(courierId);
            } catch (Exception e) {
                // Курьер уже удален или не существует
            }
        }
    }

    @Test
    @Step("Тест успешного завершения заказа")
    public void testFinishOrderSuccess() {
        // Создаем курьера
        if (login == null) {
            login = TestDataGenerator.generateRandomLogin();
            password = TestDataGenerator.generateRandomPassword();
            firstName = TestDataGenerator.generateRandomFirstName();
            System.out.println("Создаем курьера для завершения заказа: login=" + login);
            createCourier(login, password, firstName);
        } else {
            System.out.println("Используем существующего курьера: login=" + login);
        }
        if (courierId == -1) {
            courierId = getCourierId(login, password);
        }

        // Создаем заказ со случайными данными
        String orderFirstName = TestDataGenerator.generateRandomFirstName();
        String orderLastName = TestDataGenerator.generateRandomLastNameForFirstName(orderFirstName);
        String address = TestDataGenerator.generateRandomAddress();
        String metroStation = TestDataGenerator.generateRandomMetroStation();
        String phone = TestDataGenerator.generateRandomPhone();
        int rentTime = TestDataGenerator.generateRandomRentTime();
        String deliveryDate = TestDataGenerator.generateRandomDeliveryDate();
        String comment = TestDataGenerator.generateRandomComment();

        System.out.println("Создаем заказ для завершения: firstName=" + orderFirstName + ", metroStation=" + metroStation);
        Response createOrderResponse = createOrder(
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
        orderTrack = getOrderTrack(createOrderResponse);

        // Получаем ID заказа
        Response trackResponse = getOrderByTrack(orderTrack);
        orderId = trackResponse.then().extract().path("order.id");

        // Принимаем заказ
        acceptOrder(orderId, courierId);

        // Завершаем заказ
        Response response = finishOrder(orderId);

        checkFinishSuccess(response);
        // После успешного завершения помечаем как завершенный
        if (response.getStatusCode() == 200) {
            orderTrack = -1;
        }
    }

    @Test
    @Step("Тест завершения заказа без id")
    public void testFinishOrderWithoutId() {
        Response response = given()
                .spec(requestSpec)
                .body("{}")
                .when()
                .put(ORDERS_FINISH_PATH);

        checkFinishError(response);
    }

    @Test
    @Step("Тест завершения заказа с неверным id")
    public void testFinishOrderWithInvalidId() {
        // Используем orderId из testFinishOrderSuccess, если он есть
        int testOrderId = (orderId != -1) ? orderId : 999999;
        Response response = finishOrder(testOrderId);

        checkFinishError(response);
    }

    @Step("Проверить успешное завершение заказа")
    private void checkFinishSuccess(Response response) {
        response.then()
                .statusCode(200)
                .body("ok", equalTo(true));
    }

    @Step("Проверить ошибку при завершении заказа")
    private void checkFinishError(Response response) {
        // API возвращает 400 для отсутствующих параметров, 404 для несуществующих ресурсов, 409 для конфликтов
        response.then()
                .statusCode(anyOf(is(400), is(404), is(409)));
    }
}