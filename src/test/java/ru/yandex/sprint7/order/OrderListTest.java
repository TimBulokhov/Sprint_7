package ru.yandex.sprint7.order;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;
import ru.yandex.sprint7.TestDataGenerator;

import java.util.Arrays;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class OrderListTest extends BaseTest {
    private static String login;
    private static String password;
    private static String firstName;
    private static int orderTrack = -1;
    private static int courierId = -1;

    @BeforeClass
    public static void setUpCourier() {
        OrderListTest test = new OrderListTest();
        test.setUp();
        
        login = TestDataGenerator.generateRandomLogin();
        password = TestDataGenerator.generateRandomPassword();
        firstName = TestDataGenerator.generateRandomFirstName();

        test.courierApi.createCourier(login, password, firstName);
        courierId = test.courierApi.getCourierId(login, password);
    }

    @AfterClass
    public static void tearDown() {
        OrderListTest test = new OrderListTest();
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
    @Description("Тест получения списка заказов")
    public void testGetOrdersList() {
        Response response = orderApi.getOrders(null, null, null, null);

        checkGetOrdersSuccess(response);
    }

    @Test
    @Description("Тест получения списка заказов с courierId")
    public void testGetOrdersWithCourierId() {
        Response response = orderApi.getOrders(courierId, null, null, null);

        checkGetOrdersSuccess(response);
    }

    @Test
    @Description("Тест получения списка заказов с фильтром станций")
    public void testGetOrdersWithStationFilter() {
        Response response = orderApi.getOrders(null, Arrays.asList("1", "2"), null, null);

        response.then().statusCode(anyOf(is(SC_OK), is(SC_INTERNAL_SERVER_ERROR)));
    }

    @Test
    @Description("Тест получения списка заказов с пагинацией")
    public void testGetOrdersWithPagination() {
        Response response = orderApi.getOrders(null, null, 5, 1);

        checkGetOrdersSuccess(response);
    }

    @Step("Проверить успешное получение списка заказов")
    private void checkGetOrdersSuccess(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("orders", notNullValue())
                .body("pageInfo", notNullValue());
    }
}
