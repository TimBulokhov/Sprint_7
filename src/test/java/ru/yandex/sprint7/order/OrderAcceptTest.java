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

public class OrderAcceptTest extends BaseTest {
    private static String login;
    private static String password;
    private static String firstName;
    private static int orderTrack = -1;
    private static int orderId = -1;
    private static int courierId = -1;

    @AfterClass
    public static void tearDown() {
        OrderAcceptTest test = new OrderAcceptTest();
        test.setUp();
        
        // Отменяем заказ
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
    @Step("Тест успешного принятия заказа")
    public void testAcceptOrderSuccess() {
        // Создаем курьера
        if (login == null) {
            login = TestDataGenerator.generateRandomLogin();
            password = TestDataGenerator.generateRandomPassword();
            firstName = TestDataGenerator.generateRandomFirstName();
            System.out.println("Создаем курьера для принятия заказа: login=" + login);
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

        System.out.println("Создаем заказ для принятия: firstName=" + orderFirstName + ", metroStation=" + metroStation);
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

        Response response = acceptOrder(orderId, courierId);

        checkAcceptSuccess(response);
    }

    @Test
    @Step("Тест принятия заказа без courierId")
    public void testAcceptOrderWithoutCourierId() {
        Response response = given()
                .spec(requestSpec)
                .when()
                .put(ORDERS_ACCEPT_PATH + "1");

        checkAcceptError(response);
    }

    @Test
    @Step("Тест принятия заказа с неверным courierId")
    public void testAcceptOrderWithInvalidCourierId() {
        // Используем orderId из testAcceptOrderSuccess, если он есть
        int testOrderId = (orderId != -1) ? orderId : 1;
        Response response = acceptOrder(testOrderId, 999999);

        checkAcceptError(response);
    }

    @Test
    @Step("Тест принятия заказа с неверным orderId")
    public void testAcceptOrderWithInvalidOrderId() {
        // Используем курьера из testAcceptOrderSuccess
        if (login == null) {
            login = TestDataGenerator.generateRandomLogin();
            password = TestDataGenerator.generateRandomPassword();
            firstName = TestDataGenerator.generateRandomFirstName();
            System.out.println("Создаем курьера для теста: login=" + login);
            createCourier(login, password, firstName);
        } else {
            System.out.println("Используем существующего курьера: login=" + login);
        }
        if (courierId == -1) {
            courierId = getCourierId(login, password);
        }

        System.out.println("Попытка принять заказ с неверным orderId=999999, courierId=" + courierId);
        Response response = acceptOrder(999999, courierId);

        checkAcceptError(response);
    }

    @Step("Проверить успешное принятие заказа")
    private void checkAcceptSuccess(Response response) {
        response.then()
                .statusCode(200)
                .body("ok", equalTo(true));
    }

    @Step("Проверить ошибку при принятии заказа")
    private void checkAcceptError(Response response) {
        // API возвращает 400 для отсутствующих параметров, 404 для несуществующих ресурсов
        response.then()
                .statusCode(anyOf(is(400), is(404)));
    }
}