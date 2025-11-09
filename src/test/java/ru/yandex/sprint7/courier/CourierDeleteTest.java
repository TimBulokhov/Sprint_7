package ru.yandex.sprint7.courier;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;
import ru.yandex.sprint7.constants.Constants;
import ru.yandex.sprint7.TestDataGenerator;

import static io.restassured.RestAssured.given;
import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class CourierDeleteTest extends BaseTest {
    private String login;
    private String password;
    private String firstName;
    private int courierId;

    @Before
    public void setUpCourier() {
        login = TestDataGenerator.generateRandomLogin();
        password = TestDataGenerator.generateRandomPassword();
        firstName = TestDataGenerator.generateRandomFirstName();

        courierApi.createCourier(login, password, firstName);
        courierId = courierApi.getCourierId(login, password);
    }

    @After
    public void tearDown() {
        if (courierId > 0) {
            try {
                courierApi.deleteCourier(courierId);
            } catch (Exception e) {
                // Курьер уже удален или не существует
            }
        }
    }

    @Test
    @Description("Тест успешного удаления курьера")
    public void testDeleteCourierSuccess() {
        Response response = courierApi.deleteCourier(courierId);

        checkDeleteSuccess(response);
    }

    @Test
    @Description("Тест удаления курьера без id")
    public void testDeleteCourierWithoutId() {
        Response response = given()
                .spec(requestSpec)
                .body("{}")
                .when()
                .delete(Constants.COURIER_PATH + "/");

        checkDeleteError(response);
    }

    @Test
    @Description("Тест удаления несуществующего курьера")
    public void testDeleteNonExistentCourier() {
        Response response = courierApi.deleteCourier(999999);

        checkDeleteError(response);
    }

    @Step("Проверить успешное удаление курьера")
    private void checkDeleteSuccess(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("ok", equalTo(true));
    }

    @Step("Проверить ошибку при удалении курьера")
    private void checkDeleteError(Response response) {
        response.then()
                .statusCode(SC_NOT_FOUND);
    }
}
