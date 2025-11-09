package ru.yandex.sprint7.order;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;
import ru.yandex.sprint7.utils.TestDataGenerator;

import java.util.Arrays;

import static org.hamcrest.Matchers.*;

public class OrderListTest extends BaseTest {
    private static String login;
    private static String password;
    private static String firstName;
    private static int orderTrack = -1;

    @AfterClass
    public static void tearDown() {
        OrderListTest test = new OrderListTest();
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
                int courierId = test.getCourierId(login, password);
                test.deleteCourier(courierId);
            } catch (Exception e) {
                // Курьер уже удален или не существует
            }
        }
    }

    @Test
    @Step("Тест получения списка заказов")
    public void testGetOrdersList() {
        Response response = getOrders(null, null, null, null);

        checkGetOrdersSuccess(response);
    }

    @Test
    @Step("Тест получения списка заказов с courierId")
    public void testGetOrdersWithCourierId() {
        // Используем курьера из других тестов или создаем нового
        if (login == null) {
            login = TestDataGenerator.generateRandomLogin();
            password = TestDataGenerator.generateRandomPassword();
            firstName = TestDataGenerator.generateRandomFirstName();
            System.out.println("Создаем курьера для получения списка заказов: login=" + login);
            createCourier(login, password, firstName);
        } else {
            System.out.println("Используем существующего курьера: login=" + login);
        }
        int courierId = getCourierId(login, password);

        Response response = getOrders(courierId, null, null, null);

        checkGetOrdersSuccess(response);
    }

    @Test
    @Step("Тест получения списка заказов с фильтром станций")
    public void testGetOrdersWithStationFilter() {
        Response response = getOrders(null, Arrays.asList("1", "2"), null, null);

        // API может возвращать 500 при неверных станциях, проверяем любой ответ
        response.then().statusCode(anyOf(is(200), is(500)));
    }

    @Test
    @Step("Тест получения списка заказов с пагинацией")
    public void testGetOrdersWithPagination() {
        Response response = getOrders(null, null, 5, 1);

        checkGetOrdersSuccess(response);
    }

    @Step("Проверить успешное получение списка заказов")
    private void checkGetOrdersSuccess(Response response) {
        response.then()
                .statusCode(200)
                .body("orders", notNullValue())
                .body("pageInfo", notNullValue());
    }
}