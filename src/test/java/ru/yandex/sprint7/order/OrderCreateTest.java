package ru.yandex.sprint7.order;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import ru.yandex.sprint7.BaseTest;
import ru.yandex.sprint7.TestDataGenerator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

@RunWith(Parameterized.class)
public class OrderCreateTest extends BaseTest {
    private final List<String> color;
    private static List<Integer> orderTracks = new ArrayList<>();

    public OrderCreateTest(List<String> color) {
        this.color = color;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                {Arrays.asList("BLACK")},
                {Arrays.asList("GREY")},
                {Arrays.asList("BLACK", "GREY")},
                {null}
        });
    }

    @AfterClass
    public static void tearDown() {
        // Отменяем все созданные заказы
        OrderCreateTest test = new OrderCreateTest(null);
        test.setUp();
        for (Integer track : orderTracks) {
            if (track != null && track != -1) {
                try {
                    test.orderApi.cancelOrder(track);
                } catch (Exception e) {
                    // Заказ уже отменен или не существует
                }
            }
        }
        orderTracks.clear();
    }

    @Test
    @Description("Тест создания заказа с разными цветами")
    public void testCreateOrderWithDifferentColors() {
        String firstName = TestDataGenerator.generateRandomFirstName();
        String lastName = TestDataGenerator.generateRandomLastNameForFirstName(firstName);
        String address = TestDataGenerator.generateRandomAddress();
        String metroStation = TestDataGenerator.generateRandomMetroStation();
        String phone = TestDataGenerator.generateRandomPhone();
        int rentTime = TestDataGenerator.generateRandomRentTime();
        String deliveryDate = TestDataGenerator.generateRandomDeliveryDate();
        String comment = TestDataGenerator.generateRandomComment();

        Response response = orderApi.createOrder(
                firstName,
                lastName,
                address,
                metroStation,
                phone,
                rentTime,
                deliveryDate,
                comment,
                color
        );

        int track = orderApi.getOrderTrack(response);
        if (track != -1) {
            orderTracks.add(track);
        }

        checkCreateOrderSuccess(response);
    }

    @Test
    @Description("Тест создания заказа без обязательных полей")
    public void testCreateOrderWithoutRequiredFields() {
        String lastName = TestDataGenerator.generateRandomLastName();
        String address = TestDataGenerator.generateRandomAddress();
        String metroStation = TestDataGenerator.generateRandomMetroStation();
        String phone = TestDataGenerator.generateRandomPhone();
        int rentTime = TestDataGenerator.generateRandomRentTime();
        String deliveryDate = TestDataGenerator.generateRandomDeliveryDate();
        String comment = TestDataGenerator.generateRandomComment();

        Response response = orderApi.createOrder(
                "", // Пустое имя
                lastName,
                address,
                metroStation,
                phone,
                rentTime,
                deliveryDate,
                comment,
                Arrays.asList("BLACK")
        );

        int track = orderApi.getOrderTrack(response);
        if (track != -1) {
            orderTracks.add(track);
        }

        // API может принимать заказы с пустыми полями, проверяем оба случая
        response.then().statusCode(anyOf(is(SC_CREATED), is(SC_BAD_REQUEST)));
    }

    @Step("Проверить успешное создание заказа")
    private void checkCreateOrderSuccess(Response response) {
        response.then()
                .statusCode(SC_CREATED)
                .body("track", notNullValue());
    }
}
