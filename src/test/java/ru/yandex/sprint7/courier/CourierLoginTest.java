package ru.yandex.sprint7.courier;

import io.qameta.allure.Step;
import io.restassured.response.Response;
import org.junit.AfterClass;
import org.junit.Test;
import ru.yandex.sprint7.BaseTest;
import ru.yandex.sprint7.utils.TestDataGenerator;

import static org.hamcrest.Matchers.*;

public class CourierLoginTest extends BaseTest {
    private static String login;
    private static String password;
    private static String firstName;

    @AfterClass
    public static void tearDown() {
        if (login != null && password != null) {
            try {
                CourierLoginTest test = new CourierLoginTest();
                test.setUp();
                int courierId = test.getCourierId(login, password);
                test.deleteCourier(courierId);
            } catch (Exception e) {
                // Курьер уже удален или не существует
            }
        }
    }

    @Test
    @Step("Тест успешного логина курьера")
    public void testLoginCourierSuccess() {
        login = TestDataGenerator.generateRandomLogin();
        password = TestDataGenerator.generateRandomPassword();
        firstName = TestDataGenerator.generateRandomFirstName();

        System.out.println("Создаем курьера для тестирования логина: login=" + login);
        // Создаем курьера для тестирования логина
        createCourier(login, password, firstName);

        Response response = loginCourier(login, password);

        checkLoginSuccess(response);
    }

    @Test
    @Step("Тест логина без логина")
    public void testLoginWithoutLogin() {
        // Используем курьера из testLoginCourierSuccess
        if (login == null || password == null) {
            login = TestDataGenerator.generateRandomLogin();
            password = TestDataGenerator.generateRandomPassword();
            if (firstName == null) {
                firstName = TestDataGenerator.generateRandomFirstName();
            }
            System.out.println("Создаем курьера для теста: login=" + login);
            createCourier(login, password, firstName);
        } else {
            System.out.println("Используем существующего курьера: login=" + login);
        }

        Response response = loginCourier("", password);

        checkLoginMissingFieldError(response);
    }

    @Test
    @Step("Тест логина без пароля")
    public void testLoginWithoutPassword() {
        // Используем курьера из testLoginCourierSuccess
        if (login == null || password == null) {
            login = TestDataGenerator.generateRandomLogin();
            password = TestDataGenerator.generateRandomPassword();
            if (firstName == null) {
                firstName = TestDataGenerator.generateRandomFirstName();
            }
            System.out.println("Создаем курьера для теста: login=" + login);
            createCourier(login, password, firstName);
        } else {
            System.out.println("Используем существующего курьера: login=" + login);
        }

        Response response = loginCourier(login, "");

        checkLoginMissingFieldError(response);
    }

    @Test
    @Step("Тест логина с неверным паролем")
    public void testLoginWithWrongPassword() {
        // Используем курьера из testLoginCourierSuccess
        if (login == null || password == null) {
            login = TestDataGenerator.generateRandomLogin();
            password = TestDataGenerator.generateRandomPassword();
            if (firstName == null) {
                firstName = TestDataGenerator.generateRandomFirstName();
            }
            System.out.println("Создаем курьера для теста: login=" + login);
            createCourier(login, password, firstName);
        } else {
            System.out.println("Используем существующего курьера: login=" + login + " с неверным паролем");
        }

        Response response = loginCourier(login, "wrong_password");

        checkLoginInvalidCredentialsError(response);
    }

    @Test
    @Step("Тест логина несуществующего курьера")
    public void testLoginNonExistentCourier() {
        Response response = loginCourier("nonexistent_login", "nonexistent_password");

        checkLoginInvalidCredentialsError(response);
    }

    @Step("Проверить успешный логин")
    private void checkLoginSuccess(Response response) {
        response.then()
                .statusCode(200)
                .body("id", notNullValue());
    }

    @Step("Проверить ошибку при отсутствии обязательных полей")
    private void checkLoginMissingFieldError(Response response) {
        response.then()
                .statusCode(400);
    }

    @Step("Проверить ошибку при неверных учетных данных")
    private void checkLoginInvalidCredentialsError(Response response) {
        response.then()
                .statusCode(404); // API возвращает 404 вместо 400
    }
}