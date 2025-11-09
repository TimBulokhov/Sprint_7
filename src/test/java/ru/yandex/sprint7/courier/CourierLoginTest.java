package ru.yandex.sprint7.courier;

import io.qameta.allure.Description;
import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;
import ru.yandex.sprint7.TestDataGenerator;

import static org.apache.http.HttpStatus.*;
import static org.hamcrest.Matchers.*;

public class CourierLoginTest extends BaseTest {
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
    @Description("Тест успешного логина курьера")
    public void testLoginCourierSuccess() {
        Response response = courierApi.loginCourier(login, password);

        checkLoginSuccess(response);
    }

    @Test
    @Description("Тест логина без логина")
    public void testLoginWithoutLogin() {
        Response response = courierApi.loginCourier("", password);

        checkLoginMissingFieldError(response);
    }

    @Test
    @Description("Тест логина без пароля")
    public void testLoginWithoutPassword() {
        Response response = courierApi.loginCourier(login, "");

        checkLoginMissingFieldError(response);
    }

    @Test
    @Description("Тест логина с неверным паролем")
    public void testLoginWithWrongPassword() {
        Response response = courierApi.loginCourier(login, "wrong_password");

        checkLoginInvalidCredentialsError(response);
    }

    @Test
    @Description("Тест логина несуществующего курьера")
    public void testLoginNonExistentCourier() {
        Response response = courierApi.loginCourier("nonexistent_login", "nonexistent_password");

        checkLoginInvalidCredentialsError(response);
    }

    @Step("Проверить успешный логин")
    private void checkLoginSuccess(Response response) {
        response.then()
                .statusCode(SC_OK)
                .body("id", notNullValue());
    }

    @Step("Проверить ошибку при отсутствии обязательных полей")
    private void checkLoginMissingFieldError(Response response) {
        response.then()
                .statusCode(SC_BAD_REQUEST);
    }

    @Step("Проверить ошибку при неверных учетных данных")
    private void checkLoginInvalidCredentialsError(Response response) {
        response.then()
                .statusCode(SC_NOT_FOUND);
    }
}
