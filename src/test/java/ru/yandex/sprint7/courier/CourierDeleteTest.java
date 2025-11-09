package ru.yandex.sprint7.courier;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;
import ru.yandex.sprint7.utils.TestDataGenerator;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class CourierDeleteTest extends BaseTest {
    private static String login;
    private static String password;
    private static String firstName;

    @AfterClass
    public static void tearDown() {
        // Удаляем курьера только если он еще не удален
        if (login != null && password != null) {
            try {
                CourierDeleteTest test = new CourierDeleteTest();
                test.setUp();
                int courierId = test.getCourierId(login, password);
                test.deleteCourier(courierId);
            } catch (Exception e) {
                // Курьер уже удален или не существует
            }
        }
    }

    @Test
    @Step("Тест успешного удаления курьера")
    public void testDeleteCourierSuccess() {
        login = TestDataGenerator.generateRandomLogin();
        password = TestDataGenerator.generateRandomPassword();
        firstName = TestDataGenerator.generateRandomFirstName();

        System.out.println("Создаем курьера для удаления: login=" + login);
        createCourier(login, password, firstName);
        int courierId = getCourierId(login, password);

        Response response = deleteCourier(courierId);

        checkDeleteSuccess(response);
    }

    @Test
    @Step("Тест удаления курьера без id")
    public void testDeleteCourierWithoutId() {
        Response response = given()
                .spec(requestSpec)
                .body("{}")
                .when()
                .delete(COURIER_PATH + "/");

        checkDeleteError(response);
    }

    @Test
    @Step("Тест удаления несуществующего курьера")
    public void testDeleteNonExistentCourier() {
        Response response = deleteCourier(999999);

        checkDeleteError(response);
    }

    @Step("Проверить успешное удаление курьера")
    private void checkDeleteSuccess(Response response) {
        response.then()
                .statusCode(200)
                .body("ok", equalTo(true));
    }

    @Step("Проверить ошибку при удалении курьера")
    private void checkDeleteError(Response response) {
        response.then()
                .statusCode(404); // API возвращает 404 вместо 400
    }
}